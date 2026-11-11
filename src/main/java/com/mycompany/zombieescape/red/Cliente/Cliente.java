/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Cliente;

import com.mycompany.zombieescape.modelo.Direccion;
import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 1 objectOut
 1 objectInput
 1 DataOut
 1 DataInput
 */
public class Cliente {
    private String ip = "localhost";             //la escribe el jugador en el login
    private final int PORT = 8084;
    private final int TIEMPO_CONEXION = 3000;   //ms: si en 3 s no responde, la IP está mal o el servidor no está

    Socket socket;

    ThreadCliente threadCliente;

    public ObjectOutputStream escritorDeObjetos;

    String nombre;
    String rol;

    //a quién le avisa ThreadCliente lo que llega (la ventana del juego)
    //volatile: la cambia el hilo de la ventana y la lee ThreadCliente
    private volatile EscuchaCliente escucha;

    public Cliente() {
    }

    public String getIP() {
        return ip;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public EscuchaCliente getEscucha() {
        return escucha;
    }

    public void setEscucha(EscuchaCliente escucha) {
        this.escucha = escucha;
    }

    //devuelve false si no se pudo conectar (servidor apagado, IP mala...)
    public boolean conectarCliente(String ip, String nombre, String rol){
        try {
            this.ip = ip;
            //connect con tiempo límite: sin esto, una IP equivocada deja la ventana trabada ~20 s
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, PORT), TIEMPO_CONEXION);
            escritorDeObjetos = new ObjectOutputStream(socket.getOutputStream());

            //si al otro lado hay algo que acepta la conexión pero no responde,
            //no esperamos para siempre: 5 s y se da por fallida
            socket.setSoTimeout(TIEMPO_CONEXION + 2000);
            threadCliente = new ThreadCliente(this, socket);
            socket.setSoTimeout(0);   //ya conectados: la partida puede tener silencios largos
            threadCliente.start();

            this.nombre = nombre;
            this.rol = rol;
            //NOMBRE   Diego   SNIPER
            Mensaje msg = new Mensaje("NOMBRE", 2);
            msg.parametros[0] = nombre;
            msg.parametros[1] = rol;
            enviar(msg);
            return true;
        } catch (IOException ex) {
            System.out.println("No se pudo conectar con " + ip + ":" + PORT + " -> " + ex.getMessage());
            cerrar();   //no dejar un socket a medio abrir
            return false;
        }
    }

    //cierra la conexión (por ejemplo, cuando el servidor nos rechazó)
    public void cerrar(){
        try {
            if (socket != null) socket.close();
        } catch (IOException ex) {
            //ya estaba cerrada
        }
    }

    // ======== Lo que el jugador le pide al servidor ========

    public void enviarMovimiento(Direccion d){
        Mensaje msg = new Mensaje("MOVE", 1);
        msg.parametros[0] = d.name();
        enviar(msg);
    }

    public void enviarEsconder(){
        enviar(new Mensaje("HIDE", 0));
    }

    public void enviarApuntado(int fila, int columna){
        Mensaje msg = new Mensaje("AIM", 2);
        msg.parametros[0] = String.valueOf(fila);
        msg.parametros[1] = String.valueOf(columna);
        enviar(msg);
    }

    public void enviarDisparo(int fila, int columna){
        Mensaje msg = new Mensaje("SHOOT", 2);
        msg.parametros[0] = String.valueOf(fila);
        msg.parametros[1] = String.valueOf(columna);
        enviar(msg);
    }

    public void enviarChat(String texto){
        Mensaje msg = new Mensaje("MENSAJE", 2);
        msg.parametros[0] = nombre;
        msg.parametros[1] = texto;
        enviar(msg);
    }

    public void enviarPrivado(String destinatario, String texto){
        Mensaje msg = new Mensaje("PRIVADO", 2);
        msg.parametros[0] = destinatario;
        msg.parametros[1] = texto;
        enviar(msg);
    }

    public void enviarReintentar(){
        enviar(new Mensaje("REINTENTAR", 0));
    }

    //synchronized: el teclado y el chat podrían mandar al mismo tiempo
    private synchronized void enviar(Mensaje msg){
        try {
            escritorDeObjetos.reset();
            escritorDeObjetos.writeObject(msg);
        } catch (IOException ex) {
            if (escucha != null) escucha.mostrarMensaje("No se pudo enviar: sin conexión");
        }
    }



}
