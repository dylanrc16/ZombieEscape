package com.mycompany.zombieescape;

import java.io.File;

/**
 * Encuentra las carpetas Img y Escenarios aunque el juego no se ejecute desde la
 * carpeta del proyecto (por ejemplo, con doble clic en el .jar).
 *
 * Busca en este orden:
 *  1. donde se ejecuta el programa (en NetBeans es la raíz del proyecto),
 *  2. al lado del .jar (o de la carpeta de clases compiladas),
 *  3. hasta dos carpetas más arriba (si el .jar está en target/, ahí está la raíz).
 *
 * @author dylnr
 */
public class Rutas {

    public static File carpeta(String nombre) {
        File aqui = new File(nombre);
        if (aqui.isDirectory()) return aqui;

        File base = ubicacionDelPrograma();
        for (int nivel = 0; base != null && nivel < 3; nivel++) {
            File candidata = new File(base, nombre);
            if (candidata.isDirectory()) return candidata;
            base = base.getParentFile();
        }
        return aqui;   // no está en ningún lado: quien la use mostrará el error con este nombre
    }

    public static File archivo(String carpeta, String archivo) {
        return new File(carpeta(carpeta), archivo);
    }

    // La carpeta donde está el .jar (o target/classes si se ejecuta desde las clases compiladas)
    private static File ubicacionDelPrograma() {
        try {
            File programa = new File(Rutas.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return programa.isFile() ? programa.getParentFile() : programa;
        } catch (Exception ex) {
            return null;
        }
    }
}
