package ssdd.p1;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    static private boolean primeraVez;


    private static final String FORBIDDEN = "<html><head>\n<title>403 Forbidden</title>\n" +
            "</head><body>\n<h1>Forbidden</h1>\n</body></html>\n";
    private static final String NOTFOUND = "<html><head>\n<title>404 Not Found</title>\n" +
            "</head><body>\n<h1>Not Found</h1>\n</body></html>\n";
    private static final String NOTIMPLEMENTED = "<html><head>\n<title>501 Not Implemented</title>\n" +
            "</head><body>\n<h1>Not Implemented</h1>\n</body></html>\n";
    private static final String BADREQUEST = "<html><head>\n<title>400 Bad Request</title>\n" +
            "</head><body>\n<h1>Bad Request</h1>\n</body></html>\n";

    public ServidorSelect(int port) {
        this.PORT = port;
    }

    /**
     * Encargado de gestionar las peticiones de los clientes
     * mediante el selector
     */
    public static void serverSelect() throws IOException {
        Selector selector = Selector.open();
        ByteBuffer bufferEnt = ByteBuffer.allocate(256);
        ByteBuffer bufferSal = ByteBuffer.allocate(512);
        primeraVez=true;
        FileChannel fc = null;

        //Apertura del canal y configuracion no bloqueante
        ServerSocketChannel channelServer = ServerSocketChannel.open();
        channelServer.configureBlocking(false);
        channelServer.socket().bind(new InetSocketAddress(PORT));
        channelServer.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            //Recibe peticiones de los clientes que se conectan
            selector.select();
            //Crea iterador sobre las peticiones que estan pendientes de atender
            Set selectedKeys = selector.selectedKeys();
            Iterator iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey keyClient = (SelectionKey) iter.next();
                iter.remove();

                //Cliente quiere conectarse al servidor
                if (keyClient.isAcceptable()) {
                    //Aceptamos nueva conexion con cliente y se le asocia el parser
                    SocketChannel channelClient = channelServer.accept();
                    channelClient.configureBlocking(false);
                    HTTPParser parser = new HTTPParser();
                    //Añadimos la nueva conexion de lectura al selector
                    channelClient.register(selector, SelectionKey.OP_READ, parser);
                }
                //Cliente quiere enviarle datos al servidor
                else if (keyClient.isReadable()) {
                    //Leemos datos del cliente
                    SocketChannel channelClient = (SocketChannel) keyClient.channel();
                    HTTPParser parser = (HTTPParser) keyClient.attachment();
                    int nbytes = channelClient.read(bufferEnt);
                    bufferEnt.flip();
                    parser.parseRequest(bufferEnt);
                    System.out.println("leidos " + nbytes + " bytes de entrada");
                    //Si se ha completado la peticion, cambia a estado de escritura
                    if (nbytes < 256) {
                        channelClient.register(selector, SelectionKey.OP_WRITE, parser);
                        bufferEnt.clear();
                    }
                    //Si la peticion no esta completa, seguira leyendo en la siguiente iteracion
                    //path = null; estado = null;
                }

                //Cliente esta preparado para leer la respuesta del servidor
                else if (keyClient.isWritable()) {
                    System.out.println("Entro a escribir");
                    if (primeraVez) {
                        System.out.println("ES primera vez");
                        HTTPParser parser = (HTTPParser) keyClient.attachment();
                        //Peticion erronea, almacena resultado en el buffer
                        if (parser.failed() || !parser.isComplete()) {
                            primeraVez = false;
                            estado = "HTTP/1.1 400 Bad Request\n";
                            bodyRespuesta = BADREQUEST;
                            type = "Content-Type: " + "text/html\n";
                            //bufferSal.clear();
                            Integer aux = bodyRespuesta.length();
                            String length = "Content-Length: " + aux.toString() + "\n\n";
                            String respuesta = estado + type + length + bodyRespuesta;
                        /*bufferSal = ByteBuffer.allocate(respuesta.length());
                        bufferSal.put(respuesta.getBytes());
                        bufferSal.flip();
                        channelClient.register(selector, SelectionKey.OP_WRITE, bufferSal);
                        bufferEnt.clear();*/
                        }
                        //Peticion correcta, almacena resultado en el buffer
                        else if (parser.isComplete()) {
                            System.out.println("La peticion esta completa");
                            primeraVez = false;
                            //Peticion GET
                            if (parser.getMethod().equalsIgnoreCase("GET")) {
                                path = parser.getPath();
                                estado = buscarFichero(path, parser);
                                String extension = "";
                                int i = path.lastIndexOf('.');
                                if (i > 0) {
                                    extension = path.substring(i + 1);
                                }
                                //Gestion de extension del fichero
                                if (extension.equalsIgnoreCase("txt")) {
                                    type = "Content-Type: " + "text/plain\n";
                                } else {
                                    type = "Content-Type: " + "text/html\n";
                                }
                            }
                            //Peticion tipo POST
                            else if (parser.getMethod().equalsIgnoreCase("POST")) {
                                type = "Content-Type: " + "text/html\n";
                                bodyPeticion = new String(parser.getBody().array(), "UTF-8");
                                dec = new URLDecoder();
                                String descodificado = dec.decode(bodyPeticion, "UTF-8");

                                //Descomposicion del cuerpo:  fname=nombre_fichero&content=contenido_fichero
                                String[] parts = descodificado.split("&");
                                String[] p = parts[0].split("=");
                                if (parts.length > 0) {
                                    path = "/" + p[1];
                                    String[] q = parts[1].split("=");
                                    bodyRespuesta = q[1];
                                    estado = buscarFichero(path, parser);
                                } else {
                                    estado = "HTTP/1.1 404 Not Found\n";
                                    bodyRespuesta = NOTFOUND;
                                    type = "Content-Type: " + "text/html\n";
                                }

                            } else {
                                //Metodo no aceptado
                                estado = "HTTP/1.1 501 Not Implemented\n";
                                bodyRespuesta = NOTIMPLEMENTED;
                                type = "Content-Type: " + "text/html\n";
                            }
                            String respuesta = "";
                            Long aux;
                            String length = "";
                            SocketChannel channelClient = (SocketChannel) keyClient.channel();
                            Path pathFc = Paths.get(System.getProperty("user.dir") + path);

                            if (estado.equalsIgnoreCase("HTTP/1.1 200 OK\n")) {
                                System.out.println("OK Mando contenido fichero");
                                String postRespuesta = "";
                                if (parser.getMethod().equalsIgnoreCase("POST")) {
                                    postRespuesta = "<html><head>\n<title>&#161&Eacutexito!</title>\n</head><body>" +
                                            "<h1>&#161&Eacutexito!</h1>\n<p>Se ha escrito lo siguiente en el fichero " + path + ":</p>\n<pre>";
                                }
                                fc = FileChannel.open(pathFc);
                                aux = fc.size();
                                length = "Content-Length: " + aux.toString() + "\n\n";
                                respuesta = estado + type + length + postRespuesta;
                                System.out.println("Bytes q oucpa la respuesta: " + respuesta.getBytes().length);
                                bufferSal.clear();
                                bufferSal.allocate(respuesta.length()*2);
                                bufferSal.put(respuesta.getBytes());
                                bufferSal.flip();
                                channelClient.write(bufferSal);
                            } else {
                                System.out.println("Mala peticion");
                                aux = (long) bodyRespuesta.length();
                                length = "Content-Length: " + aux.toString() + "\n\n";
                                respuesta = estado + type + length + bodyRespuesta;
                                bufferSal.clear();
                                bufferSal.put(respuesta.getBytes());
                                bufferSal.flip();
                                channelClient.write(bufferSal);
                                System.out.println("Ha finalizado la interaccion con el cliente");
                                channelClient.close();
                                primeraVez = true;
                            }
                        }
                    } else {
                        System.out.println("NO primera vez");
                        SocketChannel channelClient = (SocketChannel) keyClient.channel();
                        HTTPParser parser = (HTTPParser) keyClient.attachment();
                        int nbytes = fc.read(bufferSal);
                        System.out.println("bytes enviados: " + nbytes);
                        channelClient.write(bufferSal);
                        if (nbytes < 512) {
                            if (parser.getMethod().equalsIgnoreCase("POST")) {
                                bufferSal.clear();
                                String respuesta = "</pre>\n</body></html>";
                                bufferSal.put(respuesta.getBytes());
                                bufferSal.flip();
                                channelClient.write(bufferSal);
                            }
                            fc.close();
                            primeraVez = true;
                            System.out.println("Ha finalizado la interaccion con el cliente");
                            channelClient.close();
                        }
                    }
                }
            }
        }
    }


    /**
     * Busca en el path actual si se encuentra el fichero indicado
     * y devuelve el codigo necesario
     */
    private static String buscarFichero(String path, HTTPParser parser) throws IOException{
        String[] subDirs = path.split("/");
        if (subDirs.length > 2) {
            bodyRespuesta = FORBIDDEN;
            return "HTTP/1.1 403 Forbidden\n";
        } else if (subDirs.length == 0) {
            bodyRespuesta = NOTFOUND;
            return "HTTP/1.1 404 Not Found\n";
        } else {
            //obtiene path actual
            String p = System.getProperty("user.dir") + path;
            if (parser.getMethod().equalsIgnoreCase("GET")) {
                File dir = new File(p);
                if (dir.exists()) {
//                    bodyRespuesta = leerFichero(p);
                    return "HTTP/1.1 200 OK\n";
                } else {
                    bodyRespuesta = NOTFOUND;
                    return "HTTP/1.1 404 Not Found\n";
                }
            } else {
                boolean ok = escribirFichero(p);
                if (ok) {
                    return "HTTP/1.1 200 OK\n";
                } else {
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
    private static String leerFichero(String archivo) throws IOException{
        try {
            String cadena;
            String cuerpo = "";
            FileReader f = new FileReader(archivo);
            BufferedReader b = new BufferedReader(f);
            while ((cadena = b.readLine()) != null) {
                cuerpo += cadena + "\n";
            }
            b.close();
            return cuerpo;
        } catch (FileNotFoundException e) {
            System.out.println("No se ha encontrado el fichero");
        }
        return null;
    }

    /**
     * Crea el fichero path con el contenido body
     * Devuelve null e informa del error en caso de producirse
     */
    private static boolean escribirFichero(String archivo) throws IOException{
        File f = new File(archivo);
        f.createNewFile();
        FileWriter fichero = new FileWriter(f);
        BufferedWriter b = new BufferedWriter(fichero);
        b.write(bodyRespuesta);
        b.close();
        return true;
    }
}
