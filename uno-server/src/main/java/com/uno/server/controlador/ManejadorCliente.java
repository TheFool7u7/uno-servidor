package com.uno.server.controlador;

import com.uno.server.modelo.Carta;
import com.uno.server.modelo.Jugador;
import com.uno.server.modelo.Partida;
import com.uno.server.util.Mensaje;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private Socket socket;
    private Partida partida;
    private int idJugador;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;
    private boolean ejecutando;
    private ServidorUNO servidor;
    
    public ManejadorCliente(Socket socket, Partida partida, int idJugador, ServidorUNO servidor) {
        this.socket = socket;
        this.partida = partida;
        this.idJugador = idJugador;
        this.servidor = servidor;
        this.ejecutando = true;
        
        try {
            // Primero crear el stream de salida para evitar bloqueos
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error al crear los streams para el cliente: " + e.getMessage());
            ejecutando = false;
        }
    }
    
    @Override
    public void run() {
        try {
            while (ejecutando) {
                try {
                    Mensaje mensaje = (Mensaje) entrada.readObject();
                    procesarMensaje(mensaje);
                } catch (ClassNotFoundException e) {
                    System.err.println("Error al leer el mensaje: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error en la conexión con el cliente: " + e.getMessage());
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
            servidor.eliminarCliente(this);
        }
    }
    
    private void procesarMensaje(Mensaje mensaje) {
        switch (mensaje.getTipo()) {
            case CONEXION:
                // Crear un nuevo jugador y agregarlo a la partida
                Jugador nuevoJugador = new Jugador(mensaje.getContenido());
                partida.agregarJugador(nuevoJugador);
                System.out.println("Jugador conectado: " + mensaje.getContenido());
                break;
                
            case INICIAR_JUEGO:
                // Iniciar el juego si hay suficientes jugadores
                if (partida.getJugadores().size() >= 2) {
                    partida.iniciarJuego();
                    servidor.enviarEstadoActualizado();
                }
                break;
                
            case JUGAR_CARTA:
                // Procesar el intento de jugar una carta
                boolean cartaJugada = partida.jugarCarta(
                    mensaje.getJugadorId(), 
                    mensaje.getCartaId(), 
                    mensaje.getColorElegido()
                );
                
                if (cartaJugada) {
                    if (!partida.isJuegoContinua()) {
                        // El juego ha terminado, enviar mensaje de victoria
                        Jugador ganador = partida.getJugadores().get(mensaje.getJugadorId());
                        servidor.enviarMensajeATodos(new Mensaje(
                            Mensaje.Tipo.CHAT, 
                            "¡" + ganador.getNombre() + " ha ganado la partida!", 
                            "Servidor"
                        ));
                    }
                    
                    // Enviar el estado actualizado a todos los clientes
                    servidor.enviarEstadoActualizado();
                }
                break;
                
            case ROBAR_CARTA:
                // Procesar el intento de robar una carta
                boolean cartaRobada = partida.robarCarta(mensaje.getJugadorId());
                // Enviar el estado actualizado
                servidor.enviarEstadoActualizado();
                break;
                
            case GRITAR_UNO:
                // Procesar el grito de UNO
                partida.gritarUNO(mensaje.getJugadorId());
                // Enviar mensaje a todos los jugadores
                servidor.enviarMensajeATodos(new Mensaje(
                    Mensaje.Tipo.CHAT,
                    "¡" + partida.getJugadores().get(mensaje.getJugadorId()).getNombre() + " grita UNO!",
                    "Servidor"
                ));
                break;
                
            case ELEGIR_COLOR:
                // Cuando un jugador elije un color después de jugar un comodín
                partida.setColorActual(mensaje.getColorElegido());
                // Enviar el estado actualizado
                servidor.enviarEstadoActualizado();
                break;
                
            case CHAT:
                // Reenviar el mensaje de chat a todos los jugadores
                servidor.enviarMensajeATodos(mensaje);
                break;
                
            default:
                System.out.println("Mensaje no reconocido");
                break;
        }
    }
    
    public void enviarMensaje(Mensaje mensaje) {
        try {
            salida.writeObject(mensaje);
            salida.flush();
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje al cliente: " + e.getMessage());
            ejecutando = false;
        }
    }
    
    public int getIdJugador() {
        return idJugador;
    }
    
    public void detener() {
        ejecutando = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar el socket del cliente: " + e.getMessage());
        }
    }
}