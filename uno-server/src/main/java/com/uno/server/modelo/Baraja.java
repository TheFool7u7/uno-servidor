package com.uno.server.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baraja implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<Carta> cartas;
    private List<Carta> descarte;
    
    public Baraja() {
        cartas = new ArrayList<>();
        descarte = new ArrayList<>();
        inicializar();
    }
    
    private void inicializar() {
        // Crear cartas numéricas (0-9)
        for (Carta.Color color : new Carta.Color[] { Carta.Color.ROJO, Carta.Color.AZUL, 
                                                    Carta.Color.VERDE, Carta.Color.AMARILLO }) {
            // Una carta 0 de cada color
            cartas.add(new Carta(color, Carta.Tipo.NUMERO, 0));
            
            // Dos cartas de cada número del 1 al 9
            for (int i = 1; i <= 9; i++) {
                cartas.add(new Carta(color, Carta.Tipo.NUMERO, i));
                cartas.add(new Carta(color, Carta.Tipo.NUMERO, i));
            }
            
            // Dos cartas especiales de cada color
            for (int i = 0; i < 2; i++) {
                cartas.add(new Carta(color, Carta.Tipo.SALTO, -1));
                cartas.add(new Carta(color, Carta.Tipo.REVERSA, -1));
                cartas.add(new Carta(color, Carta.Tipo.MAS_DOS, -1));
            }
        }
        
        // Comodines
        for (int i = 0; i < 4; i++) {
            cartas.add(new Carta(Carta.Color.COMODIN, Carta.Tipo.CAMBIO_COLOR, -1));
            cartas.add(new Carta(Carta.Color.COMODIN, Carta.Tipo.MAS_CUATRO, -1));
        }
        
        barajar();
    }
    
    public void barajar() {
        Collections.shuffle(cartas);
    }
    
    public Carta robar() {
        if (cartas.isEmpty()) {
            // Si no hay cartas, mezclar el descarte y colocarlo como mazo
            if (descarte.isEmpty() || descarte.size() == 1) {
                // Si no hay suficientes cartas, reinicializar la baraja
                inicializar();
            } else {
                // Guardar la última carta jugada
                Carta ultimaCarta = descarte.remove(descarte.size() - 1);
                
                // Transferir cartas del descarte al mazo y barajar
                cartas.addAll(descarte);
                descarte.clear();
                descarte.add(ultimaCarta);
                barajar();
            }
        }
        
        return cartas.isEmpty() ? null : cartas.remove(0);
    }
    
    public void descartar(Carta carta) {
        descarte.add(carta);
    }
    
    public Carta verUltimaCarta() {
        if (descarte.isEmpty()) {
            return null;
        }
        return descarte.get(descarte.size() - 1);
    }
    
    public int cartasRestantes() {
        return cartas.size();
    }
}
