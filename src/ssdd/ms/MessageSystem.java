package ssdd.ms;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

public class MessageSystem {
	private int pid;
	private boolean showDebugMsgs;
	private ArrayList<PeerAddress> addresses = new ArrayList<PeerAddress>();		//lista de @ip-puerto del resto de procesos
	private MailBox mailbox;

	public MessageSystem(int source, String networkFile, boolean debug) throws FileNotFoundException {
		showDebugMsgs = debug;
		pid = source;
		int port = loadPeerAddresses(networkFile);
		mailbox = new MailBox(port);
		mailbox.start();
	}
	
	public void send(int dst, Serializable message) {
		if (showDebugMsgs)
			System.out.println("Sending " + message.toString() + " from " + pid + " to " + dst);
		try {
			Envelope e = new Envelope(pid,dst,message); //crea mensaje
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

	public Envelope receive() {
        return mailbox.getNextMessage();
	}
	
	public void stopMailbox() {
        try {
            mailbox.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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