/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: ServidorRunnable.java
* TIEMPO: 5
* DESCRIPCIÓN: Recibe la petición de un cliente concreto, la analiza
*           y le devuelve su correspondiente respuesta.
*/
package ssdd.p1;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;

public class ServidorRunnable implements Runnable{	

    static private Socket clientSocket; //socket de conexion con cliente
    static private OutputStream clientResponse;

    BlockingHTTPParser parser = new BlockingHTTPParser();
    //Info de respuesta del servidor
    static private String estado;
    static private String path;
    static private String bodyRespuesta;
    static private String bodyPeticion;
    static private String type;
    static private String length;
    static private URLDecoder dec;

    //Respuestas a peticiones a clientes
    private static final String FORBIDDEN = "<html><head>\n<title>403 Forbidden</title>\n" +
            "</head><body>\n<h1>Forbidden</h1>\n</body></html>\n";
    private static final String NOTFOUND = "<html><head>\n<title>404 Not Found</title>\n" +
            "</head><body>\n<h1>Not Found</h1>\n</body></html>\n";
    private static final String NOTIMPLEMENTED = "<html><head>\n<title>501 Not Implemented</title>\n" +
            "</head><body>\n<h1>Not Implemented</h1>\n</body></html>\n";
    private static final String BADREQUEST = "<html><head>\n<title>400 Bad Request</title>\n" +
            "</head><body>\n<h1>Bad Request</h1>\n</body></html>\n";

    /**
     * Constructor para almacenar socket, direccion y puerto
     */
	public ServidorRunnable(Socket S){
		this.clientSocket=S;
	}

    /**
     * Se ejecuta al lanzar este thread
     */
	public void run(){
		try{
			parser.parseRequest(clientSocket.getInputStream());
			//Peticion incompleta
			if(!parser.isComplete()){
				estado = "HTTP/1.1 400 Bad Request\n";
                bodyRespuesta = BADREQUEST;
                type = "Content-Type: " + "text/html\n";
                respuestaCliente(bodyRespuesta);
			}
			//Peticion tipo GET
			else {
				if (parser.getMethod().equalsIgnoreCase("GET")){
					path = parser.getPath();
					estado = buscarFichero(path);
                    String extension = "";
                    int i = path.lastIndexOf('.');
                    if (i > 0) {
                        extension = path.substring(i+1);
                    }
                    //Tratamiento de la extension de fichero
                    if(extension.equalsIgnoreCase("txt")) {
                        type = "Content-Type: " + "text/plain\n";
                    }else{
                        type = "Content-Type: " + "text/html\n";
                    }
                    respuestaCliente(bodyRespuesta);
				}
                //Peticion tipo POST
				else if (parser.getMethod().equalsIgnoreCase("POST")){
                    type = "Content-Type: " + "text/html\n";
					bodyPeticion = new String(parser.getBody().array(), "UTF-8");
					dec = new URLDecoder();
					String descodificado = dec.decode(bodyPeticion, "UTF-8");

                    //Descomposicion del cuerpo:  fname=nombre_fichero&content=contenido_fichero
                    String[] parts = descodificado.split("&");
                    String[] p = parts[0].split("=");
                    if(parts.length>0){
                        path = "/"+p[1];
                        String[] q = parts[1].split("=");
                        bodyRespuesta = q[1];
                        estado = buscarFichero(path);
                    }else{
                        estado = "HTTP/1.1 404 Not Found\n";
                        bodyRespuesta = NOTFOUND;
                        type = "Content-Type: " + "text/html\n";
                    }
                    if(estado.equalsIgnoreCase("HTTP/1.1 200 OK\n")){
                        String postRespuesta = "<html><head>\n<title>&#161&Eacutexito!</title>\n</head><body>" +
                                "<h1>&#161&Eacutexito!</h1>\n<p>Se ha escrito lo siguiente en el fichero " + path + ":</p>\n<pre>" +
                                bodyRespuesta +  "</pre>\n</body></html>";
                        respuestaCliente(postRespuesta);
                    }else{
                        respuestaCliente(bodyRespuesta);
                    }
				}
                //Metodo no implementado
				else {
					estado = "HTTP/1.1 501 Not Implemented\n";
                    bodyRespuesta = NOTIMPLEMENTED;
                    type = "Content-Type: " + "text/html\n";
                    respuestaCliente(bodyRespuesta);
				}
			}
		}catch (IOException e) {
   			System.err.println(e);
   			System.exit(-1);
   		}
	}

	/**
     * Busca en el path actual si se encuentra el fichero indicado
     * y devuelve el codigo necesario
	 */
	private String buscarFichero(String path) throws IOException{
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
    private static String leerFichero(String archivo)  throws IOException{
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

    /**
     * Envía la respueta al cliente, tanto cabecera como cuerpo.
     */
    private static void respuestaCliente(String respuesta) throws IOException{
        Integer aux = respuesta.length();
        length = "Content-Length: " + aux.toString() + "\n\n";
        clientResponse = clientSocket.getOutputStream();
        clientResponse.write(estado.getBytes());
        clientResponse.write(type.getBytes());
        clientResponse.write(length.getBytes());
        clientResponse.write(respuesta.getBytes());
        System.out.println("Ha finalizado la interaccion con el cliente con estado: "+estado);
        clientResponse.close();
    }
}