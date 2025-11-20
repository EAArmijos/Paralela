/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Header
 */

package com.mycompany.imagenes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Programa para procesar múltiples imágenes de forma concurrente:
 * - Lee todas las imágenes (.jpg y .png) de la carpeta "imagenes".
 * - Crea un hilo por imagen: cada hilo carga, convierte a escala de grises (secuencial dentro del hilo),
 *   y guarda en "imagenes_grises_concurrente".
 * - Mide el tiempo total de ejecución (desde inicio hasta que todos los hilos terminen).
 * 
 * Requisitos:
 * - Coloca la carpeta "imagenes" en el directorio raíz del proyecto (al mismo nivel que src).
 * - El programa creará automáticamente la carpeta "imagenes_grises_concurrente" si no existe.
 * 
 * Mejores prácticas implementadas:
 * - Uso de rutas relativas basadas en el directorio de trabajo del proyecto (user.dir).
 * - Filtrado de archivos por extensión (insensible a mayúsculas).
 * - Manejo de excepciones por hilo para que un error en una imagen no detenga las demás.
 * - Medición precisa de tiempo con nanoTime() para el proceso total.
 * - Conservación del nombre original del archivo en la salida (solo cambia la carpeta).
 * - Soporte para al menos 100 imágenes; un hilo por imagen (no divide la imagen internamente, ya que el lab pide "un hilo para cada imagen").
 * - Recomendación: Limita hilos si > cores CPU (e.g., Runtime.getRuntime().availableProcessors()) para evitar thrashing, pero aquí usa todos para demo.
 * - Para optimización: El procesamiento píxel es CPU-bound; con 100 hilos, espera overhead en creación/join, pero speedup si multi-core.
 * - Nota: Se preserva alpha en gris (corregido del código original). Usa ExecutorService en producción, pero raw Thread para el lab.
 */
public class Imagenes {

    public static void main(String[] args) {
        // Directorio base del proyecto
        String directorioProyecto = System.getProperty("user.dir");
        
        // Carpeta de entrada y salida
        File carpetaEntrada = new File(directorioProyecto + File.separator + "Imagenes");
        File carpetaSalida = new File(directorioProyecto + File.separator + "imagenes_grises_concurrente");
        
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
        
        System.out.println("Iniciando procesamiento concurrente de " + archivosImagenes.length + " imágenes (1 hilo por imagen).");
        
        long inicioTotal = System.nanoTime(); // Tiempo inicial total
        Thread[] hilos = new Thread[archivosImagenes.length];
        int imagenesExitosas = 0;
        
        // Crear y asignar hilos: uno por imagen
        for (int i = 0; i < archivosImagenes.length; i++) {
            File archivoActual = archivosImagenes[i];
            hilos[i] = new Thread(new FiltroGris(archivoActual, carpetaSalida));
            hilos[i].start();
        }
        
        // Esperar a que todos los hilos terminen (cumple 3.b y 3.c)
        try {
            for (Thread hilo : hilos) {
                hilo.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupción en join: " + e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }
        
        long finTotal = System.nanoTime(); // Tiempo final total
        long tiempoTotalMs = (finTotal - inicioTotal) / 1_000_000;
        
        // Reporte final (simplificado; en FiltroGris se maneja el conteo por hilo)
        System.out.println("\n=== RESULTADOS CONCURRENTE ===");
        System.out.println("Todas las imágenes han terminado de procesar.");
        System.out.println("Tiempo total de ejecución: " + tiempoTotalMs + " ms");
        // Nota: Para conteo preciso de exitosas, usa AtomicInteger compartido, pero aquí asumimos todas OK para simplicidad.
    }
}