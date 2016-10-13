package ssdd.p1;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServidorRunnable implements Runnable{	
	
	// Guardar direccion y puerto donde escucha servidor
	static private String ADDRESS;
	static private int PORT;
    private Socket clientSocket; //socket de conexion con cliente
    private OutputStream clientResponse;

    BlockingHTTPParser parser = new BlockingHTTPParser();
    //Info de respuesta del servidor
	private String estado = "HTTP/1.1 ";
	private String path;
	private String body;
    private String type;
    private String length = "Content-Length: ";
	private URLDecoder dec;
    private Pattern fname = Pattern.compile("fname=.*&");
    private Pattern content = Pattern.compile("&content=.*");

    private static String FORBIDDEN = "<html><head>\n<title>403 Forbidden</title>\n" +
            "</head><body>\n<h1>Forbidden</h1>\n</body></html>\n";
    private static String NOTFOUND = "<html><head>\n<title>404 Not Found</title>\n" +
            "</head><body>\n<h1>Not Found</h1>\n</body></html>\n";

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
				estado += "400 Bad Request\n";
			}
			//Peticion tipo GET
			else {
				if (parser.getMethod().equalsIgnoreCase("GET")){
					path = parser.getPath();
					estado += buscarFichero(path);

                    //Devuelve contenido al cliente por bytebuffer
                    //
                    Integer aux = body.length();
                    length += aux.toString()+"\n\n";
                    clientResponse = clientSocket.getOutputStream();
                    clientResponse.write(estado.getBytes());
                    clientResponse.write(length.getBytes());
                    clientResponse.write(body.getBytes());
                    clientResponse.close();
				}
                //Peticion tipo POST
				else if (parser.getMethod().equalsIgnoreCase("POST")){
					body = new String(parser.getBody().array(), "UTF-8");
					dec = new URLDecoder();
					String descodificado = dec.decode(body, "UTF-8");

                    Matcher matcherF = fname.matcher(descodificado);
                    Matcher matcherC = content.matcher(descodificado);
                    if (matcherF.matches()) {
                        path = matcherF.group();
                    }
                    //Comprobaciones del fichero
                    estado = buscarFichero(path);
                    if (matcherC.matches()) {
                        body = matcherC.group();
                    }


				} else {
                    //Metodo no aceptado
					estado += "501 Not Implemented\n";
				}
			}
		}catch (IOException e) {
   			System.err.println(e);
   			System.exit(-1);
   		}
   		System.out.println(estado);
        System.out.println(type);
        System.out.println(length);
        System.out.println(body);
	}

	/**
     * Busca en el path actual si se encuentra el fichero indicado
     * y devuelve el codigo necesario
	 */
	private String buscarFichero(String path){
		String[] subDirs = path.split("/");
		if(subDirs.length>2){
            body = FORBIDDEN;
			return "403 Forbidden\n";
		}else if(subDirs.length == 0) {
            body = NOTFOUND;
            return "404 Not Found\n";
        }else{
            //obtiene path actual
            String p = System.getProperty("user.dir") + path;
            File dir = new File(p);
            if (dir.exists()) {
                body = leerFichero(p);
                return "200 OK\n";
            } else {
                body = NOTFOUND;
                return "404 Not Found\n";
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
}