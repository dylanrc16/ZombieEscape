/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Servidor;
import com.mycompany.zombieescape.logica.CargadorMapas;
import com.mycompany.zombieescape.logica.EscuchaPartida;
import com.mycompany.zombieescape.logica.Partida;
import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.red.Models.EstadoJuego;
import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 *
 * @author Duvalier Quiros
 */
public class ChatServer implements EscuchaPartida {
    private static final int PORT = 8084;
    public static final int MAX_JUGADORES = 10;
    public static final int MAX_SNIPERS = 2;      //el enunciado: 1-2 francotiradores

    //candado propio para registrar jugadores (revisar límites + agregar en un solo paso).
    //Es aparte del de ChatServer para no mezclarlo con el de Partida y evitar un deadlock
    final Object candadoRegistro = new Object();

    ServerSocket serverSocket;
    ServerFrame refFrame;

    ArrayList<ThreadServidor> threadServidores;
    ThreadConexiones threadConexiones;
    Partida partida;

    public ChatServer(ServerFrame refFrame) {
        this.refFrame = refFrame;
        this.runServer();
    }

    private void runServer(){
        try {
            //cargar los escenarios y crear la partida (el servidor es el dueño del juego)
            ArrayList<Mapa> mapas = CargadorMapas.cargarTodos(CargadorMapas.CARPETA);
            partida = new Partida(mapas, this);
            refFrame.addMessage("Escenarios cargados: " + mapas.size());

            serverSocket =  new ServerSocket(PORT);
            mostrarIps();
            refFrame.addMessage("Leyendo conexiones ...");
            threadServidores = new ArrayList<ThreadServidor>();
            //CONECTAR
            threadConexiones = new ThreadConexiones(this);
            threadConexiones.start();

        } catch (IOException ex) {
            refFrame.addMessage("Error al iniciar el servidor: " + ex.getMessage());
            System.getLogger(ChatServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    //escribe las IP de esta PC, para saber cuál escribir en el login de los otros jugadores
    private void mostrarIps(){
        refFrame.addMessage("Puerto: " + PORT + ". Los jugadores de esta PC pueden usar: localhost");
        try {
            for (NetworkInterface red : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!red.isUp() || red.isLoopback()) continue;
                for (InetAddress dir : java.util.Collections.list(red.getInetAddresses())) {
                    if (dir instanceof Inet4Address) {   //solo IPv4 (las que se escriben tipo 192.168.1.15)
                        refFrame.addMessage("IP de este servidor: " + dir.getHostAddress()
                                + "  (" + red.getDisplayName() + ")");
                    }
                }
            }
        } catch (SocketException ex) {
            refFrame.addMessage("No se pudo leer la IP de esta PC (use ipconfig)");
        }
    }

    //lo llama ThreadConexiones cuando acepta un cliente nuevo
    public synchronized void agregarCliente(ThreadServidor ts){
        threadServidores.add(ts);
    }

    //lo llama ThreadServidor cuando su cliente se desconecta
    public void quitar(ThreadServidor ts){
        synchronized (this) {
            threadServidores.remove(ts);
        }
        //fuera del synchronized: Partida avisa con partidaCambio() y eso vuelve a usar broadcast
        if (ts.getJugador() != null) {
            partida.quitarJugador(ts.getJugador());
            refFrame.addMessage(ts.getJugador().getNombre() + " se desconectó");
        }
    }

    //botón "Iniciar partida" del ServerFrame
    public void iniciarPartida(){
        partida.iniciarNivel(0);
        if (partida.getEstado().equals(Partida.JUGANDO)) {
            refFrame.addMessage("Partida iniciada: nivel 1 - " + partida.getMapa().getNombre());
        } else {
            refFrame.addMessage("No se puede iniciar: se necesita al menos un superviviente");
        }
    }

    //Partida avisa que algo cambió: se manda la foto nueva a todos.
    //Se llama con Partida quieta (dentro de su synchronized): por eso la foto se empaqueta
    //AQUÍ, una sola vez para todos. Así nunca sale una foto "a medio cambiar" y no se
    //repite el trabajo por cada cliente. Después cada cartero la manda a su ritmo.
    @Override
    public void partidaCambio(){
        EstadoJuego estado = new EstadoJuego(partida.getMapa(), partida.getJugadores(),
                partida.getZombies(), partida.getRanking(), partida.getNivel(),
                partida.getTotalNiveles(), partida.getSegundos(), partida.getEstado());
        byte[] foto = empaquetar(estado);
        if (foto == null) return;
        Mensaje msg = new Mensaje("ESTADO", 0);
        msg.objeto = foto;
        synchronized (this) {
            for (ThreadServidor ts : threadServidores) {
                ts.ponerEstado(msg);
            }
        }
    }

    //EstadoJuego -> bytes (el cliente hace lo contrario al recibirlo)
    private byte[] empaquetar(EstadoJuego estado){
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream salida = new ObjectOutputStream(bytes);
            salida.writeObject(estado);
            salida.close();
            return bytes.toByteArray();
        } catch (IOException ex) {
            System.getLogger(ChatServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return null;
        }
    }

    //reenviar un msg a todos los conectados (no espera a nadie: cada cartero lo manda)
    public synchronized void broadcast(Mensaje msg){
        for (ThreadServidor ts : threadServidores) {
            ts.encolar(msg);
        }
    }

    //busca la conexión del jugador con ese nombre (para los mensajes privados)
    public synchronized ThreadServidor buscarCliente(String nombre){
        for (ThreadServidor ts : threadServidores) {
            if (ts.getJugador() != null && ts.getJugador().getNombre().equals(nombre)) {
                return ts;
            }
        }
        return null;
    }

    //mandar un msg a un solo cliente (se le deja a su cartero)
    public void enviarA(ThreadServidor ts, Mensaje msg){
        ts.encolar(msg);
    }





}
