/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: PeerAddress.java
* TIEMPO: 0h
* DESCRIPCIÓN: Se encarga de la conexion con la máquina con los datos
*            de ip puerto correspondientes.
*/
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
		return new Socket(address, port);
	}
}
