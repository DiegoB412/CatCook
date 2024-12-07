package com.example.demo_catcook;

// Clase para representar un ingrediente en una receta
public class IngredienteReceta {
    private String nombre;
    private double cantidad;
    private String unidadMedida;
    private String rutaImagen;

    // Constructor de la clase
    public IngredienteReceta(String nombre, double cantidad, String unidadMedida, String rutaImagen) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.rutaImagen = rutaImagen;
    }

    //getters para obtener los valores de cada variable
    public String getNombre() { return nombre; }
    public double getCantidad() { return cantidad; }
    public String getUnidadMedida() { return unidadMedida; }
    public String getRutaImagen() { return rutaImagen; }
}
