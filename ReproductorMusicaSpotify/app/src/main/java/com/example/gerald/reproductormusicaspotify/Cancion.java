package com.example.gerald.reproductormusicaspotify;

/**
 * Created by gerald on 14/04/18.
 */

public class Cancion {
    public String nombre;
    public int cantidadCanciones;
    public String uri;

    public Cancion(String pNombre,int pCantidadCanciones, String pUri) {
        this.nombre = pNombre;
        this.cantidadCanciones = pCantidadCanciones;
        this.uri = pUri;
    }
}
