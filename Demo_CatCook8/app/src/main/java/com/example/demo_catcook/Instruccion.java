package com.example.demo_catcook;

// Clase para representar una instrucción en una receta
public class Instruccion {
    private int paso;
    private String texto;
    private String imagenUrl;

    //Se crea el constructor de la clase
    public Instruccion(int paso, String texto, String imagenUrl) {
        this.paso = paso;
        this.texto = texto;
        this.imagenUrl = imagenUrl;
    }
    //Getters para obtener los valores de cada variable
    public int getPaso() {
        return paso;
    }
    public String getTexto() {
        return texto;
    }
    public String getImagenUrl() {
        return imagenUrl;
    }
}

