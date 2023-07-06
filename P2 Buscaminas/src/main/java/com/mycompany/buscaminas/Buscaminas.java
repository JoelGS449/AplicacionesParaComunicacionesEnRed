package com.mycompany.buscaminas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


/**
 *
 * @author usuario
 */
public class Buscaminas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
  
        Scanner sc = new Scanner(System.in);
        int n,m,p;

        System.out.println("¿Que nivel desea jugar?\n1.Fácil\n2.Intermedio\n3.Avanzado");
        n = sc.nextInt();
        Tablero tab = Tiro(-1, -1, n);
        
        System.out.println(tab);
        
        while (tab.getEstadoJuego() == 0) {
            System.out.println("Presiona \n1) para volver a tirar.\n2)Para marcar ");
            n = sc.nextInt()-1;
            System.out.println("Ingresa las coordenadas");
            m=sc.nextInt()-1;
            p=sc.nextInt()-1;
            tab = Tiro(m, p, n);
            System.out.println(tab);
        }
        if (tab.getEstadoJuego() == 1) {
            System.out.println("Ganaste");
        } else {
            System.out.println("Perdiste");
        }

    }

    public static Tablero Tiro(int x, int y, int jug) {
        Tablero t=null;
        try {
            DatagramSocket socket = new DatagramSocket();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            
            Jugada j1 = new Jugada();

            j1.setTipo(jug);
            j1.setX(x);
            j1.setY(y);
            oos.writeObject(j1);
            
            byte[]d = baos.toByteArray();
            InetAddress dir = InetAddress.getByName("127.0.0.1");
            int port = 1234;
            DatagramPacket p = new DatagramPacket(d,d.length,dir,port);
            socket.send(p);
            
            System.out.println("objeto enviado");
            byte[] tRecibe = new byte[6535];
            DatagramPacket p1 = new DatagramPacket(tRecibe,tRecibe.length);
            socket.receive(p1);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(p1.getData()));
            
            t = (Tablero) ois.readObject();
            
            
        } catch (UnknownHostException ex) {
        } catch (IOException | ClassNotFoundException ex) {
        }
        return t;
    }
}
