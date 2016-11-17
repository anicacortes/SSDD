package ssdd.ms;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerAddress {
	public String address;
	public int port;

	public PeerAddress(String a, int p) {
		address = a;
		port = p;
	}
	
	public Socket connect() throws UnknownHostException, IOException {
		System.out.println("IP: "+address + " port: "+port);
		return new Socket(address, port);
	}
}
