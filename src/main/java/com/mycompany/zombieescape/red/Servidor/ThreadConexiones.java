/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Servidor;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author diego
 */
public class ThreadConexiones extends Thread{
    private boolean isRunning = true;
    private boolean isPause = false;
    private ChatServer chatServer;

    public ThreadConexiones(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
    
    
    public void run(){
        while (isRunning){
            try {
                Socket nuevoSocket = chatServer.serverSocket.accept();
                ThreadServidor ts = new ThreadServidor(chatServer, nuevoSocket);
                chatServer.threadServidores.add(ts);
                ts.start();
                chatServer.refFrame.addMessage("Conexión aceptada, cliente conectado!");
                chatServer.refFrame.addMessage("Esperando siguiente conexión . . .");                
            } catch (IOException ex) {
                System.getLogger(ThreadConexiones.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
    }
}
