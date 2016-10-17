package ssdd.p1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorThreads {

    static private int PORT;

    public ServidorThreads(int port){
        this.PORT = port;
    }

    public static void serverThreads(){
        ServerSocket serverSocket = null; //para escuchar
        // Inicializa socket del cliente con el que se comunica el servidor,
        serverSocket = creaListenSocket(PORT);
        Socket clientSocket = null;       //uno por cliente
        try{
            //Lanza un thread cuando se conecta un cliente
            while (true) {
                clientSocket = creaClientSocket(serverSocket);
                Thread t = new Thread(new ServidorRunnable(clientSocket));
                t.start();
            }
            // Cierre del Socket para comunicarse con el servidor.
            //serverSocket.close();
        } catch (Exception e){
            System.err.println(e);
        }
    }

    //Crea un socket de servidor
    //Aborta programa si no lo logra
    private static ServerSocket creaListenSocket(int serverSockNum){
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
    private static Socket creaClientSocket(ServerSocket server){
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
