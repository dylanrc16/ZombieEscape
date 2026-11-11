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
    //si en este tiempo el cliente no se presenta (NOMBRE), se le cierra la conexión
    private static final int ESPERA_SALUDO = 10000;   //ms

    private boolean isRunning = true;
    private ChatServer chatServer;
    private Socket socket;
    public ObjectInputStream lectorDeObjetos;
    public ObjectOutputStream escritorDeObjetos;
    private String nombre;
    private String rol;
    Jugador jugador;

    //el constructor ya no lee ni escribe nada: así el hilo que acepta conexiones
    //nunca se queda esperando a un cliente que no manda nada
    public ThreadServidor(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.socket = socket;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void run (){
        try {
            //los streams se crean aquí, en el hilo de ESTE cliente.
            //Primero el de salida: así el cliente recibe enseguida la cabecera y no se traba
            socket.setSoTimeout(ESPERA_SALUDO);
            escritorDeObjetos = new ObjectOutputStream(socket.getOutputStream());
            lectorDeObjetos = new ObjectInputStream(socket.getInputStream());
            chatServer.agregarCliente(this);   //recién ahora puede recibir broadcasts

            while (isRunning){
                try {
                    Mensaje msg = (Mensaje)lectorDeObjetos.readObject();
                    procesar(msg);
                } catch (ClassNotFoundException ex) {
                    System.getLogger(ThreadServidor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                } catch (RuntimeException ex) {
                    //un mensaje raro o un error inesperado NO mata la conexión: se ignora ese mensaje
                    chatServer.refFrame.addMessage("Mensaje inválido de " + (nombre == null ? "un cliente" : nombre)
                            + ": " + ex);
                }
            }
        } catch (IOException ex) {
            //el cliente cerró la ventana, se cayó la red o no se presentó a tiempo
        } finally {
            //finally: se ejecuta SIEMPRE, salga como salga el hilo. Así nunca queda un jugador fantasma
            chatServer.quitar(this);
            try {
                socket.close();
            } catch (IOException ex) {
                //ya estaba cerrado
            }
        }
    }

    //procesar el mensaje recibido en el server desde el cliente
    private void procesar(Mensaje msg){
        if (msg == null || msg.tipo == null) return;
        if (msg.tipo.equals("NOMBRE")){
            registrar(msg);
        }else if (jugador == null){
            //sin NOMBRE primero no se acepta nada más
        }else if (msg.tipo.equals("MENSAJE")){
            String texto = param(msg, 1);
            if (vacio(texto)) return;
            //el emisor lo pone el servidor, así nadie se hace pasar por otro
            Mensaje chat = new Mensaje("MENSAJE", 2);
            chat.parametros[0] = nombre;
            chat.parametros[1] = texto;
            chatServer.refFrame.addMessage(nombre + ": " + texto);
            chatServer.broadcast(chat);
        }else if (msg.tipo.equals("PRIVADO")){
            privado(msg);
        }else if (msg.tipo.equals("MOVE")){
            //el cliente solo pide; Partida decide si se puede mover
            Direccion d = Direccion.textoAMovimiento(param(msg, 0));
            chatServer.partida.moverJugador(jugador, d);
        }else if (msg.tipo.equals("HIDE")){
            chatServer.partida.esconder(jugador);
        }else if (msg.tipo.equals("AIM")){
            apuntar(msg);
        }else if (msg.tipo.equals("SHOOT")){
            disparar(msg);
        }else if (msg.tipo.equals("REINTENTAR")){
            reintentar();
        }
    }

    //parametros[i] sin riesgo: null si el mensaje trae menos parámetros de los esperados
    private String param(Mensaje msg, int i){
        if (msg.parametros == null || i >= msg.parametros.length) return null;
        return msg.parametros[i];
    }

    //NOMBRE: crea el Jugador según el rol y lo mete en la partida
    private void registrar(Mensaje msg){
        if (jugador != null) return;   //ya estaba registrado
        String nombrePedido = param(msg, 0);
        String rolPedido = param(msg, 1);
        if (vacio(nombrePedido)){
            rechazar("falta el nombre", "Escriba un nombre.");
            return;
        }
        nombrePedido = nombrePedido.trim();

        //revisar y agregar tiene que ser una sola operación: si dos clientes entran al mismo
        //tiempo, sin el candado los dos podrían ver "hay campo" y pasarse del límite
        synchronized (chatServer.candadoRegistro) {
            if (chatServer.partida.getJugadores().size() >= ChatServer.MAX_JUGADORES){
                rechazar("partida llena", "La partida está llena (máximo " + ChatServer.MAX_JUGADORES + " jugadores).");
                return;
            }
            if ("SNIPER".equals(rolPedido) && contarSnipers() >= ChatServer.MAX_SNIPERS){
                rechazar("ya hay " + ChatServer.MAX_SNIPERS + " snipers", "Ya hay " + ChatServer.MAX_SNIPERS + " francotiradores. Entre como superviviente.");
                return;
            }
            if (nombreOcupado(nombrePedido)){
                rechazar("nombre repetido: " + nombrePedido, "Ya hay un jugador llamado " + nombrePedido + ". Use otro nombre.");
                return;
            }
            //escribimos en el servidor que se conectó tal persona
            this.nombre = nombrePedido;
            this.rol = rolPedido;
            if ("SNIPER".equals(rol)){
                jugador = new Sniper(nombre);
            }
            else{
                jugador = new Superviviente(nombre);
            }
            chatServer.partida.agregarJugador(jugador);
        }
        try {
            socket.setSoTimeout(0);   //ya se presentó: de aquí en adelante puede estar callado lo que quiera
        } catch (IOException ex) {
            //si falla, la conexión ya se cerró y el ciclo de run() lo va a notar
        }
        chatServer.refFrame.addMessage("Conectamos a: " + nombre + " (" + rol + ")");
        avisar("Bienvenido, " + nombre);
    }

    //sin importar mayúsculas: "ana" y "Ana" se confundirían en las ventanas
    private boolean nombreOcupado(String nombrePedido){
        for (Jugador j : chatServer.partida.getJugadores()){
            if (j.getNombre().equalsIgnoreCase(nombrePedido)) return true;
        }
        return false;
    }

    //RECHAZO: el cliente cierra su ventana de juego y vuelve al login con el motivo
    private void rechazar(String motivoServidor, String motivoJugador){
        chatServer.refFrame.addMessage("Conexion rechazada, " + motivoServidor);
        Mensaje rechazo = new Mensaje("RECHAZO", 1);
        rechazo.parametros[0] = motivoJugador;
        chatServer.enviarA(this, rechazo);
        isRunning = false;
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
        String destino = param(msg, 0);
        String texto = param(msg, 1);
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

    //REINTENTAR: si de verdad había una derrota, se repite el nivel y se avisa a todos quién lo pidió
    private void reintentar(){
        if (chatServer.partida.reintentarNivel()) {
            Mensaje aviso = new Mensaje("MENSAJE", 2);
            aviso.parametros[0] = "Servidor";
            aviso.parametros[1] = nombre + " reinició el nivel " + chatServer.partida.getNivel();
            chatServer.broadcast(aviso);
            chatServer.refFrame.addMessage(nombre + " reinició el nivel " + chatServer.partida.getNivel());
        }
    }

    //AIM: mover la mira del sniper
    private void apuntar(Mensaje msg){
        try {
            int fila = Integer.parseInt(param(msg, 0));
            int columna = Integer.parseInt(param(msg, 1));
            chatServer.partida.apuntar(jugador, fila, columna);
        } catch (NumberFormatException ex) {
            //coordenadas inválidas (o faltan): se ignora
        }
    }

    //SHOOT: el resultado (acertó, falló, sin balas...) solo se lo decimos al sniper
    private void disparar(Mensaje msg){
        try {
            int fila = Integer.parseInt(param(msg, 0));
            int columna = Integer.parseInt(param(msg, 1));
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
