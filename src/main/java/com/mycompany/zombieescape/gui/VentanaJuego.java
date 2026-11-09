package com.mycompany.zombieescape.gui;

import com.mycompany.zombieescape.modelo.Direccion;
import com.mycompany.zombieescape.modelo.Jugador;
import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.Posicion;
import com.mycompany.zombieescape.modelo.Sniper;
import com.mycompany.zombieescape.modelo.Superviviente;
import com.mycompany.zombieescape.modelo.Zombie;
import com.mycompany.zombieescape.red.Cliente.Cliente;
import com.mycompany.zombieescape.red.Cliente.EscuchaCliente;
import com.mycompany.zombieescape.red.Models.EstadoJuego;
import java.awt.event.ActionEvent;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Ventana donde se juega. Recibe las "fotos" del servidor (EstadoJuego) y las dibuja.
 * No decide nada del juego: solo muestra y manda lo que el jugador pide.
 *
 * @author dylnr
 */
public class VentanaJuego extends javax.swing.JFrame implements EscuchaCliente {

    private Cliente cliente;
    private String miNombre;
    private String miRol;
    private HiloMovimiento hiloMovimiento;
    private boolean espacioPresionado;    // para que dejar presionado el espacio no alterne sin parar
    private int nivelMostrado = -1;       // para ajustar la ventana solo cuando cambia el mapa
    private boolean apuntando;            // sniper: true = mira de 3x3, false = ve todo el mapa

