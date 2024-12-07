package com.example.demo_catcook;

// Clase que representa una receta personal guardando sus atributos
public class RecetaPersonal {
    private int idReceta;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private String ingredientes;
    private String tiempoPreparacion;
    private String instrucciones;
    private String dificultad;
    private String url;

    // Constructor de la clase para recibir los datos de cada atributo
    public RecetaPersonal(int idReceta, String nombre, String instrucciones, String descripcion,
                          String imagenUrl, String ingredientes, String tiempoPreparacion,
                          String dificultad, String url) {
        this.idReceta = idReceta;
        this.nombre = nombre;
        this.instrucciones= instrucciones;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.ingredientes = ingredientes;
        this.tiempoPreparacion = tiempoPreparacion;
        this.dificultad = dificultad;
        this.url = url;
    }
    // Getters y setters para cada atributo
    public int getIdReceta() {return idReceta;}
    public String getNombre() {return nombre;}
    public String getDescripcion() {return descripcion;}
    public String getImagenUrl() {return imagenUrl;}
    public String getIngredientes() {return ingredientes;}
    public String getTiempoPreparacion() {return tiempoPreparacion;}
    public String getDificultad() {return dificultad;}
    public String getUrl() {return url;}
    public String getInstrucciones() {return instrucciones;}

    public void setIngredientes(String ingredientes) {this.ingredientes = ingredientes;}
    public void setDificultad(String dificultad) {this.dificultad = dificultad;}
    public void setInstrucciones(String instrucciones) {this.instrucciones = instrucciones;}

}

