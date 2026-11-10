package com.mycompany.zombieescape.gui;

import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Imágenes que usan varias ventanas (el logo, el ícono). Así cada ventana no repite
 * el código de buscar el archivo en la carpeta Img.
 *
 * @author dylnr
 */
public class Recursos {
    private static final String CARPETA_IMG = "Img";

    // Img/<nombre>.png, o null si el archivo no está
    public static Image imagen(String nombre) {
        File archivo = new File(CARPETA_IMG, nombre + ".png");
        if (!archivo.exists()) {
            System.out.println("No se encontró la imagen: " + archivo.getPath());
            return null;
        }
        return new ImageIcon(archivo.getPath()).getImage();
    }

    // El logo DD como ícono de la ventana (en vez de la taza de café de Java)
    public static void ponerIcono(JFrame ventana) {
        Image icono = imagen("icono_dd");
        if (icono != null) {
            ventana.setIconImage(icono);
        }
    }
}
