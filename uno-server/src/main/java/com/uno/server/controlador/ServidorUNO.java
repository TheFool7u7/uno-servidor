package com.uno.server.controlador;

import com.uno.server.modelo.Partida;
import com.uno.server.util.Mensaje;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServidorUNO {
    private ServerSocket serverSocket;
    private boolean ejecutando;
    private List<ManejadorCliente> clientes;
    private Partida partida;
    private final int PUERTO = 5000;
    
    public ServidorUNO() {
        clientes = new ArrayList<>();
        partida = new Partida();
        ejecutando = false;
    }
    
    public void iniciar() {
        try {
            serverSocket = new ServerSocket(PUERTO);
            ejecutando = true;
            System.out.println("Servidor UNO iniciado en el puerto " + PUERTO);
            
            // Hilo para aceptar conexiones
            new Thread(() -> {
                aceptarConexiones();
            }).start();
            
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
    
    private void aceptarConexiones() {
        try {
            while (ejecutando) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada: " + clientSocket.getInetAddress());
                
                // Crear un manejador para este cliente
                int idJugador = clientes.size();
                ManejadorCliente manejador = new ManejadorCliente(clientSocket, partida, idJugador, this);
                clientes.add(manejador);
                
                // Iniciar el hilo del manejador
                new Thread(manejador).start();
                
                // Enviar mensaje de bienvenida
                manejador.enviarMensaje(new Mensaje(
                    Mensaje.Tipo.CHAT, 
                    "Bienvenido al servidor UNO. Hay " + clientes.size() + " jugadores conectados.", 
                    "Servidor"
                ));
                
                // Notificar a todos los clientes sobre el nuevo jugador
               enviarMensajeATodos(new Mensaje(
                    Mensaje.Tipo.CHAT, 
                    "Un nuevo jugador se ha unido. Total de jugadores: " + clientes.size(), 
                    "Servidor"
                ));
            }
        } catch (IOException e) {
            if (ejecutando) {
                System.err.println("Error al aceptar conexiones: " + e.getMessage());
            }
        }
    }
    
    public void enviarEstadoActualizado() {
        // Crear mensaje con el estado actual del juego
        Mensaje mensaje = new Mensaje(
            Mensaje.Tipo.ACTUALIZAR_ESTADO,
            partida.getJugadores(),
            partida.getBaraja().verUltimaCarta(),
            partida.getJugadorActual(),
            partida.getColorActual(),
            partida.isSentidoHorario()
        );
        
        // Enviar a todos los clientes
        enviarMensajeATodos(mensaje);
    }
    
    public void enviarMensajeATodos(Mensaje mensaje) {
        for (ManejadorCliente cliente : clientes) {
            cliente.enviarMensaje(mensaje);
        }
    }
    
    public void eliminarCliente(ManejadorCliente cliente) {
        clientes.remove(cliente);
        System.out.println("Cliente desconectado. Clientes restantes: " + clientes.size());
        
        // Notificar a todos los clientes
        enviarMensajeATodos(new Mensaje(
            Mensaje.Tipo.CHAT, 
            "Un jugador se ha desconectado. Total de jugadores: " + clientes.size(), 
            "Servidor"
        ));
    }
    
    public void detener() {
        ejecutando = false;
        
        // Cerrar todos los clientes
        for (ManejadorCliente cliente : clientes) {
            cliente.detener();
        }
        clientes.clear();
        
        // Cerrar el servidor
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar el servidor: " + e.getMessage());
        }
        
        System.out.println("Servidor detenido");
    }
    
    public static void main(String[] args) {
        ServidorUNO servidor = new ServidorUNO();
        servidor.iniciar();
        
        // Agregar un hook de apagado para cerrar el servidor correctamente
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando servidor...");
            servidor.detener();
        }));
    }
} 