package com.uno.server.modelo;

import java.io.Serializable;

public class Carta implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Color {
        ROJO, AZUL, VERDE, AMARILLO, COMODIN
    }
    
    public enum Tipo {
        NUMERO, SALTO, REVERSA, MAS_DOS, CAMBIO_COLOR, MAS_CUATRO
    }
    
    private Color color;
    private Tipo tipo;
    private int numero; // -1 para cartas especiales
    
    public Carta(Color color, Tipo tipo, int numero) {
        this.color = color;
        this.tipo = tipo;
        this.numero = numero;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Tipo getTipo() {
        return tipo;
    }
    
    public int getNumero() {
        return numero;
    }
    
    public String getNombreArchivo() {
        if (tipo == Tipo.NUMERO) {
            return color.toString().toLowerCase() + "_" + numero + ".png";
        } else if (tipo == Tipo.CAMBIO_COLOR) {
            return "comodin_cambio_color.png";
        } else if (tipo == Tipo.MAS_CUATRO) {
            return "comodin_mas_cuatro.png";
        } else {
            return color.toString().toLowerCase() + "_" + tipo.toString().toLowerCase() + ".png";
        }
    }
    
    @Override
    public String toString() {
        if (tipo == Tipo.NUMERO) {
            return "Carta " + color + " número " + numero;
        } else {
            return "Carta " + color + " " + tipo;
        }
    }
    
    public boolean puedeJugarseEncima(Carta cartaAnterior) {
        // Comodines siempre se pueden jugar
        if (this.color == Color.COMODIN) {
            return true;
        }
        
        // Mismo color
        if (this.color == cartaAnterior.getColor()) {
            return true;
        }
        
        // Mismo número o tipo
        if (this.tipo == Tipo.NUMERO && cartaAnterior.getTipo() == Tipo.NUMERO) {
            return this.numero == cartaAnterior.getNumero();
        } else {
            return this.tipo == cartaAnterior.getTipo();
        }
    }
}
