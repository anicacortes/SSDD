package ssdd.p1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorThreads {

    static private int PORT;

    public ServidorThreads(int port){
        this.PORT = port;
    }

    public static void serverThreads()  throws IOException{
        ServerSocket serverSocket = null;
        // Inicializa socket del cliente con el que se comunica el servidor,
        serverSocket = creaListenSocket(PORT);
        Socket clientSocket = null;
        try{
            //Lanza un thread cuando se conecta un cliente
            while (true) {
                clientSocket = creaClientSocket(serverSocket);
                Thread t = new Thread(new ServidorRunnable(clientSocket));
                t.start();
            }
        } catch (Exception e){
            System.err.println(e);
        }
    }

    /**
     * Crea un socket del servidor
     */
    private static ServerSocket creaListenSocket(int serverSockNum) throws IOException{
        ServerSocket server = null;
        server = new ServerSocket(serverSockNum);
        return server;
    }

    /**
     * Establece conexion con server y devuelve el socket
     * Aborta programa si no lo logra
     */
    private static Socket creaClientSocket(ServerSocket server) throws IOException{
        Socket res = null;
        res = server.accept();
        return res;
    }
}
