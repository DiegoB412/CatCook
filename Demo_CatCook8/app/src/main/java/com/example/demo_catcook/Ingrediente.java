package com.example.demo_catcook;

//Clase de apoyo para la clase IngredienteAdapter2 y addIngredienteActivity
public class Ingrediente {
    private int id_ingrediente;
    private String nombre;
    private String tipo;
    private String urlImagen;

    //Constructor para inicializar la clase
    public Ingrediente(int id, String nomb, String url){
        this.id_ingrediente= id;
        this.nombre= nomb;
        this.urlImagen= url;
    }

    //Getters y setters para obtener los valores de la clase
    public int getIdIngrediente() {return id_ingrediente;}
    public String getNombre() {return nombre;}
    public String getUrlImagen() {return urlImagen;}
}
