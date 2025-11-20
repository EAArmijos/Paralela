/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.imagenes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Runnable para procesar una sola imagen a escala de grises (secuencial dentro del hilo).
 * - Carga la imagen desde archivoEntrada.
 * - Convierte píxeles a gris (preservando alpha).
 * - Guarda en carpetaSalida con el mismo nombre.
 * 
 * Mejores prácticas:
 * - Manejo de IOException para no crash el hilo.
 * - Formato de salida basado en extensión de entrada.
 * - Logging simple por hilo.
 */
public class FiltroGris implements Runnable {
    private final File archivoEntrada;
    private final File carpetaSalida;
    
    public FiltroGris(File archivoEntrada, File carpetaSalida) {
        this.archivoEntrada = archivoEntrada;
        this.carpetaSalida = carpetaSalida;
    }
    
    @Override
    public void run() {
        try {
            // Cargar la imagen
            BufferedImage imagen = ImageIO.read(archivoEntrada);
            if (imagen == null) {
                System.err.println("No se pudo cargar: " + archivoEntrada.getName());
                return;
            }
            
            int ancho = imagen.getWidth();
            int alto = imagen.getHeight();
            
            System.out.println("Hilo procesando: " + archivoEntrada.getName() + " (" + ancho + "x" + alto + ")");
            
            // Procesar píxeles secuencialmente dentro del hilo (bucle anidado)
            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {
                    int pixel = imagen.getRGB(x, y);
                    
                    // Extraer componentes (incluyendo alpha)
                    int alpha = (pixel >> 24) & 0xff;
                    int rojo = (pixel >> 16) & 0xff;
                    int verde = (pixel >> 8) & 0xff;
                    int azul = pixel & 0xff;
                    
                    // Calcular gris (promedio simple)
                    int gris = (rojo + verde + azul) / 3;
                    
                    // Nuevo píxel con alpha preservado
                    int nuevoPixel = (alpha << 24) | (gris << 16) | (gris << 8) | gris;
                    
                    imagen.setRGB(x, y, nuevoPixel);
                }
            }
            
            // Guardar en carpeta de salida con el mismo nombre
            String nombreSalida = carpetaSalida.getAbsolutePath() + File.separator + archivoEntrada.getName();
            String formato = nombreSalida.toLowerCase().endsWith(".png") ? "png" : "jpg";
            if (ImageIO.write(imagen, formato, new File(nombreSalida))) {
                System.out.println("Guardada: " + archivoEntrada.getName());
            } else {
                System.err.println("Error al guardar: " + nombreSalida);
            }
            
        } catch (IOException e) {
            System.err.println("Error procesando " + archivoEntrada.getName() + ": " + e.getMessage());
        }
    }
}