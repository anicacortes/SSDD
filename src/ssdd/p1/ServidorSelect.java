package ssdd.p1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

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
            System.out.println("Selector abierto: " + selector.isOpen());
            channelServer = ServerSocketChannel.open();
            channelServer.configureBlocking(false);  //Configura canal no bloqueante
            channelServer.socket().bind(new InetSocketAddress(PORT));
            SelectionKey keyServer = channelServer.register(selector, SelectionKey.OP_ACCEPT); //devuelve id de con quien se conecta

            while(true) {
                System.out.println("Esperando al select...");
                int nClientes = selector.select();
                if (nClientes == 0) {
                    continue;
                }
                System.out.println("Numero de clientes seleccionados: " + nClientes);
                Set selectedKeys = selector.selectedKeys();
                Iterator iter = selectedKeys.iterator();
                SelectionKey keyClient;
                while (iter.hasNext()) {
                    SelectionKey k = (SelectionKey)iter.next();
                    if (k.isAcceptable()) {
                        //Aceptamos nueva conexion con cliente
                        SocketChannel channelClient = channelServer.accept();
                        channelClient.configureBlocking(false);
                        HTTPParser parser = null;
                        keyServer.attach(parser);   //asociar a cada cliente un httpParser
                        //Añadimos la nueva conexion al selector
                        keyClient = channelClient.register(selector, SelectionKey.OP_READ);
                    }
                    else if (k.isReadable()) {
                        System.out.println("El cliente quiere leer");
                        //Leemos datos del cliente
                        SocketChannel channelClient = (SocketChannel) k.channel();
                        int nBytes = channelClient.read(buffer);
                        System.out.println("Bytes recibidos: " + nBytes);
                        if(buffer.hasRemaining()) {
                            //Si aun le quedan datos,tengo que seguir leyendo?
                        }
                        //ver si ha recibido toda la informacion
                        //calcular respuesta
                        //guardar la respuesta
                        String output = new String(buffer.array()).trim();
                        System.out.println("Mensaje del cliente: " + output);
                    }
                    else if (k.isWritable()) {
                        System.out.println("El cliente quiere escribir");
                        SocketChannel channelClient = (SocketChannel) k.channel();
                        keyClient = channelClient.register(selector, SelectionKey.OP_WRITE); //escucha cliente para escribir
                        System.out.println("Un cliente quiere escribir");
                        buffer.clear();
                        String respuesta = "";
                        buffer.put(respuesta.getBytes());
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            channelClient.write(buffer);
                        }
                    }
                }
                //Borramos las claves seleccionados porque ya las hemos tratado
                selectedKeys.clear();
            }
        }catch (IOException e){}
        System.err.println("Exception: ");

    }
}
