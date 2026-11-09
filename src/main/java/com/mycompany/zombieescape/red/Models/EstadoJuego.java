package com.mycompany.zombieescape.red.Models;

import com.mycompany.zombieescape.modelo.Jugador;
import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.Zombie;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * "Foto" de la partida que el servidor le manda a todos los clientes.
 * El cliente no calcula nada: solo dibuja lo que viene aquí.
 *
 * @author dylnr
 */
public class EstadoJuego implements Serializable {
    private Mapa mapa;                       // null mientras la partida no ha empezado
    private ArrayList<Jugador> jugadores;
    private ArrayList<Zombie> zombies;
    private ArrayList<Jugador> ranking;      // jugadores de más a menos puntos
    private int nivel;
    private int totalNiveles;
    private int segundos;
    private String estado;                   // ESPERANDO, JUGANDO, NIVEL_SUPERADO, DERROTA, VICTORIA

    public EstadoJuego(Mapa mapa, ArrayList<Jugador> jugadores, ArrayList<Zombie> zombies,
                       ArrayList<Jugador> ranking, int nivel, int totalNiveles,
                       int segundos, String estado) {
        this.mapa = mapa;
        this.jugadores = jugadores;
        this.zombies = zombies;
        this.ranking = ranking;
        this.nivel = nivel;
        this.totalNiveles = totalNiveles;
        this.segundos = segundos;
        this.estado = estado;
    }

    // Busca al jugador con ese nombre (para que cada cliente se encuentre a sí mismo)
    public Jugador buscarJugador(String nombre) {
        for (Jugador j : jugadores) {
            if (j.getNombre().equals(nombre)) return j;
        }
        return null;
    }

    public Mapa getMapa() { return mapa; }
    public ArrayList<Jugador> getJugadores() { return jugadores; }
    public ArrayList<Zombie> getZombies() { return zombies; }
    public ArrayList<Jugador> getRanking() { return ranking; }
    public int getNivel() { return nivel; }
    public int getTotalNiveles() { return totalNiveles; }
    public int getSegundos() { return segundos; }
    public String getEstado() { return estado; }
}
