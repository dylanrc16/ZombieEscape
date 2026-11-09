package com.mycompany.zombieescape.modelo;

public class Superviviente extends Jugador implements IMovible {
    public static final int DURACION_TARJETA = 30000;   // ms que dura la tarjeta de respawn
    public static final int PROTECCION = 2000;          // ms invisible a zombies al revivir

    private Direccion direccion = Direccion.ABAJO;
    private Direccion movimientoPendiente;   // lo que pidió el usuario con las flechas
    private boolean escondido;
    private boolean escapo;
    private String skin = "jugador_gym";     // la ropa: jugador_gym, jugador_traje...
    private long momentoMuerte;              // cuándo murió (para que la tarjeta venza)
    private boolean tarjetaActiva;           // true mientras se le pueda rescatar
    private long protegidoHasta;             // hasta cuándo los zombies no lo ven

    public Superviviente(String nombre) {
        super(nombre);
    }

    @Override
    public Direccion getDireccion() {
        return direccion; 
    }
    public boolean isEscondido() {
        return escondido; 
    }
    public boolean isEscapo() {
        return escapo; 
    }
    public void setSkin(String skin) {
        this.skin = skin; 
    }
    public boolean isTarjetaActiva() {
        return tarjetaActiva; 
    }

    // Recién revivido: los zombies no lo ven durante unos segundos
    public boolean isProtegido() {
        return System.currentTimeMillis() < protegidoHasta;
    }

    // Al morir deja una tarjeta de respawn en el lugar donde cayó
    @Override
    public void morir() {
        super.morir();
        escondido = false;
        movimientoPendiente = null;
        momentoMuerte = System.currentTimeMillis();
        tarjetaActiva = true;
    }

    // Lo llama Partida para que la tarjeta desaparezca si nadie la recogió a tiempo
    public void revisarVencimientoTarjeta() {
        if (tarjetaActiva && System.currentTimeMillis() - momentoMuerte > DURACION_TARJETA) {
            tarjetaActiva = false;
        }
    }

    // Otro superviviente pisó su tarjeta: vuelve a jugar desde ahí, con unos segundos de protección
    @Override
    public void revivir() {
        super.revivir();
        tarjetaActiva = false;
        escondido = false;
        movimientoPendiente = null;
        protegidoHasta = System.currentTimeMillis() + PROTECCION;
    }

    // El servidor guarda hacia dónde quiere ir el usuario; mover() lo ejecuta
    public void pedirMovimiento(Direccion d) {
        movimientoPendiente = d;
    }

    @Override
    public void mover(Mapa mapa) {
        if (!vivo || escapo || movimientoPendiente == null) return;

        direccion = movimientoPendiente;
        movimientoPendiente = null;
        ultimoMovimiento = System.currentTimeMillis();

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
        vivo = true;
        tarjetaActiva = false;
        protegidoHasta = 0;
    }

    // Vivo: su ropa. Muerto: su tarjeta de respawn (o nada si ya venció)
    @Override
    public String getImagen() {
        if (vivo) return skin;
        return tarjetaActiva ? "tarjeta_respawn" : null;
    }

    @Override
    public boolean puedeMoverse() {
        return true;
    }
}
