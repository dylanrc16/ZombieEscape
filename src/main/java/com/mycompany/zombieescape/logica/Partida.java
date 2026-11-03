package com.mycompany.zombieescape.logica;

import com.mycompany.zombieescape.modelo.Direccion;
import com.mycompany.zombieescape.modelo.Jugador;
import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.Posicion;
import com.mycompany.zombieescape.modelo.Sniper;
import com.mycompany.zombieescape.modelo.Superviviente;
import com.mycompany.zombieescape.modelo.TipoCelda;
import com.mycompany.zombieescape.modelo.Zombie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

// Reglas del juego. Todos los métodos que cambian el estado son synchronized
// porque los llaman varios hilos a la vez (un ThreadServidor por jugador y un HiloZombie por zombie).
public class Partida {
    // Estados posibles (sin enum)
    public static final String ESPERANDO      = "ESPERANDO";
    public static final String JUGANDO        = "JUGANDO";
    public static final String NIVEL_SUPERADO = "NIVEL_SUPERADO";
    public static final String DERROTA        = "DERROTA";
    public static final String VICTORIA       = "VICTORIA";

    private static final String[] SKINS = {
        "jugador_gym", "jugador_traje", "jugador_futbol", "jugador_ingeniero", "jugador_medico"
    };

    // Puntos
    private static final int PUNTOS_PRIMERO = 100;  // el primero en escapar
    private static final int PUNTOS_MENOS   = 20;   // cada llegada siguiente recibe 20 menos...
    private static final int PUNTOS_MINIMO  = 20;   // ...pero nunca menos de 20
    private static final int PUNTOS_ZOMBIE  = 25;   // el sniper gana puntos por zombie eliminado
    private static final int PUNTOS_RESCATE = 30;   // por revivir a un compañero con su tarjeta

    // Disparo: probabilidad de fallar entre 10 % y 20 %
    private static final int FALLO_MIN = 10;
    private static final int FALLO_MAX = 20;

    // Zombies: milisegundos entre pasos y distancia mínima a la entrada al aparecer
    private static final int VELOCIDAD_MIN = 500;
    private static final int VELOCIDAD_MAX = 800;
    private static final int MARGEN_ENTRADA = 3;    // se suma al rango de detección
    private static final int PAUSA_ENTRE_NIVELES = 3000;

    private ArrayList<Mapa> niveles;
    private EscuchaPartida escucha;
    private Random azar = new Random();

    private Mapa mapa;
    private int nivelActual = -1;
    private String estado = ESPERANDO;
    private ArrayList<Jugador> jugadores = new ArrayList<>();
    private ArrayList<Zombie> zombies = new ArrayList<>();
    private ArrayList<HiloZombie> hilos = new ArrayList<>();
    private long inicioNivel;
    private int segundosFinales;
    private int llegadas;                      // cuántos han escapado en este nivel

    public Partida(ArrayList<Mapa> niveles, EscuchaPartida escucha) {
        this.niveles = niveles;
        this.escucha = escucha;
    }

    // ================== Jugadores que entran y salen ==================

    public synchronized void agregarJugador(Jugador j) {
        if (j.puedeMoverse()) {
            ((Superviviente) j).setSkin(SKINS[contarSupervivientes() % SKINS.length]);
        }
        jugadores.add(j);
        if (estado.equals(JUGANDO)) {
            colocar(j, jugadores.size() - 1);   // entró con la partida empezada
        }
        notificar();
    }

    public synchronized void quitarJugador(Jugador j) {
        jugadores.remove(j);
        revisarFinNivel();
        notificar();
    }

    // ================== Inicio de nivel ==================

    public synchronized void iniciarNivel(int numero) {
        if (numero < 0 || numero >= niveles.size()) return;
        if (contarSupervivientes() == 0) return;   // se necesita al menos un superviviente

        detenerZombies();
        nivelActual = numero;
        mapa = niveles.get(numero);
        llegadas = 0;

        for (int i = 0; i < jugadores.size(); i++) {
            colocar(jugadores.get(i), i);
        }
        crearZombies();

        estado = JUGANDO;
        inicioNivel = System.currentTimeMillis();
        for (HiloZombie h : hilos) {
            h.start();
        }
        notificar();
    }

    // Supervivientes en las entradas; snipers en una calle al azar (el centro de lo que ven)
    private void colocar(Jugador j, int indice) {
        if (j.puedeMoverse()) {
            ArrayList<Posicion> entradas = mapa.buscarCeldas(TipoCelda.ENTRADA);
            ((Superviviente) j).reiniciar(entradas.get(indice % entradas.size()));
        } else {
            ArrayList<Posicion> calles = mapa.buscarCeldas(TipoCelda.CAMINO);
            Sniper s = (Sniper) j;
            s.setPosicion(calles.get(azar.nextInt(calles.size())));
            s.recargar();
        }
    }

