package ssdd.p1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServidorSelect {

    static private int PORT;

    public ServidorSelect(int port){
        this.PORT = port;
    }

    public static void serverSelect(){
        ServerSocketChannel channelServer = null;
        Selector selector;
        ByteBuffer buffer = ByteBuffer.allocate(100); //capacidad

        try {
            selector = Selector.open();
            channelServer = ServerSocketChannel.open();
            channelServer.configureBlocking(false);  //Configura canal no bloqueante
            channelServer.socket().bind(new InetSocketAddress(PORT));
            System.out.println("Selector abierto: " + selector.isOpen());


            int nClientes = selector.selectNow();
            System.out.println("numero clientes esperando conectarse: " + nClientes);
            SelectionKey keyServer = channelServer.register(selector, SelectionKey.OP_ACCEPT); //devuelve id de con quien se conecta
            System.out.println("Se ha conectado un cliente");
            //Crea un canal para cada cliente
            SocketChannel channelClient = channelServer.accept();
            channelClient.configureBlocking(false);
            HTTPParser parser = null;
            keyServer.attach(parser);   //asociar a cada cliente un httpParser

            SelectionKey keyClient = channelClient.register(selector, SelectionKey.OP_READ); //escucha cliente para leer
            System.out.println("Un cliente quiere leer");
            int bytesRead = channelClient.read(buffer);
            //mirar si has recibido todo


            //ver si ha recibido toda la informacion
            //calcular respuesta
            //guardar la respuesta

            keyClient = channelClient.register(selector, SelectionKey.OP_WRITE); //escucha cliente para escribir
            System.out.println("Un cliente quiere escribir");
            buffer.clear();
            String respuesta="";
            buffer.put(respuesta.getBytes());
            buffer.flip();
            while(buffer.hasRemaining()){
                channelClient.write(buffer);
            }
        }catch (IOException e){}


    }
}
