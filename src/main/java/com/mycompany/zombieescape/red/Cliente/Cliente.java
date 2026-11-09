/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Cliente;

import com.mycompany.zombieescape.modelo.Direccion;
import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 1 objectOut
 1 objectInput
 1 DataOut
 1 DataInput
 */
public class Cliente {
    private final String IP = "localhost";
    private final int PORT = 8084;

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
        return IP;
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
    public boolean conectarCliente(String nombre, String rol){
        try {
            socket = new Socket(IP, PORT);
            escritorDeObjetos = new ObjectOutputStream(socket.getOutputStream());

            threadCliente = new ThreadCliente(this, socket);
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
            System.getLogger(Cliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return false;
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