    // Zombies en calles libres, lejos de la entrada, mirando hacia un lado por el que puedan caminar
    private void crearZombies() {
        ArrayList<Posicion> entradas = mapa.buscarCeldas(TipoCelda.ENTRADA);
        ArrayList<Posicion> candidatas = new ArrayList<>();
        for (Posicion p : mapa.buscarCeldas(TipoCelda.CAMINO)) {
            if (lejosDeLaEntrada(p, entradas)) {
                candidatas.add(p);
            }
        }
        Collections.shuffle(candidatas, azar);

        Direccion[] direcciones = Direccion.values();   // las 4 direcciones del enum
        int cantidad = Math.min(mapa.getCantidadZombies(), candidatas.size());
        for (int i = 0; i < cantidad; i++) {
            Posicion p = candidatas.get(i);
            Direccion d = direcciones[azar.nextInt(direcciones.length)];
            for (int intento = 0; intento < 4 && !mapa.esCaminable(p.siguiente(d)); intento++) {
                d = d.girarDerecha();
            }
            int velocidad = VELOCIDAD_MIN + azar.nextInt(VELOCIDAD_MAX - VELOCIDAD_MIN + 1);
            Zombie z = new Zombie(p, d, velocidad);
            zombies.add(z);
            hilos.add(new HiloZombie(z, this));
        }
    }

    private boolean lejosDeLaEntrada(Posicion p, ArrayList<Posicion> entradas) {
        int minima = mapa.getDeteccion() + MARGEN_ENTRADA;
        for (Posicion e : entradas) {
            if (p.distancia(e) <= minima) return false;
        }
        return true;
    }

    // ================== Acciones de los jugadores ==================

    public synchronized void moverJugador(Jugador j, Direccion d) {
        if (!estado.equals(JUGANDO) || j == null || d == null) return;
        if (!j.isVivo() || !j.puedeMoverse()) return;   // espectadores y snipers no caminan

        Superviviente s = (Superviviente) j;
        boolean yaHabiaEscapado = s.isEscapo();
        s.pedirMovimiento(d);
        s.mover(mapa);

        if (!s.isEscapo()) {
            revisarRescates(s);
        }

        if (!yaHabiaEscapado && s.isEscapo()) {
            int puntos = Math.max(PUNTOS_MINIMO, PUNTOS_PRIMERO - PUNTOS_MENOS * llegadas);
            s.sumarPuntos(puntos);
            llegadas++;
        }
        revisarDetecciones();
        revisarFinNivel();
        notificar();
    }

    public synchronized void esconder(Jugador j) {
        if (!estado.equals(JUGANDO) || j == null || !j.puedeMoverse()) return;
        ((Superviviente) j).alternarEscondite(mapa);
        notificar();
    }

    // Devuelve un texto con el resultado, para avisarle al sniper por el chat
    public synchronized String disparar(Jugador j, int fila, int columna) {
        if (!estado.equals(JUGANDO) || j == null) return "No hay partida en curso";
        if (j.puedeMoverse()) return "Solo el sniper puede disparar";

        Sniper sn = (Sniper) j;
        Posicion objetivo = new Posicion(fila, columna);
        if (!sn.tieneBalas()) return "Sin balas";
        if (!sn.puedeVer(objetivo)) return "Ese punto está fuera de tu vista";

        sn.gastarBala();

        // Margen de error: entre 10 % y 20 % de las veces la bala se desvía a una celda vecina
        int probabilidadFallo = FALLO_MIN + azar.nextInt(FALLO_MAX - FALLO_MIN + 1);
        boolean desviada = azar.nextInt(100) < probabilidadFallo;
        if (desviada) {
            Direccion[] lados = Direccion.values();
            objetivo = objetivo.siguiente(lados[azar.nextInt(lados.length)]);
        }

        Zombie z = zombieEn(objetivo);
        String resultado;
        if (z != null) {
            z.morir();                      // su HiloZombie termina solo (revisa isVivo)
            sn.sumarPuntos(PUNTOS_ZOMBIE);
            resultado = desviada ? "La bala se desvió... ¡pero le dio a otro zombie!" : "¡Zombie eliminado!";
        } else {
            resultado = desviada ? "La bala se desvió" : "No había ningún zombie ahí";
        }
        notificar();
        return resultado + " (balas: " + sn.getBalas() + ")";
    }

    // ================== Lo que hacen los hilos de los zombies ==================

    public synchronized void moverZombie(Zombie z) {
        if (!estado.equals(JUGANDO) || !z.isVivo() || !zombies.contains(z)) return;
        z.mover(mapa);
        revisarTarjetas();
        revisarDetecciones();
        revisarFinNivel();
        notificar();
    }

    // ================== Reglas ==================

