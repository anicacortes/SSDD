package ssdd.p1;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServidorRunnable implements Runnable{	
	
	// Guardar direccion y puerto donde escucha servidor
	static private String ADDRESS;
	static private int PORT;
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
    static private Pattern fname = Pattern.compile("fname=.*&");
    static private Pattern content = Pattern.compile("&content=.*");

    private static final String FORBIDDEN = "<html><head>\n<title>403 Forbidden</title>\n" +
            "</head><body>\n<h1>Forbidden</h1>\n</body></html>\n";
    private static final String NOTFOUND = "<html><head>\n<title>404 Not Found</title>\n" +
            "</head><body>\n<h1>Not Found</h1>\n</body></html>\n";
    private static final String EXITOPOST = "<html><head>\n<title>¡Éxito!</title>\n</head><body>" +
            "<h1>¡Éxito!</h1>\n<p>Se ha escrito lo siguiente en el fichero" + path + ":</p>\n<pre>" +
            bodyRespuesta +  "</pre>\n</body></html>";

    //Constructor para almacenar socket, direccion y puerto
	public ServidorRunnable(Socket S,String address,int port){
		this.clientSocket=S;
		ADDRESS = address;
		PORT = port;
	}

	public void run(){
		try{
			parser.parseRequest(clientSocket.getInputStream());
			//Peticion incompleta
			if(!parser.isComplete()){
				estado = "HTTP/1.1 400 Bad Request\n";
			}
			//Peticion tipo GET
			else {
				if (parser.getMethod().equalsIgnoreCase("GET")){
					path = parser.getPath();
					estado = buscarFichero(path);
                    respuestaCliente(bodyRespuesta);
				}
                //Peticion tipo POST
				else if (parser.getMethod().equalsIgnoreCase("POST")){
					bodyPeticion = new String(parser.getBody().array(), "UTF-8");
                    System.out.println("body recibido: " + bodyPeticion);
					dec = new URLDecoder();
					String descodificado = dec.decode(bodyPeticion, "UTF-8");

                    //fname=nombre_fichero&content=contenido_fichero
                    String[] parts = descodificado.split("&");
                    String[] p = parts[0].split("=");
                    path = p[1];
                    String[] q = parts[1].split("=");
                    bodyRespuesta = q[1];
                    System.out.println("path: " + path);
                    System.out.println("body fichero: " + bodyRespuesta);
                    buscarFichero(path);
                    respuestaCliente(EXITOPOST);
				} else {
                    //Metodo no aceptado
					estado = "HTTP/1.1 501 Not Implemented\n";
				}
			}
		}catch (IOException e) {
   			System.err.println(e);
   			System.exit(-1);
   		}
   		System.out.println(estado);
        System.out.println(path);
        System.out.println(length);
        System.out.println(bodyRespuesta);
	}

	/**
     * Busca en el path actual si se encuentra el fichero indicado
     * y devuelve el codigo necesario
	 */
	private String buscarFichero(String path){
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
            FileWriter fichero = new FileWriter(archivo);
            BufferedWriter b = new BufferedWriter(fichero);
            b.write(bodyRespuesta);
            b.close();
            return true;
        }catch (IOException e){
            System.out.println("Excepcion: "+e);
            return false;
        }
    }

    /**
     *
     * @throws IOException
     */
    private static void respuestaCliente(String respuesta) throws IOException{
        //Devuelve contenido al cliente
        Integer aux = bodyRespuesta.length();
        length = "Content-Length: " + aux.toString() + "\n\n";
        clientResponse = clientSocket.getOutputStream();
        clientResponse.write(estado.getBytes());
        clientResponse.write(length.getBytes());
        clientResponse.write(respuesta.getBytes());
        clientResponse.close();
    }
}