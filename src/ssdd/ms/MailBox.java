/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: MailBox.java
* TIEMPO: 1h
* DESCRIPCIÓN: Se encarga de recibir los mensajes que llegan de otros procesos
*           y almacenarlos en una lista con acceso en exclusión mutua si el
*           número me mensajes almacenados es menor de 10.
*/
package ssdd.ms;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MailBox extends Thread {

    private int port;
    private ServerSocket socket;
    private BlockingQueue<Envelope> queue; // Lista de mensajes bloqueante del proceso
    private final int MAX = 10;
    private boolean fin = false;
    private int localLamportClock;
    private int tLamportClock;

    public MailBox(int p) {
		port = p;
        queue = new ArrayBlockingQueue<>(MAX);
    }
	
	public void run() {             // Servidor secuencial
		try {
            socket = new ServerSocket(port);    //escucha en un puerto
            while(!fin){
                Socket s = socket.accept();
                ObjectInputStream input = new ObjectInputStream(s.getInputStream());
                Envelope e = (Envelope) input.readObject();
                //Modificar el reloj lamport local
                /*tLamportClock = e.getLamportClock();
                localLamportClock = MessageSystem.getLamportClock();
                if(localLamportClock >= tLamportClock){
                    localLamportClock++;
                }else{
                    localLamportClock = tLamportClock+1;
                }*/
                //MessageSystem.setLamportClock(localLamportClock);
               // MessageValue m = (MessageValue)e.getPayload();
                //System.out.println("LLEGA al BUZON mesaje "+((MessageValue)e.getPayload()).getValue()+" de "+e.getSource()+" con valor " +e.getLamportClock()+ " el LC es "+MessageSystem.getLamportClock());
                /*if(m.getValue().equals("Fin")){
                    fin=true;
                }else{
                    queue.offer(e);     //mete en mensaje si cabe en la cola, sino lo descarta
                }*/
                queue.offer(e);
                input.close();
                s.close();
            }
        } catch (SocketException e) {
			System.err.println("Cerrando buzón.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
	}

    /**
     * Devuelve el primer mensaje de la cola. Si la cola esta vacia espera hasta que
     * tiene un mensaje para devolver
     */
    public Envelope getNextMessage() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

	
}
