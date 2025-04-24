package com.uno.server.util;

import java.io.Serializable;
import java.util.List;
import com.uno.server.modelo.Carta;
import com.uno.server.modelo.Jugador;

public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Tipo {
        CONEXION,
        INICIAR_JUEGO,
        ACTUALIZAR_ESTADO,
        JUGAR_CARTA,
        ROBAR_CARTA,
        GRITAR_UNO,
        ELEGIR_COLOR,
        CHAT
    }
    
    private Tipo tipo;
    private String contenido;
    private int jugadorId;
    private int cartaId;
    private Carta.Color colorElegido;
    private List<Jugador> jugadores;
    private Carta ultimaCarta;
    private int jugadorActual;
    private Carta.Color colorActual;
    private boolean sentidoHorario;
    
    // Constructor para mensajes simples
    public Mensaje(Tipo tipo) {
        this.tipo = tipo;
    }
    
    // Constructor para mensajes de conexión
    public Mensaje(Tipo tipo, String nombreJugador) {
        this.tipo = tipo;
        this.contenido = nombreJugador;
    }
    
    // Constructor para mensajes de jugar carta
    public Mensaje(Tipo tipo, int jugadorId, int cartaId) {
        this.tipo = tipo;
        this.jugadorId = jugadorId;
        this.cartaId = cartaId;
    }
    
    // Constructor para mensajes de elegir color
    public Mensaje(Tipo tipo, Carta.Color colorElegido) {
        this.tipo = tipo;
        this.colorElegido = colorElegido;
    }
    
    // Constructor para mensajes de actualización de estado
    public Mensaje(Tipo tipo, List<Jugador> jugadores, Carta ultimaCarta, 
                  int jugadorActual, Carta.Color colorActual, boolean sentidoHorario) {
        this.tipo = tipo;
        this.jugadores = jugadores;
        this.ultimaCarta = ultimaCarta;
        this.jugadorActual = jugadorActual;
        this.colorActual = colorActual;
        this.sentidoHorario = sentidoHorario;
    }
    
    // Constructor para mensajes de chat
    public Mensaje(Tipo tipo, String mensaje, String remitente) {
        this.tipo = tipo;
        this.contenido = remitente + ": " + mensaje;
    }
    
    // Getters y setters
    public Tipo getTipo() {
        return tipo;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public int getJugadorId() {
        return jugadorId;
    }
    
    public int getCartaId() {
        return cartaId;
    }
    
    public Carta.Color getColorElegido() {
        return colorElegido;
    }
    
    public List<Jugador> getJugadores() {
        return jugadores;
    }
    
    public Carta getUltimaCarta() {
        return ultimaCarta;
    }
    
    public int getJugadorActual() {
        return jugadorActual;
    }
    
    public Carta.Color getColorActual() {
        return colorActual;
    }
    
    public boolean isSentidoHorario() {
        return sentidoHorario;
    }
}