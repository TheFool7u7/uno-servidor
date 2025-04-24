package com.uno.server.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Jugador implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String nombre;
    private List<Carta> mano;
    private boolean haGritadoUNO;
    
    public Jugador(String nombre) {
        this.nombre = nombre;
        this.mano = new ArrayList<>();
        this.haGritadoUNO = false;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public List<Carta> getMano() {
        return mano;
    }
    
    public void agregarCarta(Carta carta) {
        mano.add(carta);
        haGritadoUNO = false; // Reset cuando se agrega una carta
    }
    
    public void removerCarta(int indice) {
        if (indice >= 0 && indice < mano.size()) {
            mano.remove(indice);
        }
    }
    
    public int cantidadCartas() {
        return mano.size();
    }
    
    public boolean haGritadoUNO() {
        return haGritadoUNO;
    }
    
    public void gritarUNO() {
        haGritadoUNO = true;
    }
    
    public void resetGritoUNO() {
        haGritadoUNO = false;
    }
    
    public boolean tieneUNO() {
        return mano.size() == 1;
    }
    
    public boolean haGanado() {
        return mano.isEmpty();
    }
}