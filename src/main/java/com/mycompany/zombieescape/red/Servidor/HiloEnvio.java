package com.mycompany.zombieescape.red.Servidor;

import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

/**
 * El "cartero" de UN cliente: es el único que escribe en su socket.
 * Los demás hilos (la partida, el chat) solo le dejan mensajes y siguen,
 * así un cliente con mala red no frena el juego de todos.
 *
 * - Chat y avisos: van en fila y llegan todos, en orden.
 * - Estado de la partida: solo importa el más reciente; uno nuevo reemplaza
 *   al que todavía no se había enviado.
 *
 * @author dylnr
 */
public class HiloEnvio extends Thread {
    private static final int MAX_EN_FILA = 500;   // si un cliente no lee nunca, no se llena la memoria

    private final ObjectOutputStream salida;
    private final LinkedList<Mensaje> fila = new LinkedList<>();
    private Mensaje estadoPendiente;               // la foto más reciente que falta enviar
    private boolean activo = true;
    private boolean terminando;                    // enviar lo que queda y parar

    public HiloEnvio(ObjectOutputStream salida, String nombre) {
        super("envio-" + nombre);
        this.salida = salida;
        setDaemon(true);
    }

    // Chat, avisos, rechazo...: se encolan
    public synchronized void encolar(Mensaje msg) {
        if (!activo) return;
        if (fila.size() >= MAX_EN_FILA) fila.removeFirst();
        fila.addLast(msg);
        notify();   // despierta al cartero si estaba esperando
    }

    // La foto de la partida: reemplaza a la anterior si no había salido todavía
    public synchronized void ponerEstado(Mensaje msg) {
        if (!activo) return;
        estadoPendiente = msg;
        notify();
    }

    // Espera (máximo ms) a que salga lo pendiente, por ejemplo el RECHAZO antes de cerrar el socket
    public void terminar(long ms) {
        synchronized (this) {
            terminando = true;
            notify();
        }
        try {
            join(ms);
        } catch (InterruptedException ex) {
            // seguimos cerrando
        }
        synchronized (this) {
            activo = false;
        }
    }

    // El siguiente mensaje a enviar; espera si no hay nada. null = terminar
    private synchronized Mensaje siguiente() throws InterruptedException {
        while (activo && fila.isEmpty() && estadoPendiente == null && !terminando) {
            wait();
        }
        if (!activo) return null;
        if (!fila.isEmpty()) return fila.removeFirst();
        if (estadoPendiente != null) {
            Mensaje m = estadoPendiente;
            estadoPendiente = null;
            return m;
        }
        return null;   // terminando y sin nada pendiente
    }

    @Override
    public void run() {
        try {
            Mensaje msg;
            while ((msg = siguiente()) != null) {
                // reset: sin esto Java reenvía la versión vieja de los objetos que ya mandó
                salida.reset();
                salida.writeObject(msg);
                salida.flush();
            }
        } catch (IOException | InterruptedException ex) {
            // el cliente se desconectó: su ThreadServidor se encarga de quitarlo
        }
        synchronized (this) {
            activo = false;
        }
    }
}
