package com.aluracursos.literatura.principal;

import com.aluracursos.literatura.model.*;
import com.aluracursos.literatura.repository.AutorRepository;
import com.aluracursos.literatura.repository.LibroRepository;
import com.aluracursos.literatura.service.ConsumoAPI;
import com.aluracursos.literatura.service.ConvierteDatos;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Principal {

    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private List<Autor> autores;
    private List<Libro> libros;
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        try {
            while (opcion != 0) {
                var menu = """
                        1 - Busqueda de libro por título
                        2 - Lista de todos los libros buscados
                        3 - Lista de autores buscados
                        4 - Lista de autores vivos en determinado año
                        5 - Lista de libros por idioma
                                      
                        0 - Salir
                        """;
                System.out.println(menu);
                opcion = teclado.nextInt();
                teclado.nextLine();

                switch (opcion) {
                    case 1:
                        buscarLibrosPorTitulo();
                        break;
                    case 2:
                        mostrarLibrosBuscados();
                        break;
                    case 3:
                        mostrarAutoresBuscados();
                        break;
                    case 4:
                        mostrarAutoresVivos();
                        break;
                    case 5:
                        mostrarLibrosPorIdioma();
                        break;

                    case 0:
                        System.out.println("Cerrando la aplicación...");
                        break;
                    default:
                        System.out.println("Opción inválida");
                        break;
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Opción inválida, por favor vuelve a ejecutar el programa e ingresa un número válido");
        }
    }

    private Datos getDatosLibros() {
        System.out.println("Ingrese el nombre del libro que deseas buscar");
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        return datosBusqueda;
    }

    private Libro agregarLibroBD(DatosLibros datosLibros, Autor autor) {
        Libro libro = new Libro(datosLibros, autor);
        return libroRepository.save(libro);
    }

    private void buscarLibrosPorTitulo() {
        Datos datos = getDatosLibros();

        if (!datos.resultados().isEmpty()) {
            DatosLibros datosLibros = datos.resultados().get(0);
            DatosAutor datosAutor = datosLibros.autor().get(0);
            Libro libroBuscado = libroRepository.findByTituloIgnoreCase(datosLibros.titulo());

            if (libroBuscado != null) {
                System.out.println(libroBuscado);
                System.out.println("El libro ya existe en la base de datos, no se puede volver a registrar.");
            } else {
                Autor autorBuscado = autorRepository.findByNombreIgnoreCase(datosAutor.nombre());

                if (autorBuscado == null) {
                    Autor autor = new Autor(datosAutor);
                    autorRepository.save(autor);
                    Libro libro = agregarLibroBD(datosLibros, autor);
                    System.out.println(libro);
                } else {
                    Libro libro = agregarLibroBD(datosLibros, autorBuscado);
                    System.out.println(libro);
                }
            }
        } else {
            System.out.println("Libro no encontrado");
        }
    }

    private void mostrarLibrosBuscados() {
        libros = libroRepository.findAll();
        if (!libros.isEmpty()) {
            libros.stream().forEach(System.out::println);
        } else {
            System.out.println("No hay libros registrados");
        }
    }

    private void mostrarAutoresBuscados() {
        autores = autorRepository.findAll();
        if (!autores.isEmpty()) {
            autores.stream().forEach(System.out::println);
        } else {
            System.out.println("No hay autores registrados");
        }
    }

    private void mostrarAutoresVivos() {
        System.out.println("Ingrese el año en el que quiere saber que autores están vivos: ");
        String fecha = teclado.nextLine();
        try {
            List<Autor> autoresVivosEnCiertaFecha = autorRepository.autorVivoEnDeteminadoAnio(fecha);
            if (!autoresVivosEnCiertaFecha.isEmpty()) {
                autoresVivosEnCiertaFecha.stream().forEach(System.out::println);
            } else {
                System.out.println("No hay autores vivos en ese año especificado");
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private void mostrarLibrosPorIdioma() {
        System.out.println("""
                1. Español (ES)
                2. Inglés (EN)
                                
                3. Regresa al menú principal
                
                Ingresa el número del idioma de los libros que quieres consultar: 
                """);

        var opcion = teclado.nextInt();
        teclado.nextLine();
        switch (opcion) {
            case 1:
                libros = libroRepository.findByIdiomasContaining("es");
                if (!libros.isEmpty()) {
                    libros.stream().forEach(System.out::println);
                } else {
                    System.out.println("No hay libros registrados en español.");
                }
                break;

            case 2:
                libros = libroRepository.findByIdiomasContaining("en");
                if (!libros.isEmpty()) {
                    libros.stream().forEach(System.out::println);
                } else {
                    System.out.println("No hay libros registrados en inglés.");
                }
                break;

            case 3:
                muestraElMenu();
                break;

            default:
                System.out.println("La opción no es válida.");


        }

    }



}

