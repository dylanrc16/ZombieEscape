package com.mycompany.zombieescape.gui;

import com.mycompany.zombieescape.modelo.Direccion;
import com.mycompany.zombieescape.modelo.Jugador;
import com.mycompany.zombieescape.red.Cliente.Cliente;
import java.util.ArrayList;

/**
 * Manda los movimientos al servidor a un ritmo fijo mientras haya una flecha presionada.
 * Las teclas solo avisan qué flecha se presionó o se soltó; este hilo es el que
 * envía y duerme. Así el sleep no congela la ventana (el hilo de Swing nunca duerme).
 *
 * @author dylnr
 */
public class HiloMovimiento extends Thread {
    private Cliente cliente;
    private volatile boolean activo = true;

    // Flechas que están presionadas ahora, en el orden en que se presionaron.
    // Manda la última: si presiona ↑ y luego →, va a la derecha; si suelta →, vuelve a ↑.
    private ArrayList<Direccion> presionadas = new ArrayList<>();

    // Un toque muy rápido (presionar y soltar antes de que el hilo despierte)
    // igual debe dar un paso; aquí se guarda para no perderlo.
    private Direccion toque;

    public HiloMovimiento(Cliente cliente) {
        this.cliente = cliente;
        setDaemon(true);   // si se cierra la ventana, este hilo no deja vivo el programa
    }

    // synchronized: lo llama el hilo de Swing y lo lee este hilo
    public synchronized void presionar(Direccion d) {
        presionadas.remove(d);   // la repetición automática de la tecla no la duplica
        presionadas.add(d);
        toque = d;
    }

    public synchronized void soltar(Direccion d) {
        presionadas.remove(d);
    }

    // Por ejemplo, si la ventana pierde el foco: Swing ya no avisaría cuando se suelte
    public synchronized void soltarTodo() {
        presionadas.clear();
        toque = null;
    }

    private synchronized Direccion siguienteDireccion() {
        Direccion d = presionadas.isEmpty() ? toque : presionadas.get(presionadas.size() - 1);
        toque = null;
        return d;
    }

    public void detener() {
        activo = false;
        interrupt();
    }

    @Override
    public void run() {
        while (activo) {
            Direccion d = siguienteDireccion();
            if (d != null) {
                cliente.enviarMovimiento(d);
            }
            try {
                Thread.sleep(Jugador.ESPERA_MOVIMIENTO);   // mismo ritmo que acepta el servidor
            } catch (InterruptedException ex) {
                return;
            }
        }
    }
}
