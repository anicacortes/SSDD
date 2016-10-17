package ssdd.p1;

import java.io.IOException;
import java.net.InetAddress;

public class Servidor {
	// Almacenar la direccion y numero de puerto
	static private String ADDRESS;
	static private int PORT;
	static private String serverType;

	public static void main(String[] args) throws IOException{

		try{
			ADDRESS= InetAddress.getLocalHost().toString();
		} catch (Exception e){
			System.err.println(e);
		}
		serverType = args[0];
		PORT = Integer.parseInt(args[1]);

        if (serverType.equalsIgnoreCase("-s")){
			ServidorSelect s = new ServidorSelect(PORT);
			s.serverSelect();
        }else if(serverType.equalsIgnoreCase("-t")){
			ServidorThreads s = new ServidorThreads(PORT);
			s.serverThreads();
        }else{
            System.out.println("Opcion no soportada");
        }
	}


}