/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Header
 */

package com.mycompany.imagenessinhilos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Programa para procesar múltiples imágenes de forma secuencial:
 * - Lee todas las imágenes (.jpg y .png) de la carpeta "imagenes".
 * - Convierte cada una a escala de grises usando el promedio de RGB.
 * - Guarda las imágenes procesadas en la carpeta "imagenes_grises_secuencial".
 * - Mide el tiempo total de ejecución.
 * 
 * Requisitos:
 * - Coloca la carpeta "imagenes" en el directorio raíz del proyecto (al mismo nivel que src).
 * - El programa creará automáticamente la carpeta "imagenes_grises_secuencial" si no existe.
 * 
 * Mejores prácticas implementadas:
 * - Uso de rutas relativas basadas en el directorio de trabajo del proyecto (user.dir).
 * - Filtrado de archivos por extensión (insensible a mayúsculas).
 * - Manejo de excepciones por imagen para que un error en una no detenga el procesamiento de las demás.
 * - Medición precisa de tiempo con nanoTime() para el proceso total.
 * - Conservación del nombre original del archivo en la salida (solo cambia la carpeta).
 * - Soporte para al menos 100 imágenes; el bucle es simple y eficiente para I/O secuencial.
 * - Recomendación: Usa imágenes de tamaño moderado (e.g., 1920x1080) para pruebas realistas; evita imágenes muy grandes (>10MB cada una) para no saturar memoria.
 * - Para optimización futura: Considera usar BufferedImage.TYPE_INT_RGB si las imágenes no tienen alpha, pero aquí se mantiene general.
 */
public class ImagenesSinHilos {

    public static void main(String[] args) {
        // Directorio base del proyecto
        String directorioProyecto = System.getProperty("user.dir");
        
        // Carpeta de entrada y salida
        File carpetaEntrada = new File(directorioProyecto + File.separator + "Imagenes");
        File carpetaSalida = new File(directorioProyecto + File.separator + "imagenes_grises_secuencial");
        
        // Verificar y crear carpeta de salida si no existe
        if (!carpetaSalida.exists()) {
            if (carpetaSalida.mkdirs()) {
                System.out.println("Carpeta de salida creada: " + carpetaSalida.getAbsolutePath());
            } else {
                System.out.println("Error al crear la carpeta de salida.");
                return;
            }
        }
        
        // Verificar que la carpeta de entrada existe y tiene archivos
        if (!carpetaEntrada.exists() || !carpetaEntrada.isDirectory()) {
            System.out.println("Error: La carpeta 'imagenes' no existe en " + carpetaEntrada.getAbsolutePath() + ". Colócala en el directorio raíz del proyecto.");
            return;
        }
        
        // Filtrar archivos de imagen (.jpg y .png, insensible a mayúsculas)
        File[] archivosImagenes = carpetaEntrada.listFiles((dir, nombre) -> {
            String lowerName = nombre.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".png");
        });
        
        if (archivosImagenes == null || archivosImagenes.length == 0) {
            System.out.println("No se encontraron imágenes (.jpg o .png) en la carpeta 'imagenes'. Agrega al menos 100 imágenes para las pruebas.");
            return;
        }
        
        System.out.println("Iniciando procesamiento secuencial de " + archivosImagenes.length + " imágenes.");
        
        long inicioTotal = System.nanoTime(); // Tiempo inicial total
        int imagenesProcesadas = 0;
        int errores = 0;
        
        // Procesamiento secuencial: bucle for-each sobre las imágenes
        for (File archivoEntrada : archivosImagenes) {
            try {
                // Cargar la imagen
                BufferedImage imagen = ImageIO.read(archivoEntrada);
                if (imagen == null) {
                    System.err.println("No se pudo cargar: " + archivoEntrada.getName());
                    errores++;
                    continue;
                }
                
                // Obtener dimensiones
                int ancho = imagen.getWidth();
                int alto = imagen.getHeight();
                
                // Procesar píxeles (bucle anidado secuencial)
                for (int y = 0; y < alto; y++) {
                    for (int x = 0; x < ancho; x++) {
                        // Obtener RGB
                        int pixel = imagen.getRGB(x, y);
                        
                        // Extraer componentes
                        int alpha = (pixel >> 24) & 0xff;
                        int red = (pixel >> 16) & 0xff;
                        int green = (pixel >> 8) & 0xff;
                        int blue = pixel & 0xff;
                        
                        // Calcular gris (promedio simple, como en el código original)
                        int gris = (red + green + blue) / 3;
                        
                        // Nuevo píxel en gris
                        int nuevoPixel = (alpha << 24) | (gris << 16) | (gris << 8) | gris;
                        
                        // Asignar
                        imagen.setRGB(x, y, nuevoPixel);
                    }
                }
                
                // Guardar en carpeta de salida con el mismo nombre
                String nombreSalida = carpetaSalida.getAbsolutePath() + File.separator + archivoEntrada.getName();
                // Detectar formato de salida basado en extensión de entrada
                String formato = nombreSalida.toLowerCase().endsWith(".png") ? "png" : "jpg";
                if (ImageIO.write(imagen, formato, new File(nombreSalida))) {
                    imagenesProcesadas++;
                    System.out.println("Procesada: " + archivoEntrada.getName() + " (" + ancho + "x" + alto + ")");
                } else {
                    System.err.println("Error al guardar: " + nombreSalida);
                    errores++;
                }
                
            } catch (IOException e) {
                System.err.println("Error procesando " + archivoEntrada.getName() + ": " + e.getMessage());
                errores++;
            }
        }
        
        long finTotal = System.nanoTime(); // Tiempo final total
        long tiempoTotalMs = (finTotal - inicioTotal) / 1_000_000;
        
        // Reporte final
        System.out.println("\n=== RESULTADOS SECUENCIAL ===");
        System.out.println("Imágenes procesadas: " + imagenesProcesadas);
        System.out.println("Errores: " + errores);
        System.out.println("Tiempo total de ejecución: " + tiempoTotalMs + " ms");
        System.out.println("Tiempo promedio por imagen: " + (tiempoTotalMs / (double) imagenesProcesadas) + " ms");
    }
}