    // Si el rescatador pisó la tarjeta de un compañero caído, lo revive
    private void revisarRescates(Superviviente rescatador) {
        revisarTarjetas();
        for (Jugador j : jugadores) {
            if (j != rescatador && j.puedeMoverse()) {
                Superviviente caido = (Superviviente) j;
                if (caido.isTarjetaActiva() && caido.getPosicion().esIgual(rescatador.getPosicion())) {
                    caido.revivir();
                    rescatador.sumarPuntos(PUNTOS_RESCATE);
                }
            }
        }
    }

    // Quita las tarjetas de respawn que ya vencieron
    private void revisarTarjetas() {
        for (Jugador j : jugadores) {
            if (j.puedeMoverse()) {
                ((Superviviente) j).revisarVencimientoTarjeta();
            }
        }
    }

    private void revisarDetecciones() {
        for (Zombie z : zombies) {
            for (Jugador j : jugadores) {
                if (j.isVivo() && j.puedeMoverse() && z.detecta((Superviviente) j, mapa)) {
                    j.morir();              // queda como espectador
                }
            }
        }
    }

    // El nivel termina cuando cada superviviente escapó o murió.
    // Todos escaparon -> siguiente nivel. Alguno murió -> pierden todos.
    private void revisarFinNivel() {
        if (!estado.equals(JUGANDO) || contarSupervivientes() == 0) return;

        boolean algunoMurio = false;
        for (Jugador j : jugadores) {
            if (j.puedeMoverse()) {
                Superviviente s = (Superviviente) j;
                if (s.isVivo() && !s.isEscapo()) return;   // todavía hay alguien jugando
                if (!s.isVivo()) algunoMurio = true;
            }
        }

        detenerZombies();
        segundosFinales = calcularSegundos();
        if (algunoMurio) {
            estado = DERROTA;
        } else if (nivelActual == niveles.size() - 1) {
            estado = VICTORIA;
        } else {
            estado = NIVEL_SUPERADO;
            programarSiguienteNivel();
        }
    }

    // Espera unos segundos (para que vean "Nivel superado") y arranca el siguiente
    private void programarSiguienteNivel() {
        final int siguiente = nivelActual + 1;
        Thread espera = new Thread(() -> {
            try {
                Thread.sleep(PAUSA_ENTRE_NIVELES);
            } catch (InterruptedException ex) {
                return;
            }
            continuarConNivel(siguiente);
        });
        espera.start();
    }

    // Solo arranca el siguiente nivel si nadie terminó la partida durante la pausa
    private synchronized void continuarConNivel(int siguiente) {
        if (estado.equals(NIVEL_SUPERADO) && nivelActual == siguiente - 1) {
            iniciarNivel(siguiente);
        }
    }

    private void detenerZombies() {
        for (HiloZombie h : hilos) {
            h.detener();
        }
        hilos.clear();
        zombies.clear();
    }

    // Para cerrar el servidor
    public synchronized void terminar() {
        detenerZombies();
        estado = ESPERANDO;
    }

    // ================== Auxiliares ==================

    private Zombie zombieEn(Posicion p) {
        for (Zombie z : zombies) {
            if (z.isVivo() && z.getPosicion().esIgual(p)) return z;
        }
        return null;
    }

    private int contarSupervivientes() {
        int cantidad = 0;
        for (Jugador j : jugadores) {
            if (j.puedeMoverse()) cantidad++;
        }
        return cantidad;
    }

    private int calcularSegundos() {
        return (int) ((System.currentTimeMillis() - inicioNivel) / 1000);
    }

    private void notificar() {
        if (escucha != null) {
            escucha.partidaCambio();
        }
    }

    // ================== Getters (devuelven copias para que nadie modifique las listas de afuera) ==================

    public synchronized Mapa getMapa() { return mapa; }
    public synchronized String getEstado() { return estado; }
    public synchronized int getNivel() { return nivelActual + 1; }
    public synchronized int getTotalNiveles() { return niveles.size(); }
    public synchronized ArrayList<Jugador> getJugadores() { return new ArrayList<>(jugadores); }
    public synchronized ArrayList<Zombie> getZombies() { return new ArrayList<>(zombies); }

    public synchronized int getSegundos() {
        return estado.equals(JUGANDO) ? calcularSegundos() : segundosFinales;
    }

    // Jugadores ordenados de más a menos puntos (ordenamiento por selección)
    public synchronized ArrayList<Jugador> getRanking() {
        ArrayList<Jugador> ranking = new ArrayList<>(jugadores);
        for (int i = 0; i < ranking.size() - 1; i++) {
            int mayor = i;
            for (int k = i + 1; k < ranking.size(); k++) {
                if (ranking.get(k).getPuntos() > ranking.get(mayor).getPuntos()) mayor = k;
            }
            Jugador aux = ranking.get(i);
            ranking.set(i, ranking.get(mayor));
            ranking.set(mayor, aux);
        }
        return ranking;
    }
}
