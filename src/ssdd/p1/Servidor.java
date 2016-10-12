package ssdd.p1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;

public class Servidor {
	// Almacenar la direccion y numero de puerto
	static private String ADDRESS;
	static private int PORT;
	static private String serverType;

	public static void main(String[] args) {

		try{
			ADDRESS= InetAddress.getLocalHost().toString();
		} catch (Exception e){
			System.err.println(e);
		}
		serverType = args[0];
		PORT = Integer.parseInt(args[1]);
        //ADDRESS = "10.0.2.15";
        System.out.println("addres ip: " + ADDRESS);

        if (serverType.equalsIgnoreCase("-s")){
            //serverSelect();
        }else if(serverType.equalsIgnoreCase("-t")){
            serverThreads();
        }else{
            System.out.println("Opcion no soportada");
        }
	}

	private static void serverThreads(){
        ServerSocket serverSocket = null; //para escuchar
        // Inicializa socket del cliente con el que se comunica el servidor,
        serverSocket = creaListenSocket(PORT);
        Socket clientSocket = null;       //uno por cliente

        try{
            //Lanza un thread cuando se conecta un cliente
            while (true) {
                System.out.println("lanza runnable");
                clientSocket = creaClientSocket(serverSocket);
                Thread t = new Thread(new ServidorRunnable(clientSocket,ADDRESS,PORT));
                t.start();
            }
            // Cierre del Socket para comunicarse con el servidor.
            //serverSocket.close();
        } catch (Exception e){
            System.err.println(e);
        }
    }

    private static void serverSelect(){

    }

	//Crea un socket de servidor
	//Aborta programa si no lo logra
	private static 
	        ServerSocket creaListenSocket(int serverSockNum){
		ServerSocket server = null;

		try{
    		server = new ServerSocket(serverSockNum);
  		} catch (IOException e) {
   			System.err.println("Problems in port: " + 
			                         serverSockNum);
   			System.exit(-1);
   		}
   		return server;
  	}

  	//Establece conexion con server y devuelve socket
  	//Aborta programa si no lo logra
  	private static
	    Socket creaClientSocket(ServerSocket server){
  		Socket res = null;

  		try {
			res = server.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}
		return res;
  	}
}