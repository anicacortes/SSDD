package ssdd.p1;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ServidorSelect {

    static private int PORT;
    static private String path;
    static private String estado;
    static private String bodyRespuesta;
    static private String bodyPeticion;
    static private String type;
    static private URLDecoder dec;

    private static final String FORBIDDEN = "<html><head>\n<title>403 Forbidden</title>\n" +
            "</head><body>\n<h1>Forbidden</h1>\n</body></html>\n";
    private static final String NOTFOUND = "<html><head>\n<title>404 Not Found</title>\n" +
            "</head><body>\n<h1>Not Found</h1>\n</body></html>\n";
    private static final String NOTIMPLEMENTED = "<html><head>\n<title>501 Not Implemented</title>\n" +
            "</head><body>\n<h1>Not Implemented</h1>\n</body></html>\n";
    private static final String BADREQUEST = "<html><head>\n<title>400 Bad Request</title>\n" +
            "</head><body>\n<h1>Bad Request</h1>\n</body></html>\n";

    public ServidorSelect(int port){
        this.PORT = port;
    }

    public static void serverSelect() throws IOException {
        Selector selector = Selector.open();
        System.out.println("Selector abierto: " + selector.isOpen());
        ByteBuffer bufferEnt = ByteBuffer.allocate(2048); //capacidadEnt
        ByteBuffer bufferSal = ByteBuffer.allocate(2048); //capacidadSal

        ServerSocketChannel channelServer = ServerSocketChannel.open();
        channelServer.configureBlocking(false);  //Configura canal no bloqueante
        channelServer.socket().bind(new InetSocketAddress(PORT));
        SelectionKey keyServer = channelServer.register(selector, SelectionKey.OP_ACCEPT); //devuelve id de con quien se conecta

        while (true) {
            System.out.println("Esperando al select...");
            int nClientes = selector.select();
            if (nClientes == 0) {
                continue;
            }
            System.out.println("Numero de clientes seleccionados: " + nClientes);
            Set selectedKeys = selector.selectedKeys();
            Iterator iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey keyClient = (SelectionKey) iter.next();
                if (keyClient.isAcceptable()) {
                    System.out.println("Aeptamos conexion");
                    //Aceptamos nueva conexion con cliente
                    SocketChannel channelClient = channelServer.accept();
                    channelClient.configureBlocking(false);
                    HTTPParser parser = new HTTPParser();
                    //keyServer.attach(parser);   //asociar a cada cliente un httpParser
                    //Añadimos la nueva conexion al selector
                    channelClient.register(selector, SelectionKey.OP_READ,parser);
                } else if (keyClient.isReadable()) {
                    System.out.println("El cliente quiere leer");
                    //Leemos datos del cliente
                    SocketChannel channelClient = (SocketChannel) keyClient.channel();
                    HTTPParser parser = (HTTPParser) keyClient.attachment();
                    //bufferEnt.flip();
                    int nBytes = channelClient.read(bufferEnt);
                    System.out.println("Bytes recibidos: " + nBytes);
                    parser.parseRequest(bufferEnt);
                    if (parser.failed()) {
                        //Metodo no aceptado
                        estado = "HTTP/1.1 501 Not Implemented\n";
                        bodyRespuesta = NOTIMPLEMENTED;
                        type = "Content-Type: " + "text/html\n";
                        channelClient.register(selector,SelectionKey.OP_WRITE,parser);
                    } else if (parser.isComplete()) {
                        if (parser.getMethod().equalsIgnoreCase("GET")) {
                            path = parser.getPath();
                            estado = buscarFichero(path,parser);
                            String extension = "";
                            int i = path.lastIndexOf('.');
                            if (i > 0) {
                                extension = path.substring(i + 1);
                            }
                            if (extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png")
                                    || extension.equalsIgnoreCase("gif") || extension.equalsIgnoreCase("jpg")) {
                                type = "Content-Type: " + "/image\n";
                            } else if (extension.equalsIgnoreCase("txt")) {
                                type = "Content-Type: " + "text/plain\n";
                            } else {
                                type = "Content-Type: " + "text/html\n";
                            }
                            channelClient.register(selector, SelectionKey.OP_WRITE,parser);
                        }
                        //Peticion tipo POST
                        else if (parser.getMethod().equalsIgnoreCase("POST")) {
                            type = "Content-Type: " + "text/html\n";
                            bodyPeticion = new String(parser.getBody().array(), "UTF-8");
                            dec = new URLDecoder();
                            String descodificado = dec.decode(bodyPeticion, "UTF-8");

                            //fname=nombre_fichero&content=contenido_fichero
                            String[] parts = descodificado.split("&");
                            String[] p = parts[0].split("=");
                            if (parts.length > 0) {
                                path = "/" + p[1];
                                String[] q = parts[1].split("=");
                                bodyRespuesta = q[1];
                                System.out.println("path: " + path);
                                System.out.println("body fichero: " + bodyRespuesta);
                                estado = buscarFichero(path, parser);
                            } else {
                                estado = "HTTP/1.1 404 Not Found\n";
                                bodyRespuesta = NOTFOUND;
                                type = "Content-Type: " + "text/html\n";
                            }
                            if (estado.equalsIgnoreCase("HTTP/1.1 200 OK\n")) {
                                String postRespuesta = "<html><head>\n<title>¡Exito!</title>\n</head><body>" +
                                        "<h1>¡Exito!</h1>\n<p>Se ha escrito lo siguiente en el fichero " + path + ":</p>\n<pre>" +
                                        bodyRespuesta + "</pre>\n</body></html>";
                            }
                            channelClient.register(selector, SelectionKey.OP_WRITE,parser);
                        } else {
                            estado = "HTTP/1.1 400 Bad Request\n";
                            bodyRespuesta = BADREQUEST;
                            type = "Content-Type: " + "text/html\n";
                            channelClient.register(selector, SelectionKey.OP_WRITE,parser);
                        }

                        if (bufferEnt.hasRemaining()) {
                            //Si aun le quedan datos,tengo que seguir leyendo?
                        }
                        //ver si ha recibido toda la informacion
                        //calcular respuesta
                        //guardar la respuesta
                        String output = new String(bufferEnt.array()).trim();
                        System.out.println("Mensaje del cliente: " + output);
                    } else if (keyClient.isWritable()) {
                        System.out.println("El cliente quiere escribir");
                        channelClient = (SocketChannel) keyClient.channel();
                        //keyClient = channelClient.register(selector, SelectionKey.OP_WRITE); //escucha cliente para escribir
                        bufferSal.clear();
                        String respuesta = "";
                        bufferSal.put(respuesta.getBytes());
                        bufferSal.flip();
                        while (bufferSal.hasRemaining()) {
                            channelClient.write(bufferSal);
                        }

                    }
                }
                //Borramos las claves seleccionados porque ya las hemos tratado
                selectedKeys.clear();
            }
        }
    }

    /**
     * Busca en el path actual si se encuentra el fichero indicado
     * y devuelve el codigo necesario
     */
    private static String buscarFichero(String path, HTTPParser parser){
        String[] subDirs = path.split("/");
        if(subDirs.length>2){
            bodyRespuesta = FORBIDDEN;
            return "HTTP/1.1 403 Forbidden\n";
        }else if(subDirs.length == 0) {
            bodyRespuesta = NOTFOUND;
            return "HTTP/1.1 404 Not Found\n";
        }else{
            //obtiene path actual
            String p = System.getProperty("user.dir") + path;
            if(parser.getMethod().equalsIgnoreCase("GET")) {
                File dir = new File(p);
                if (dir.exists()) {
                    bodyRespuesta = leerFichero(p);
                    return "HTTP/1.1 200 OK\n";
                } else {
                    bodyRespuesta = NOTFOUND;
                    return "HTTP/1.1 404 Not Found\n";
                }
            }else{
                boolean ok = escribirFichero(p);
                if (ok){return "HTTP/1.1 200 OK\n";}
                else{
                    bodyRespuesta = NOTFOUND;
                    return "HTTP/1.1 404 Not Found\n";
                }
            }
        }
    }

    /**
     * Lee el contenido del fichero y lo devuelve
     * Devuelve null e informa del error en caso de producirse
     */
    private static String leerFichero(String archivo){
        try {
            String cadena; String cuerpo = "";
            FileReader f = new FileReader(archivo);
            BufferedReader b = new BufferedReader(f);
            while ((cadena = b.readLine()) != null) {
                cuerpo += cadena + "\n";
            }
            b.close();
            return cuerpo;
        }
        catch (FileNotFoundException e){System.out.println("No se ha encontrado el fichero");}
        catch (IOException e){System.out.println("Excepcion: "+e);}
        return null;
    }

    /**
     * Crea el fichero path con el contenido body
     * Devuelve null e informa del error en caso de producirse
     */
    private static boolean escribirFichero(String archivo){
        try {
            File f = new File(archivo);
            f.createNewFile();
            FileWriter fichero = new FileWriter(f);
            BufferedWriter b = new BufferedWriter(fichero);
            b.write(bodyRespuesta);
            b.close();
            return true;
        }catch (IOException e){
            System.out.println("Excepcion: "+e);
            return false;
        }
    }
}
