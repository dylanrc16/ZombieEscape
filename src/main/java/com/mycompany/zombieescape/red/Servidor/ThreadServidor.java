/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Servidor;


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
    
    public void run (){
        
        while (isRunning){
            
            try {
                Mensaje msg = (Mensaje)lectorDeObjetos.readObject();
                //chatServer.refFrame.addMessage("Recibido: " + msg);
                //procesar el mensaje recibido en el server desde el cliente
                if (msg.tipo.equals("NOMBRE")){
                    if (msg.parametros[0].length() > 0){
                    //escribimos en el servidor que se conectó tal persona
                        chatServer.refFrame.addMessage("Conectamos a: " + msg.parametros[0]);
                        this.nombre = msg.parametros[0];
                        this.rol = msg.parametros[1];}
                    else{
                        chatServer.refFrame.addMessage("Conectamos a nadie" + msg.parametros[0]);   
                    }
                    if (rol.equals("SNIPER")){
                        jugador = new Sniper(nombre);
                    }
                    else{
                        jugador = new Superviviente(nombre);
                    }
                }else if (msg.tipo.equals("MENSAJE")){
                    //le reenviamos el mensaje a todos los conectado
                    chatServer.broadcast(msg);
                }else if (msg.tipo.equals("MOVE")){
                    //le reenviamos el moviento a todos
                    chatServer.broadcast(msg);
                }
                
                
                
                
                
                
                
                
            } catch (IOException ex) {
                System.getLogger(ThreadServidor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            } catch (ClassNotFoundException ex) {
                System.getLogger(ThreadServidor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            
            
            
        }
        
    }
    
    
    
    
}
