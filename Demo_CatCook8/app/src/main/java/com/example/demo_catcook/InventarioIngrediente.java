package com.example.demo_catcook;
//Clase para guardar los valores referentes al inventario de los ingredientes
public class InventarioIngrediente {
    private int idIngrediente;
    private String nombre;
    private String tipo;
    private String rutaImagen;
    private double cantidadDisponible;
    private String unidadMedida;
    private int idUsuario;

    public InventarioIngrediente(){}

    //Constructor de la clase usado en FragmentInventario
    public InventarioIngrediente(int idIngrediente, String nombre, Double cantidad, String unidadM, String ruta, int idUsuario){
        this.idIngrediente= idIngrediente;
        this.nombre= nombre;
        this.cantidadDisponible= cantidad;
        this.unidadMedida= unidadM;
        this.rutaImagen= ruta;
        this.idUsuario= idUsuario;
    }
    //Getters y setters de la clase usados para obtener los valores de los ingredientes
    public int getIdIngrediente() {return idIngrediente;}
    public String getNombre() {return nombre;}
    public String getTipo() {return tipo;}
    public String getRutaImagen() {return rutaImagen;}
    public String getUnidadMedida() {return unidadMedida;}
    public double getCantidadDisponible() {return cantidadDisponible;}
    public void setCantidadDisponible(double cantidadDisponible) {this.cantidadDisponible = cantidadDisponible;}

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}
