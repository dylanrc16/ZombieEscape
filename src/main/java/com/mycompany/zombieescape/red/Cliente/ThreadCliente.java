/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Cliente;

import com.mycompany.zombieescape.red.Models.Mensaje;
import com.mycompany.zombieescape.red.Servidor.ThreadServidor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author diego
 */
public class ThreadCliente extends Thread{
    private boolean isRunning = true;
    private Cliente cliente;
    private Socket socket;
    private ObjectInputStream lectorDeObjetos;

    
    public ThreadCliente(Cliente cliente, Socket socket) {
        
        try {
            this.cliente = cliente;
            this.socket = socket;
            lectorDeObjetos = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.getLogger(ThreadCliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    public void run (){
        
        while (isRunning){
            
            try {
                Mensaje msg = (Mensaje)lectorDeObjetos.readObject();
                
                if (msg.tipo.equals("MENSAJE")){
                    //pintar el mensaje recibido en el textarea
                    String emisor = msg.parametros[0];
                    String texto = msg.parametros[1];
                    //cliente.refPantalla.addMessage(emisor+": "+texto);
                }else if (msg.tipo.equals("MOVE")){
                    //pintar el movimiento ...
                    String emisor = msg.parametros[0];
                    String x = msg.parametros[1];
                    String y = msg.parametros[2];
                    //cliente.refPantalla.addMessage(emisor+" se movió a "+x +","+"y");
                }
                
            } catch (IOException ex) {
                System.getLogger(ThreadServidor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            } catch (ClassNotFoundException ex) {
                System.getLogger(ThreadServidor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            
            
            
        }
        
    }
}
