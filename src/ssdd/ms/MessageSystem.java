/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: MessageSystem.java
* TIEMPO: 1h
* DESCRIPCIÓN: Se encarga del envio y recepcion de mensajes interactuando con
*           su propio buzon de mensajes, el cual lo crea al inicio.
*/
package ssdd.ms;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MessageSystem {
	private int pid;
	private boolean showDebugMsgs;
	private ArrayList<PeerAddress> addresses = new ArrayList<PeerAddress>();		//lista de @ip-puerto del resto de procesos
	private MailBox mailbox;
    private static int lamportClock;

	public MessageSystem(int source, String networkFile, boolean debug) throws FileNotFoundException {
		showDebugMsgs = debug;
		pid = source;
		int port = loadPeerAddresses(networkFile);
        lamportClock = 1;
		mailbox = new MailBox(port);
		mailbox.start();
	}

    /**
     * Envía el mensaje indicado al proceso indicado como un objeto serializable
     */
	public void send(int dst, Serializable message) {
		if (showDebugMsgs)
			System.out.println("Sending " + message.toString() + " from " + pid + " to " + dst+" with Lamport clock " + lamportClock);

        try {
			Envelope e = new Envelope(pid,dst,message,lamportClock); //crea mensaje
            PeerAddress p = addresses.get(dst-1);         //obtener ip-puerto
            Socket s = p.connect();
            ObjectOutputStream msg = new ObjectOutputStream(s.getOutputStream());
            msg.writeObject(e);
            msg.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Envía el mensaje indicado al todos los procesos menos a si mismo como un objeto serializable si
     * son mensajes de request o ack. En caso de ser otro tipo de mensajes, se lo envia a si mismo tambien.
	 */
	public void sendMulticast(Serializable message){
        if(!(message instanceof REQ) && !(message instanceof ACK)) {
            for (int i = 1; i <= addresses.size(); i++) {
                send(i, message);
            }
        }else{
            for (int i = 1; i <= addresses.size(); i++) {
                if (pid != i) {
                    send(i, message);
                }
            }
        }
	}

    /**
     * Devuelve el primero mensaje de la cola
     */
	public Envelope receive() {
        return mailbox.getNextMessage();
	}

    /**
     * Indica la finalizacion del proceso del buzon mediante
     * el envio de un mensaje
     */
	public void stopMailbox(int id) {
        try {
			send(id, new MessageValue("Fin"));
            mailbox.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int getLamportClock() {
        return lamportClock;
    }

    public static void setLamportClock(int lamportClock) {
        MessageSystem.lamportClock = lamportClock;
    }

    public int getProcess() {
        return addresses.size();
    }

    /**
     * Almacena en la lista addresses la información de las máquinas
     * disponible en el fichero.
     */
	private int loadPeerAddresses(String networkFile) throws FileNotFoundException {
		BufferedReader in = new BufferedReader(new FileReader(networkFile));
		String line;
		int port = 0;
		int n = 0;
		try {
			while ((line = in.readLine()) != null) {
				++n;
				int sep = line.indexOf(':');
				if (sep != -1) {
					addresses.add(new PeerAddress(
							line.substring(0, sep),
							Integer.parseInt(line.substring(sep + 1))));
					if (n == pid) {
                        port = addresses.get(addresses.size()-1).port;
                    }
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {}
		}
		return port;
	}
}
