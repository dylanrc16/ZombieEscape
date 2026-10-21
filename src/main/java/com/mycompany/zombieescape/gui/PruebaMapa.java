package com.mycompany.zombieescape.gui;

import com.mycompany.zombieescape.logica.CargadorMapas;
import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.Posicion;
import com.mycompany.zombieescape.modelo.TipoCelda;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Ventana de PRUEBA: muestra los escenarios con jugadores y zombies de ejemplo.
 * No es parte del juego final; sirve para ver que el mapa y las imágenes funcionan.
 *
 * @author dylnr
 */
public class PruebaMapa extends JFrame {
    private static final String[] SKINS = {
        "jugador_gym", "jugador_traje", "jugador_futbol", "jugador_ingeniero", "jugador_medico"
    };
    private static final String[] NOMBRES = { "Dylan", "Duvan", "Ana", "Luis", "Sofi" };
    private static final String[] DIRECCIONES = { "arriba", "abajo", "izquierda", "derecha" };

    private PanelMapa panelMapa = new PanelMapa();
    private ArrayList<Mapa> mapas;

    public PruebaMapa(ArrayList<Mapa> mapas) {
        super("Zombi Escape - Prueba de mapas");
        this.mapas = mapas;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Selector de nivel arriba
        JComboBox<String> selector = new JComboBox<>();
        for (int i = 0; i < mapas.size(); i++) {
            selector.addItem("Nivel " + (i + 1) + ": " + mapas.get(i).getNombre());
        }
        selector.addActionListener(e -> mostrarNivel(selector.getSelectedIndex()));

        JPanel barra = new JPanel();
        barra.setBackground(new Color(24, 24, 27));
        JLabel etiqueta = new JLabel("Escenario:");
        etiqueta.setForeground(Color.WHITE);
        barra.add(etiqueta);
        barra.add(selector);

        add(barra, BorderLayout.NORTH);
        add(panelMapa, BorderLayout.CENTER);
        mostrarNivel(0);
        setLocationRelativeTo(null);
    }

    private void mostrarNivel(int indice) {
        Mapa mapa = mapas.get(indice);
        panelMapa.setMapa(mapa);
        panelMapa.setFiguras(figurasDeEjemplo(mapa));
        pack();   // ajusta la ventana al tamaño del mapa
    }

    // Jugadores en las entradas y zombies en calles al azar, solo para ver cómo se ve
    private ArrayList<Figura> figurasDeEjemplo(Mapa mapa) {
        ArrayList<Figura> figuras = new ArrayList<>();
        Random azar = new Random(mapa.getNombre().length());

        ArrayList<Posicion> entradas = mapa.buscarCeldas(TipoCelda.ENTRADA);
        for (int i = 0; i < SKINS.length && i < entradas.size(); i++) {
            Posicion p = entradas.get(i);
            figuras.add(new Figura(SKINS[i], p.getFila(), p.getColumna(), NOMBRES[i], false));
        }

        ArrayList<Posicion> calles = mapa.buscarCeldas(TipoCelda.CAMINO);
        for (int i = 0; i < mapa.getCantidadZombies(); i++) {
            Posicion p = calles.get(azar.nextInt(calles.size()));
            String dir = DIRECCIONES[azar.nextInt(DIRECCIONES.length)];
            figuras.add(new Figura("zombie_" + dir, p.getFila(), p.getColumna(), null, false));
        }

        // Un jugador escondido en el primer escondite, para ver la transparencia
        ArrayList<Posicion> escondites = mapa.buscarCeldas(TipoCelda.ESCONDITE);
        if (!escondites.isEmpty()) {
            Posicion p = escondites.get(0);
            figuras.add(new Figura("jugador_gym", p.getFila(), p.getColumna(), "Oculto", true));
        }
        return figuras;
    }

    public static void main(String[] args) {
        try {
            ArrayList<Mapa> mapas = CargadorMapas.cargarTodos(CargadorMapas.CARPETA);
            SwingUtilities.invokeLater(() -> new PruebaMapa(mapas).setVisible(true));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error al cargar escenarios: " + ex.getMessage());
        }
    }
}
