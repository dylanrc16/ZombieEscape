/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Servidor;
import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 *
 * @author Duvalier Quiros
 */
public class ChatServer {
    private static final int PORT = 8084;
    
    ServerSocket serverSocket;
    ServerFrame refFrame;
    
    ArrayList<ThreadServidor> threadServidores;
    ThreadConexiones threadConexiones;

    public ChatServer(ServerFrame refFrame) {
        this.refFrame = refFrame;
        this.runServer();
    }
    
    private void runServer(){
        try {
            serverSocket =  new ServerSocket(PORT);
            refFrame.addMessage("Leyendo conexiones ...");
            threadServidores = new ArrayList<ThreadServidor>();
            //CONECTAR 
            threadConexiones = new ThreadConexiones(this);
            threadConexiones.start();            
            
        } catch (IOException ex) {
            System.getLogger(ChatServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    //reenviar un msg a todos los conectados
    public void broadcast(Mensaje msg){
        for (ThreadServidor ts : threadServidores) {
            try {
                ts.escritorDeObjetos.writeObject(msg);
            } catch (IOException ex) {
                System.getLogger(ChatServer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }        
        
    }
    
    
    
    
    
}
