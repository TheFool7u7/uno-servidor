package com.uno.server.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Partida implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Baraja baraja;
    private List<Jugador> jugadores;
    private int jugadorActual;
    private boolean sentidoHorario;
    private Carta.Color colorActual;
    private boolean juegoContinua;
    private boolean esperandoColor; // Para cuando se juega una carta de cambio de color
    
    public Partida() {
        baraja = new Baraja();
        jugadores = new ArrayList<>();
        jugadorActual = 0;
        sentidoHorario = true;
        juegoContinua = true;
        esperandoColor = false;
    }
    
    public void agregarJugador(Jugador jugador) {
        jugadores.add(jugador);
    }
    
    public void iniciarJuego() {
        // Repartir 7 cartas a cada jugador
        for (Jugador jugador : jugadores) {
            for (int i = 0; i < 7; i++) {
                jugador.agregarCarta(baraja.robar());
            }
        }
        
        // Colocar primera carta en el descarte
        Carta primeraCarta;
        do {
            primeraCarta = baraja.robar();
            // Si la primera carta es un comodín, volver a robar
        } while (primeraCarta.getColor() == Carta.Color.COMODIN);
        
        baraja.descartar(primeraCarta);
        colorActual = primeraCarta.getColor();
        
        // Aplicar el efecto de la primera carta si es una carta especial
        if (primeraCarta.getTipo() != Carta.Tipo.NUMERO) {
            aplicarEfectoCartaEspecial(primeraCarta);
        }
    }
    
    public boolean jugarCarta(int indiceJugador, int indiceCarta, Carta.Color colorElegido) {
        if (indiceJugador != jugadorActual || !juegoContinua) {
            return false;
        }
        
        Jugador jugadorActualObj = jugadores.get(jugadorActual);
        if (indiceCarta < 0 || indiceCarta >= jugadorActualObj.getMano().size()) {
            return false;
        }
        
        Carta cartaJugada = jugadorActualObj.getMano().get(indiceCarta);
        Carta ultimaCarta = baraja.verUltimaCarta();
        
        // Verificar si la carta puede ser jugada
        if (esperandoColor) {
            // Si estamos esperando un color, solo podemos jugar una carta del color elegido
            if (cartaJugada.getColor() != colorActual && cartaJugada.getColor() != Carta.Color.COMODIN) {
                return false;
            }
            esperandoColor = false;
        } else if (!cartaJugada.puedeJugarseEncima(ultimaCarta)) {
            return false;
        }
        
        // La carta es válida, jugarla
        jugadorActualObj.removerCarta(indiceCarta);
        baraja.descartar(cartaJugada);
        
        // Actualizar el color actual si es necesario
        if (cartaJugada.getColor() == Carta.Color.COMODIN) {
            colorActual = colorElegido;
            if (cartaJugada.getTipo() == Carta.Tipo.MAS_CUATRO) {
                // Hacer que el siguiente jugador robe 4 cartas
                int siguienteJugador = obtenerSiguienteJugador();
                for (int i = 0; i < 4; i++) {
                    jugadores.get(siguienteJugador).agregarCarta(baraja.robar());
                }
                
                // Saltar el turno del siguiente jugador
                pasarAlSiguienteJugador();
            }
        } else {
            colorActual = cartaJugada.getColor();
            aplicarEfectoCartaEspecial(cartaJugada);
        }
        
        // Verificar si el jugador ha ganado
        if (jugadorActualObj.haGanado()) {
            juegoContinua = false;
            return true;
        }
        
        // Verificar UNO
        if (jugadorActualObj.tieneUNO() && !jugadorActualObj.haGritadoUNO()) {
            // El jugador no gritó UNO, debe robar 2 cartas de penalización
            jugadorActualObj.agregarCarta(baraja.robar());
            jugadorActualObj.agregarCarta(baraja.robar());
        }
        
        // Pasar al siguiente jugador si no se ha pasado ya por algún efecto especial
        if (cartaJugada.getTipo() != Carta.Tipo.SALTO && 
            !(cartaJugada.getTipo() == Carta.Tipo.MAS_CUATRO)) {
            pasarAlSiguienteJugador();
        }
        
        return true;
    }
    
    private void aplicarEfectoCartaEspecial(Carta carta) {
        switch (carta.getTipo()) {
            case SALTO:
                // Saltar al siguiente jugador
                pasarAlSiguienteJugador();
                break;
            case REVERSA:
                // Cambiar dirección del juego
                sentidoHorario = !sentidoHorario;
                // En un juego de 2 jugadores, el reverso actúa como un salto
                if (jugadores.size() == 2) {
                    pasarAlSiguienteJugador();
                }
                break;
            case MAS_DOS:
                // Hacer que el siguiente jugador robe 2 cartas
                int siguienteJugador = obtenerSiguienteJugador();
                for (int i = 0; i < 2; i++) {
                    jugadores.get(siguienteJugador).agregarCarta(baraja.robar());
                }
                // Saltar el turno del siguiente jugador
                pasarAlSiguienteJugador();
                break;
            default:
                // No hacer nada para otras cartas especiales
                break;
        }
    }
    
    public boolean robarCarta(int indiceJugador) {
        if (indiceJugador != jugadorActual || !juegoContinua) {
            return false;
        }
        
        Jugador jugadorActualObj = jugadores.get(jugadorActual);
        Carta cartaRobada = baraja.robar();
        jugadorActualObj.agregarCarta(cartaRobada);
        
        // Si la carta robada puede ser jugada, el jugador tiene la opción de jugarla inmediatamente
        if (cartaRobada.puedeJugarseEncima(baraja.verUltimaCarta())) {
            return true; // Retornar true para indicar que puede jugar la carta robada
        } else {
            // Si no puede jugar la carta, pasa al siguiente jugador
            pasarAlSiguienteJugador();
            return false;
        }
    }
    
    public void gritarUNO(int indiceJugador) {
        if (indiceJugador >= 0 && indiceJugador < jugadores.size()) {
            jugadores.get(indiceJugador).gritarUNO();
        }
    }
    
    public void pasarAlSiguienteJugador() {
        if (sentidoHorario) {
            jugadorActual = (jugadorActual + 1) % jugadores.size();
        } else {
            jugadorActual = (jugadorActual - 1 + jugadores.size()) % jugadores.size();
        }
    }
    
    private int obtenerSiguienteJugador() {
        if (sentidoHorario) {
            return (jugadorActual + 1) % jugadores.size();
        } else {
            return (jugadorActual - 1 + jugadores.size()) % jugadores.size();
        }
    }
    
    public Baraja getBaraja() {
        return baraja;
    }
    
    public List<Jugador> getJugadores() {
        return jugadores;
    }
    
    public int getJugadorActual() {
        return jugadorActual;
    }
    
    public boolean isSentidoHorario() {
        return sentidoHorario;
    }
    
    public Carta.Color getColorActual() {
        return colorActual;
    }
    
    public void setColorActual(Carta.Color colorActual) {
        this.colorActual = colorActual;
        this.esperandoColor = false;
    }
    
    public boolean isEsperandoColor() {
        return esperandoColor;
    }
    
    public void setEsperandoColor(boolean esperandoColor) {
        this.esperandoColor = esperandoColor;
    }
    
    public boolean isJuegoContinua() {
        return juegoContinua;
    }
}