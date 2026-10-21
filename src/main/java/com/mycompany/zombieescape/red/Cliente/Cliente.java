/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.zombieescape.red.Cliente;

import com.mycompany.zombieescape.red.Models.Mensaje;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JOptionPane;

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
    VentanaLogin refPantalla;
    
    ThreadCliente threadCliente;
    
    public ObjectOutputStream escritorDeObjetos;
    
    String nombre;
    String rol;

    public Cliente(VentanaLogin refPantalla) {
        this.refPantalla = refPantalla;
    }

    public String getIP() {
        return IP;
    }
    
    public void conectarCliente(){
        try {
            socket = new Socket(IP, PORT);
            escritorDeObjetos = new ObjectOutputStream(socket.getOutputStream());
            
            threadCliente = new ThreadCliente(this, socket);
            threadCliente.start();
            
            nombre = refPantalla.nombre;
            rol = refPantalla.rol;
            //NOMBRE   Diego
            Mensaje msg = new Mensaje("NOMBRE", 2);
            msg.parametros[0] = nombre;
            msg.parametros[1] = rol;
            escritorDeObjetos.writeObject(msg);                 
        } catch (IOException ex) {
            System.getLogger(Cliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    
    
}
