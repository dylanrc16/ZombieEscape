/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.zombieescape.logica;

import com.mycompany.zombieescape.Rutas;
import com.mycompany.zombieescape.modelo.Direccion;
import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.Posicion;
import com.mycompany.zombieescape.modelo.TipoCelda;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

// Lee los archivos de escenario (.txt) y los convierte en objetos Mapa
public class CargadorMapas {

    public static final String CARPETA = "Escenarios";
    private static final String SIMBOLOS = "#.HBES";   // los únicos que entiende el juego

    // Carga un solo escenario a partir de la ruta del archivo
    public static Mapa cargar(String ruta) throws IOException {
        String nombre = "Sin nombre";
        int deteccion = 3;
        int zombies = 3;
        ArrayList<String> filas = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (linea.contains("=")) {
                    // Línea de configuración: CLAVE=valor
                    String[] partes = linea.split("=", 2);
                    String clave = partes[0].trim().toUpperCase();
                    String valor = partes[1].trim();

                    if (clave.equals("NOMBRE")) {
                        nombre = valor;
                    } else if (clave.equals("DETECCION")) {
                        deteccion = Integer.parseInt(valor);
                    } else if (clave.equals("ZOMBIES")) {
                        zombies = Integer.parseInt(valor);
                    }
                } else {
                    // Línea del mapa
                    filas.add(linea);
                }
            }
        } catch (NumberFormatException ex) {
            throw new IOException("En " + ruta + ", DETECCION y ZOMBIES deben ser números");
        }
        if (deteccion < 1) {
            throw new IOException("En " + ruta + ", DETECCION debe ser 1 o más");
        }
        if (zombies < 0) {
            throw new IOException("En " + ruta + ", ZOMBIES no puede ser negativo");
        }

        if (filas.isEmpty()) {
            throw new IOException("El escenario no tiene mapa: " + ruta);
        }

        // Pasar la lista de filas a la matriz
        int columnas = filas.get(0).length();
        char[][] matriz = new char[filas.size()][columnas];
        for (int f = 0; f < filas.size(); f++) {
            String fila = filas.get(f);
            if (fila.length() != columnas) {
                throw new IOException("En " + ruta + ", la fila " + (f + 1) + " tiene "
                        + fila.length() + " columnas y debería tener " + columnas);
            }
            // un símbolo desconocido se dibujaría como calle y el error no se notaría
            for (int c = 0; c < columnas; c++) {
                if (SIMBOLOS.indexOf(fila.charAt(c)) < 0) {
                    throw new IOException("En " + ruta + ", la fila " + (f + 1) + " tiene el símbolo desconocido '"
                            + fila.charAt(c) + "' en la columna " + (c + 1) + ". Use solo # . H B E S");
                }
            }
            matriz[f] = fila.toCharArray();
        }

        Mapa mapa = new Mapa(nombre, deteccion, zombies, matriz);

        // Un nivel sin entrada o sin salida no se puede jugar
        if (mapa.buscarCeldas(TipoCelda.ENTRADA).isEmpty()) {
            throw new IOException("El escenario " + ruta + " no tiene entrada (E)");
        }
        if (mapa.buscarCeldas(TipoCelda.SALIDA).isEmpty()) {
            throw new IOException("El escenario " + ruta + " no tiene salida (S)");
        }
        for (Posicion entrada : mapa.buscarCeldas(TipoCelda.ENTRADA)) {
            if (!hayCamino(mapa, entrada)) {
                throw new IOException("En " + ruta + ", la entrada de la fila " + (entrada.getFila() + 1)
                        + ", columna " + (entrada.getColumna() + 1) + " no tiene camino a ninguna salida");
            }
        }

        return mapa;
    }

    // ¿Se puede llegar caminando desde esa entrada hasta alguna salida?
    // Búsqueda por anchura (BFS): se avanza en "ondas" desde la entrada por las celdas
    // caminables, marcando las visitadas para no repetirlas.
    private static boolean hayCamino(Mapa mapa, Posicion inicio) {
        boolean[][] visitada = new boolean[mapa.getFilas()][mapa.getColumnas()];
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        pendientes.add(inicio);
        visitada[inicio.getFila()][inicio.getColumna()] = true;
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.poll();
            if (mapa.esSalida(actual)) return true;
            for (Direccion d : Direccion.values()) {
                Posicion vecina = actual.siguiente(d);
                if (mapa.esCaminable(vecina) && !visitada[vecina.getFila()][vecina.getColumna()]) {
                    visitada[vecina.getFila()][vecina.getColumna()] = true;
                    pendientes.add(vecina);
                }
            }
        }
        return false;
    }

    // Carga todos los .txt de la carpeta, en orden: nivel01, nivel02, ...
    public static ArrayList<Mapa> cargarTodos(String carpeta) throws IOException {
        File directorio = Rutas.carpeta(carpeta);   // la busca también al lado del .jar
        File[] archivos = directorio.listFiles();
        if (archivos == null) {
            throw new IOException("No existe la carpeta: " + directorio.getAbsolutePath());
        }
        Arrays.sort(archivos);

        ArrayList<Mapa> mapas = new ArrayList<>();
        for (File archivo : archivos) {
            if (archivo.getName().endsWith(".txt")) {
                mapas.add(cargar(archivo.getPath()));
            }
        }
        return mapas;
    }
}
