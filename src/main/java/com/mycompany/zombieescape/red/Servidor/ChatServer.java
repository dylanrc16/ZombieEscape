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
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 *
 * @author Duvalier Quiros
 */
public class ChatServer implements EscuchaPartida {
    private static final int PORT = 8084;
    public static final int MAX_JUGADORES = 10;
    public static final int MAX_SNIPERS = 2;      //el enunciado: 1-2 francotiradores

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

    //Partida avisa que algo cambió: se manda la foto nueva a todos
    @Override
    public void partidaCambio(){
        EstadoJuego estado = new EstadoJuego(partida.getMapa(), partida.getJugadores(),
                partida.getZombies(), partida.getRanking(), partida.getNivel(),
                partida.getTotalNiveles(), partida.getSegundos(), partida.getEstado());
        Mensaje msg = new Mensaje("ESTADO", 0);
        msg.objeto = estado;
        broadcast(msg);
    }

    //reenviar un msg a todos los conectados
    public synchronized void broadcast(Mensaje msg){
        for (ThreadServidor ts : threadServidores) {
            enviarA(ts, msg);
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

    //mandar un msg a un solo cliente
    public synchronized void enviarA(ThreadServidor ts, Mensaje msg){
        try {
            //reset: sin esto Java reenvía la versión vieja de los objetos que ya mandó
            ts.escritorDeObjetos.reset();
            ts.escritorDeObjetos.writeObject(msg);
        } catch (IOException ex) {
            //el cliente se cayó; su ThreadServidor lo va a quitar
        }
    }





}
