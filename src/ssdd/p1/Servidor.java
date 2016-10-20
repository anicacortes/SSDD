/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: Servidor.java
* TIEMPO: 0.5
* DESCRIPCIÓN: Recibe los parámetros que indican el tipo de servidor y el puerto
*           y ejecuta el servidor adecuado.
*/
package ssdd.p1;

import java.io.IOException;
import java.net.InetAddress;

public class Servidor {
	// Almacenar la direccion y numero de puerto
	static private String ADDRESS;
	static private int PORT;
	static private String serverType;

	public static void main(String[] args){

		try{
			ADDRESS= InetAddress.getLocalHost().toString();
		} catch (Exception e){
			System.err.println(e);
		}
		serverType = args[0];
		PORT = Integer.parseInt(args[1]);
		try {
			if (serverType.equalsIgnoreCase("-s")) {
				ServidorSelect s = new ServidorSelect(PORT);
				s.serverSelect();
			} else if (serverType.equalsIgnoreCase("-t")) {
				ServidorThreads s = new ServidorThreads(PORT);
				s.serverThreads();
			} else {
				System.out.println("Opcion no soportada");
			}
		}catch (IOException e){
            System.out.println("Excepcion: " + e);
        }
	}


}