    public VentanaJuego(Cliente cliente, String nombre, String rol) {
        initComponents();
        this.cliente = cliente;
        this.miNombre = nombre;
        this.miRol = rol;

        setTitle("Zombi Escape - " + nombre);
        lblJugador.setText(nombre + "  ·  " + (esSniper() ? "Sniper" : "Superviviente"));
        scrollMapa.getViewport().setBackground(java.awt.Color.BLACK);
        panelMapa.setPreferredSize(new java.awt.Dimension(640, 384));   // tamaño mientras no hay mapa
        pack();
        setLocationRelativeTo(null);

        // Me suscribo ANTES de conectar, para no perder los primeros mensajes
        cliente.setEscucha(this);

        lblBalas.setVisible(esSniper());          // solo el sniper tiene balas
        lblCoordenadas.setVisible(esSniper());    // y necesita saber dónde está apuntando
        if (esSniper()) {
            // El sniper se maneja solo con el mouse
            lblControles.setText("<html>Clic derecho: apuntar / dejar de apuntar<br>Clic izquierdo: disparar ("
                    + Sniper.BALAS_POR_ESCENARIO + " balas por nivel)</html>");
            configurarMouseSniper();
        } else {
            hiloMovimiento = new HiloMovimiento(cliente);
            hiloMovimiento.start();
            configurarTeclas();
        }

        // Si cambia de ventana con la flecha presionada, Swing nunca avisaría que la soltó
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                if (hiloMovimiento != null) hiloMovimiento.soltarTodo();
                espacioPresionado = false;
            }
        });
    }

    private boolean esSniper() {
        return "SNIPER".equals(miRol);
    }

    // ================== Teclado (Key Bindings) ==================

    // Key Bindings en vez de KeyListener: funcionan sin importar qué componente tenga el foco
    private void configurarTeclas() {
        InputMap teclas = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acciones = getRootPane().getActionMap();

        enlazarFlecha(teclas, acciones, KeyEvent.VK_UP, Direccion.ARRIBA);
        enlazarFlecha(teclas, acciones, KeyEvent.VK_DOWN, Direccion.ABAJO);
        enlazarFlecha(teclas, acciones, KeyEvent.VK_LEFT, Direccion.IZQUIERDA);
        enlazarFlecha(teclas, acciones, KeyEvent.VK_RIGHT, Direccion.DERECHA);

        configurarEspacio(teclas, acciones);

        // El JScrollPane del mapa usa las flechas para desplazarse; se las quitamos
        InputMap delScroll = scrollMapa.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        delScroll.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
        delScroll.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
        delScroll.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
        delScroll.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
    }

    // Espacio: entrar o salir del escondite (una vez por cada vez que se presiona)
    private void configurarEspacio(InputMap teclas, ActionMap acciones) {
        teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "esconder");
        teclas.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), "soltarEspacio");
        acciones.put("esconder", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (escribiendo() || espacioPresionado) return;
                espacioPresionado = true;
                cliente.enviarEsconder();
            }
        });
        acciones.put("soltarEspacio", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                espacioPresionado = false;
            }
        });
    }

    // Presionar avisa al hilo qué flecha está abajo; soltar le avisa que ya no
    private void enlazarFlecha(InputMap teclas, ActionMap acciones, int tecla, Direccion d) {
        String presionar = "presionar_" + d.getNombre();
        String soltar = "soltar_" + d.getNombre();
        teclas.put(KeyStroke.getKeyStroke(tecla, 0, false), presionar);
        teclas.put(KeyStroke.getKeyStroke(tecla, 0, true), soltar);
        acciones.put(presionar, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!escribiendo()) hiloMovimiento.presionar(d);
            }
        });
        acciones.put(soltar, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hiloMovimiento.soltar(d);
            }
        });
    }

    // Si está escribiendo en el chat, las teclas son para el texto, no para el jugador
    private boolean escribiendo() {
        return getFocusOwner() instanceof JTextComponent && ((JTextComponent) getFocusOwner()).isEditable();
    }

    // ================== Mouse del sniper ==================

    // Clic derecho: entra en modo apuntar (mira 3x3 donde hizo clic) o sale de él.
    // Clic izquierdo: dispara a la cruz roja, solo mientras está apuntando.
    private void configurarMouseSniper() {
        panelMapa.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Posicion celda = panelMapa.celdaEn(e.getX(), e.getY());
                if (celda == null) return;
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (apuntando) {
                        dejarDeApuntar();
                    } else {
                        apuntando = true;
                        cliente.enviarApuntado(celda.getFila(), celda.getColumna());
                        panelMapa.setVision(celda, Sniper.RADIOVISION);   // se ve ya; la foto del servidor lo confirma
                    }
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!apuntando) {
                        lblAviso.setText("Primero apunte con clic derecho");
                        return;
                    }
                    // El servidor revisa si está dentro de la mira, si tiene balas y el margen de error
                    cliente.enviarDisparo(celda.getFila(), celda.getColumna());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Posicion celda = panelMapa.celdaEn(e.getX(), e.getY());
                panelMapa.setApuntada(celda);   // la cruz roja (solo se ve dentro de la mira)
                lblCoordenadas.setText(celda == null ? "Mouse: -"
                        : "Mouse: fila " + celda.getFila() + ", columna " + celda.getColumna());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panelMapa.setApuntada(null);
                lblCoordenadas.setText("Mouse: -");
            }
        };
        panelMapa.addMouseListener(mouse);          // clics
        panelMapa.addMouseMotionListener(mouse);    // movimiento (para la mira)
    }

    private void dejarDeApuntar() {
        apuntando = false;
        panelMapa.setVision(null, 0);   // vuelve a ver todo el mapa
    }

    // ================== Lo que llega del servidor ==================

    // Lo llama ThreadCliente (otro hilo). Swing solo se debe tocar desde su propio hilo,
    // por eso se pasa el trabajo con invokeLater.
    @Override
    public void mostrarEstado(EstadoJuego estado) {
        SwingUtilities.invokeLater(() -> actualizar(estado));
    }

    @Override
    public void mostrarMensaje(String texto) {
        SwingUtilities.invokeLater(() -> lblAviso.setText("<html>" + escaparHtml(texto) + "</html>"));
    }

    private void actualizar(EstadoJuego estado) {
        Mapa mapa = estado.getMapa();
        if (mapa == null) {
            lblEstado.setText("Esperando inicio...");
            lblNivel.setText("Nivel: -");
            txaJugadores.setText(textoJugadores(estado));
            return;
        }

        // Nivel nuevo: el mapa cambia de tamaño y la ventana se ajusta
        if (estado.getNivel() != nivelMostrado) {
            nivelMostrado = estado.getNivel();
            dejarDeApuntar();               // en un nivel nuevo empieza viendo todo
            panelMapa.setMapa(mapa);
            pack();
            setLocationRelativeTo(null);
        }
        panelMapa.setFiguras(crearFiguras(estado));
        if (esSniper()) {
            actualizarSniper(estado);
        }

        lblNivel.setText("Nivel " + estado.getNivel() + "/" + estado.getTotalNiveles() + ": " + mapa.getNombre());
        lblTiempo.setText("Tiempo: " + formatoTiempo(estado.getSegundos()));
        lblEstado.setText(textoEstado(estado.getEstado()));
        txaJugadores.setText(textoJugadores(estado));
    }

    // Mientras apunta, la mira se centra donde el servidor la tiene; las balas salen de la foto
    private void actualizarSniper(EstadoJuego estado) {
        Jugador yo = estado.buscarJugador(miNombre);
        if (yo == null || yo.getPosicion() == null || yo.puedeMoverse()) return;
        Sniper sniper = (Sniper) yo;
        if (apuntando) {
            panelMapa.setVision(sniper.getPosicion(), Sniper.RADIOVISION);
        }
        lblBalas.setText("Balas: " + sniper.getBalas() + "/" + Sniper.BALAS_POR_ESCENARIO
                + (apuntando ? "  (apuntando)" : ""));
    }

    // Convierte la foto del servidor en cosas que PanelMapa sabe dibujar
    private ArrayList<Figura> crearFiguras(EstadoJuego estado) {
        ArrayList<Figura> figuras = new ArrayList<>();
        for (Zombie z : estado.getZombies()) {
            if (z.isVivo()) {
                figuras.add(new Figura(z.getImagen(), z.getPosicion().getFila(),
                        z.getPosicion().getColumna(), null, false));
            }
        }
        for (Jugador j : estado.getJugadores()) {
            if (!j.puedeMoverse()) continue;   // el sniper no está dentro de la ciudad: no se dibuja
            String imagen = j.getImagen();
            if (imagen == null || j.getPosicion() == null) continue;   // tarjeta vencida o sin colocar
            boolean escondido = false;
            if (j.puedeMoverse()) {
                Superviviente s = (Superviviente) j;
                if (s.isEscapo()) continue;                     // ya salió del mapa
                escondido = s.isEscondido();
            }
            String etiqueta = j.getNombre().equals(miNombre) ? "Tú" : j.getNombre();
            figuras.add(new Figura(imagen, j.getPosicion().getFila(),
                    j.getPosicion().getColumna(), etiqueta, escondido));
        }
        return figuras;
    }

    // ================== Textos del panel ==================

    private String textoJugadores(EstadoJuego estado) {
        String texto = "";
        for (Jugador j : estado.getRanking()) {
            String marca = j.getNombre().equals(miNombre) ? "> " : "  ";
            texto += marca + j.getNombre() + "  " + j.getPuntos() + " pts  " + situacion(j) + "\n";
        }
        return texto;
    }

    private String situacion(Jugador j) {
        if (!j.puedeMoverse()) return "(sniper)";
        Superviviente s = (Superviviente) j;
        if (s.isEscapo()) return "(escapó)";
        if (!s.isVivo()) return s.isTarjetaActiva() ? "(caído)" : "(muerto)";
        if (s.isEscondido()) return "(escondido)";
        return "";
    }

    private String textoEstado(String estado) {
        switch (estado) {
            case "JUGANDO":        return "¡A escapar!";
            case "NIVEL_SUPERADO": return "¡Nivel superado!";
            case "DERROTA":        return "Derrota";
            case "VICTORIA":       return "¡Victoria!";
            default:               return "Esperando inicio...";
        }
    }

    private String formatoTiempo(int segundos) {
        int s = segundos % 60;
        return (segundos / 60) + ":" + (s < 10 ? "0" : "") + s;
    }

    private String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollMapa = new javax.swing.JScrollPane();
        panelMapa = new com.mycompany.zombieescape.gui.PanelMapa();
        panelInfo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblJugador = new javax.swing.JLabel();
        lblNivel = new javax.swing.JLabel();
        lblTiempo = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        lblBalas = new javax.swing.JLabel();
        lblCoordenadas = new javax.swing.JLabel();
        lblJugadoresTitulo = new javax.swing.JLabel();
        scrollJugadores = new javax.swing.JScrollPane();
        txaJugadores = new javax.swing.JTextArea();
        lblAviso = new javax.swing.JLabel();
        lblControles = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Zombi Escape");

        scrollMapa.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        panelMapa.setLayout(new java.awt.BorderLayout());
        scrollMapa.setViewportView(panelMapa);

        getContentPane().add(scrollMapa, java.awt.BorderLayout.CENTER);

        panelInfo.setBackground(new java.awt.Color(24, 24, 27));
        panelInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panelInfo.setPreferredSize(new java.awt.Dimension(260, 400));
        panelInfo.setLayout(new javax.swing.BoxLayout(panelInfo, javax.swing.BoxLayout.PAGE_AXIS));

        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(120, 200, 80));
        lblTitulo.setText("ZOMBI ESCAPE");
        lblTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panelInfo.add(lblTitulo);

        lblJugador.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblJugador.setForeground(new java.awt.Color(228, 228, 231));
        lblJugador.setText("Jugador");
        lblJugador.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panelInfo.add(lblJugador);

        lblNivel.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblNivel.setForeground(new java.awt.Color(228, 228, 231));
        lblNivel.setText("Nivel: -");
        lblNivel.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 0, 0, 0));
        panelInfo.add(lblNivel);

        lblTiempo.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblTiempo.setForeground(new java.awt.Color(228, 228, 231));
        lblTiempo.setText("Tiempo: 0:00");
        lblTiempo.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));
        panelInfo.add(lblTiempo);

        lblEstado.setFont(new java.awt.Font("SansSerif", 1, 15)); // NOI18N
        lblEstado.setForeground(new java.awt.Color(120, 200, 80));
        lblEstado.setText("Conectando...");
        lblEstado.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 0, 0));
        panelInfo.add(lblEstado);

        lblBalas.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        lblBalas.setForeground(new java.awt.Color(230, 180, 80));
        lblBalas.setText("Balas: -");
        lblBalas.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        panelInfo.add(lblBalas);

        lblCoordenadas.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblCoordenadas.setForeground(new java.awt.Color(228, 228, 231));
        lblCoordenadas.setText("Mouse: -");
        lblCoordenadas.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));
        panelInfo.add(lblCoordenadas);

        lblJugadoresTitulo.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblJugadoresTitulo.setForeground(new java.awt.Color(150, 150, 160));
        lblJugadoresTitulo.setText("JUGADORES");
        lblJugadoresTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 0, 4, 0));
        panelInfo.add(lblJugadoresTitulo);

        scrollJugadores.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(40, 40, 45)));
        scrollJugadores.setAlignmentX(0.0F);

        txaJugadores.setEditable(false);
        txaJugadores.setBackground(new java.awt.Color(40, 40, 45));
        txaJugadores.setColumns(18);
        txaJugadores.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txaJugadores.setForeground(new java.awt.Color(228, 228, 231));
        txaJugadores.setRows(10);
        txaJugadores.setFocusable(false);
        scrollJugadores.setViewportView(txaJugadores);

        panelInfo.add(scrollJugadores);

        lblAviso.setFont(new java.awt.Font("SansSerif", 2, 12)); // NOI18N
        lblAviso.setForeground(new java.awt.Color(150, 150, 160));
        lblAviso.setText(" ");
        lblAviso.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        panelInfo.add(lblAviso);

        lblControles.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblControles.setForeground(new java.awt.Color(150, 150, 160));
        lblControles.setText("Flechas: moverse  |  Espacio: esconderse");
        lblControles.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        panelInfo.add(lblControles);

        getContentPane().add(panelInfo, java.awt.BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblAviso;
    private javax.swing.JLabel lblBalas;
    private javax.swing.JLabel lblCoordenadas;
    private javax.swing.JLabel lblControles;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblJugador;
    private javax.swing.JLabel lblJugadoresTitulo;
    private javax.swing.JLabel lblNivel;
    private javax.swing.JLabel lblTiempo;
    private javax.swing.JLabel lblTitulo;
    private com.mycompany.zombieescape.gui.PanelMapa panelMapa;
    private javax.swing.JPanel panelInfo;
    private javax.swing.JScrollPane scrollJugadores;
    private javax.swing.JTextArea txaJugadores;
    private javax.swing.JScrollPane scrollMapa;
    // End of variables declaration//GEN-END:variables
}
