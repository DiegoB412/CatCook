package com.example.demo_catcook;

//Dentro de esta clase se crea la estructura de la receta
public class Receta {
    private int id;
    private String nombre;
    private String descripcion;
    private String instrucciones;
    private String tiempoPreparacion;
    private String dificultad;
    private String urlImagen;
    private String ingredientes;
    private boolean esPersonal;
    private int idCategoria;

    //Constructor para crear una receta
    public Receta(int id, String nombre, String dificultad, String descripcion, String rutaImagen, int idCategoria) {
        this.id = id;
        this.nombre = nombre;
        this.dificultad= dificultad;
        this.descripcion= descripcion;
        this.urlImagen= rutaImagen;
        this.idCategoria= idCategoria;
    }
    //Constructor para crear una receta personal
    public Receta( String nombre, String dificultad, String descripcion, String rutaImagen,
                   String instrucciones, String tiempoPreparacion, String ingredientes) {
        this.nombre = nombre;
        this.dificultad= dificultad;
        this.descripcion= descripcion;
        this.urlImagen= rutaImagen;
        this.instrucciones= instrucciones;
        this.tiempoPreparacion= tiempoPreparacion;
        this.ingredientes= ingredientes;
    }

    public Receta() {
    }

    //Getters y setters de los atributos
    public int getId() { return id; }
    public String getUrlImagen(){return urlImagen;}
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getDificultad() { return dificultad; }
    public String getTiempoPreparacion() {
        return tiempoPreparacion;
    }
    public boolean isEsPersonal() {
        return esPersonal;
    }
    public String getIngredientes() {
        return ingredientes;
    }
    public void setEsPersonal(boolean esPersonal) {
        this.esPersonal = esPersonal;
    }
    public String getInstrucciones() {
        return instrucciones;
    }
    public int getIdCategoria() {
        return idCategoria;
    }
    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
    }
    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }


}
