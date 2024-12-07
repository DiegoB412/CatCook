package com.example.demo_catcook;

// Clase para representar una valoración
public class Valoracion {
    private float puntuacion;
    private String comentario;
    private String nombreUsuario;
    private String fotoUsuario;

    // Constructor de la clase
    public Valoracion(float puntuacion, String comentario, String nombreUsuario, String fotoUsuario) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.nombreUsuario = nombreUsuario;
        this.fotoUsuario = fotoUsuario;
    }

    // Getters y setters
    public float getPuntuacion() { return puntuacion; }
    public String getComentario() { return comentario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getFotoUsuario() { return fotoUsuario; }
}

