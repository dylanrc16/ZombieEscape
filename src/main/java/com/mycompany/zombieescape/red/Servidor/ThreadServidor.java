/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Servidor;


import com.mycompany.zombieescape.modelo.Direccion;
import com.mycompany.zombieescape.modelo.Jugador;
import com.mycompany.zombieescape.modelo.Sniper;
import com.mycompany.zombieescape.modelo.Superviviente;
import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author diego
 */
public class ThreadServidor extends Thread {
    private boolean isRunning = true;
    private ChatServer chatServer;
    private Socket socket;
    public ObjectInputStream lectorDeObjetos;
    public ObjectOutputStream escritorDeObjetos;
    private String nombre;
    private String rol;
    Jugador jugador;

    public ThreadServidor(ChatServer chatServer, Socket socket) {
        try {
            this.chatServer = chatServer;
            this.socket = socket;
            lectorDeObjetos = new ObjectInputStream(socket.getInputStream());
            escritorDeObjetos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.getLogger(ThreadServidor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void run (){

        while (isRunning){

            try {
                Mensaje msg = (Mensaje)lectorDeObjetos.readObject();
                //chatServer.refFrame.addMessage("Recibido: " + msg);
                //procesar el mensaje recibido en el server desde el cliente
                if (msg.tipo.equals("NOMBRE")){
                    registrar(msg);
                }else if (jugador == null){
                    //sin NOMBRE primero no se acepta nada más
                }else if (msg.tipo.equals("MENSAJE")){
                    if (vacio(msg.parametros[1])) continue;
                    //el emisor lo pone el servidor, así nadie se hace pasar por otro
                    msg.parametros[0] = nombre;
                    chatServer.refFrame.addMessage(nombre + ": " + msg.parametros[1]);
                    chatServer.broadcast(msg);
                }else if (msg.tipo.equals("PRIVADO")){
                    privado(msg);
                }else if (msg.tipo.equals("MOVE")){
                    //el cliente solo pide; Partida decide si se puede mover
                    Direccion d = Direccion.textoAMovimiento(msg.parametros[0]);
                    chatServer.partida.moverJugador(jugador, d);
                }else if (msg.tipo.equals("HIDE")){
                    chatServer.partida.esconder(jugador);
                }else if (msg.tipo.equals("AIM")){
                    apuntar(msg);
                }else if (msg.tipo.equals("SHOOT")){
                    disparar(msg);
                }

            } catch (IOException ex) {
                isRunning = false;
            } catch (ClassNotFoundException ex) {
                System.getLogger(ThreadServidor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }



        }
        //el cliente cerró la ventana o se cayó la conexión
        chatServer.quitar(this);
        try {
            socket.close();
        } catch (IOException ex) {
            //ya estaba cerrado
        }

    }

    //NOMBRE: crea el Jugador según el rol y lo mete en la partida
    private void registrar(Mensaje msg){
        if (jugador != null) return;   //ya estaba registrado
        if (msg.parametros[0] == null || msg.parametros[0].trim().isEmpty()){
            chatServer.refFrame.addMessage("Conexion rechazada, cliente sin nombre");
            avisar("Conexión rechazada: falta el nombre");
            isRunning = false;
            return;
        }
        if (chatServer.partida.getJugadores().size() >= ChatServer.MAX_JUGADORES){
            chatServer.refFrame.addMessage("Conexion rechazada, partida llena");
            avisar("La partida está llena (máximo " + ChatServer.MAX_JUGADORES + " jugadores)");
            isRunning = false;
            return;
        }
        if ("SNIPER".equals(msg.parametros[1]) && contarSnipers() >= ChatServer.MAX_SNIPERS){
            chatServer.refFrame.addMessage("Conexion rechazada, ya hay " + ChatServer.MAX_SNIPERS + " snipers");
            avisar("Ya hay " + ChatServer.MAX_SNIPERS + " francotiradores. Entre como superviviente.");
            isRunning = false;
            return;
        }
        //escribimos en el servidor que se conectó tal persona
        this.nombre = msg.parametros[0].trim();
        this.rol = msg.parametros[1];
        if ("SNIPER".equals(rol)){
            jugador = new Sniper(nombre);
        }
        else{
            jugador = new Superviviente(nombre);
        }
        chatServer.refFrame.addMessage("Conectamos a: " + nombre + " (" + rol + ")");
        avisar("Bienvenido, " + nombre);
        chatServer.partida.agregarJugador(jugador);
    }

    private int contarSnipers(){
        int cantidad = 0;
        for (Jugador j : chatServer.partida.getJugadores()){
            if (!j.puedeMoverse()) cantidad++;
        }
        return cantidad;
    }

    //PRIVADO: solo lo reciben el destinatario y el que lo mandó (para que lo vea en su chat)
    private void privado(Mensaje msg){
        String destino = msg.parametros[0];
        String texto = msg.parametros[1];
        if (vacio(destino) || vacio(texto)) return;
        ThreadServidor otro = chatServer.buscarCliente(destino);
        if (otro == null || otro == this){
            avisar(destino + " no está conectado");
            return;
        }
        Mensaje privado = new Mensaje("PRIVADO", 3);
        privado.parametros[0] = nombre;
        privado.parametros[1] = destino;
        privado.parametros[2] = texto;
        chatServer.enviarA(otro, privado);
        chatServer.enviarA(this, privado);
        chatServer.refFrame.addMessage("(privado) " + nombre + " -> " + destino);
    }

    private boolean vacio(String texto){
        return texto == null || texto.trim().isEmpty();
    }

    //AIM: mover la mira del sniper
    private void apuntar(Mensaje msg){
        try {
            int fila = Integer.parseInt(msg.parametros[0]);
            int columna = Integer.parseInt(msg.parametros[1]);
            chatServer.partida.apuntar(jugador, fila, columna);
        } catch (NumberFormatException ex) {
            //coordenadas inválidas: se ignora
        }
    }

    //SHOOT: el resultado (acertó, falló, sin balas...) solo se lo decimos al sniper
    private void disparar(Mensaje msg){
        try {
            int fila = Integer.parseInt(msg.parametros[0]);
            int columna = Integer.parseInt(msg.parametros[1]);
            String resultado = chatServer.partida.disparar(jugador, fila, columna);
            avisar(resultado);
        } catch (NumberFormatException ex) {
            avisar("Disparo inválido");
        }
    }

    //mensaje INFO solo para este cliente
    private void avisar(String texto){
        Mensaje info = new Mensaje("INFO", 1);
        info.parametros[0] = texto;
        chatServer.enviarA(this, info);
    }




}
