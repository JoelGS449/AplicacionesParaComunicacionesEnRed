/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.buscaminas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *

 */
public class Servidor {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Tablero juego = null;
        
        try {
            int pto=1234;
            DatagramSocket socket = new DatagramSocket(pto);
            System.out.println("Servidor esperando juego...");
            
            while (true) {
                System.out.println("Cliente conectado");
                byte[] buffer = new byte[6025];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
                Jugada j = (Jugada) ois.readObject();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);

                if (j != null) {
                    if (j.getX() < 0 && j.getY() < 0) {
                        System.out.println("Nuevo juego");
                        juego = new Tablero(j.getTipo());
                        oos.writeObject(juego);
                        System.out.println(juego);
                        
                        byte[]regresa = baos.toByteArray();    
                        InetAddress dir = packet.getAddress();
                        int port = packet.getPort();
                        DatagramPacket p3 = new DatagramPacket(regresa,regresa.length,dir,port);
                        socket.send(p3);
                       
                    } else {
                        juego.hazJugada(j.getX(), j.getY(), j.getTipo());
                        if (juego.getEstadoJuego() == -1) {
                            System.out.println("Perdiste.");
                            System.out.println(juego);
                        }
                        oos.writeObject(juego);
                        System.out.println(juego);
                        byte[]regresa = baos.toByteArray();
                        InetAddress dir = packet.getAddress();
                        int port = packet.getPort();
                        DatagramPacket p3 = new DatagramPacket(regresa,regresa.length,dir,port);
                        socket.send(p3);
                    }
                }
            }
        } catch (ClassNotFoundException ce) {
        } catch (IOException ex) {
        }
    }
}