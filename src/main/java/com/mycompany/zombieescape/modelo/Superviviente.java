package com.mycompany.zombieescape.modelo;

public class Superviviente extends Jugador implements IMovible {
    private Direccion direccion = Direccion.ABAJO;
    private Direccion movimientoPendiente;   // lo que pidió el usuario con las flechas
    private boolean escondido;
    private boolean escapo;
    private String skin = "jugador_gym";     // la ropa: jugador_gym, jugador_traje...

    public Superviviente(String nombre) {
        super(nombre);
    }

    @Override
    public Direccion getDireccion() { return direccion; }
    public boolean isEscondido() { return escondido; }
    public boolean isEscapo() { return escapo; }
    public void setSkin(String skin) { this.skin = skin; }

    // El servidor guarda hacia dónde quiere ir el usuario; mover() lo ejecuta
    public void pedirMovimiento(Direccion d) {
        movimientoPendiente = d;
    }

    @Override
    public void mover(Mapa mapa) {
        if (!vivo || escapo || movimientoPendiente == null) return;

        direccion = movimientoPendiente;
        movimientoPendiente = null;

        Posicion destino = posicion.siguiente(direccion);
        if (mapa.esCaminable(destino)) {
            posicion = destino;
            escondido = false;               // al moverse sale del escondite
            if (mapa.esSalida(destino)) {
                escapo = true;
            }
        }
    }

    // Solo puede esconderse parado sobre una celda H. Si ya estaba escondido, sale.
    public void alternarEscondite(Mapa mapa) {
        if (!vivo || escapo) return;
        if (escondido) {
            escondido = false;
        } else if (mapa.esEscondite(posicion)) {
            escondido = true;
        }
    }

    // Para empezar un nivel nuevo
    public void reiniciar(Posicion inicio) {
        posicion = inicio;
        direccion = Direccion.ABAJO;
        movimientoPendiente = null;
        escondido = false;
        escapo = false;
    }

    @Override
    public String getImagen() { return skin; }

    @Override
    public boolean puedeMoverse() { return true; }
}
