/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.zombieescape.logica;

import com.mycompany.zombieescape.modelo.Mapa;
import com.mycompany.zombieescape.modelo.TipoCelda;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

// Lee los archivos de escenario (.txt) y los convierte en objetos Mapa
public class CargadorMapas {

    public static final String CARPETA = "Escenarios";

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

        return mapa;
    }

    // Carga todos los .txt de la carpeta, en orden: nivel01, nivel02, ...
    public static ArrayList<Mapa> cargarTodos(String carpeta) throws IOException {
        File[] archivos = new File(carpeta).listFiles();
        if (archivos == null) {
            throw new IOException("No existe la carpeta: " + carpeta);
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