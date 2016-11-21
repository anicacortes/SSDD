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
    private int tMax = 10;
    private boolean fin = false;

    //para poner elemento y lo descarta si esta lleno --> offer
    //para sacar elemento --> take

    public MailBox(int p) {
		port = p;
        queue = new ArrayBlockingQueue<>(tMax);
    }
	
	public void run() { // Servidor secuencial
		try {
            socket = new ServerSocket(port);    //escucha en un puerto
            while(!fin){
                Socket s = socket.accept();
                ObjectInputStream input = new ObjectInputStream(s.getInputStream());
                Envelope e = (Envelope) input.readObject();
                if(((MessageValue)e.getPayload()).getValue().equals("Fin")){
                    fin=true;
                }else{
                    queue.offer(e);     //mete en mensaje si cabe en la cola, sino lo descarta
                }
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
     * Devuelve el primer mensaje de la cola. Si la cola eta vacia espera hasta que
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
