package com.example.demo_catcook;

import android.os.Parcel;
import android.os.Parcelable;
//Clase para llenar los item del historial de los ingredientes gastados
public class HistorialItem implements Parcelable {
    private String nombreIngrediente;
    private double cantidadCambio;
    private String fecha;
    private String descripcion;
    private boolean isHeader;

    //constructor que recibe la fecha para los encabezados
    public HistorialItem(String fecha) {
        this.fecha = fecha;
        this.isHeader = true; // Indica que es un encabezado
    }

    // Constructor que recibe los datos de los ingredientes
    public HistorialItem(String nombreIngrediente, double cantidadCambio, String fecha, String descripcion) {
        this.nombreIngrediente = nombreIngrediente;
        this.cantidadCambio = cantidadCambio;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.isHeader = false;
    }
    // Métodos Getter de los datos de los ingredientes del historial
    public String getNombreIngrediente() {return nombreIngrediente;}
    public double getCantidadCambio() {return cantidadCambio;}
    public String getFecha() {return fecha;}
    public String getDescripcion() {return descripcion;}
    public boolean isHeader() {return isHeader;}

    protected HistorialItem(Parcel in) {
        nombreIngrediente = in.readString();
        cantidadCambio = in.readDouble();
        fecha = in.readString();
        descripcion = in.readString();
    }
    //Creacion del parcelable
    public static final Creator<HistorialItem> CREATOR = new Creator<HistorialItem>() {
        @Override
        public HistorialItem createFromParcel(Parcel in) {
            return new HistorialItem(in);
        }

        @Override
        public HistorialItem[] newArray(int size) {
            return new HistorialItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    //Definicion de como se deben escribir los datos del historial
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nombreIngrediente);
        dest.writeDouble(cantidadCambio);
        dest.writeString(fecha);
        dest.writeString(descripcion);
    }
    //Se envian los datos del historial si es encabezado o no
    public void setHeader(boolean header) {
        isHeader = header;
    }
}

