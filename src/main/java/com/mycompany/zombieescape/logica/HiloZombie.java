package com.mycompany.zombieescape.logica;

import com.mycompany.zombieescape.modelo.Zombie;

// Un hilo por zombie: cada cierto tiempo le pide a la partida que lo mueva
public class HiloZombie extends Thread {
    private Zombie zombie;
    private Partida partida;
    private volatile boolean activo = true;   // volatile: otro hilo lo cambia con detener()

    public HiloZombie(Zombie zombie, Partida partida) {
        this.zombie = zombie;
        this.partida = partida;
    }

    @Override
    public void run() {
        while (activo && zombie.isVivo()) {
            try {
                Thread.sleep(zombie.getVelocidad());   // más lento que los jugadores
            } catch (InterruptedException ex) {
                return;                                 // lo despertaron con detener()
            }
            if (activo) {
                partida.moverZombie(zombie);
            }
        }
    }

    // Para terminar el hilo (cambio de nivel o fin de partida)
    public void detener() {
        activo = false;
        interrupt();
    }
}
