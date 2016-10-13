package ssdd.p1;

import java.io.File;
import java.io.IOException;
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

    BlockingHTTPParser parser = new BlockingHTTPParser();
    //Info de respuesta del servidor
	private String estado = "HTTP/1.1 ";
	private String path;
	private String body;
    private String type;
    private String length;
	private URLDecoder dec;
    private Pattern fname = Pattern.compile("fname=.*&");
    private Pattern content = Pattern.compile("&content=.*");
    //Constructor para almacenar socket, direccion y puerto
	public ServidorRunnable(Socket S,String address,int port){
		this.clientSocket=S;
		ADDRESS = address;
		PORT = port;
	}
	public void run(){
		try{
			parser.parseRequest(clientSocket.getInputStream());
			//Peticion esta incompleta
			if(!parser.isComplete()){
				estado += "400 Bad Request";
			}else {
				if (parser.getMethod().equalsIgnoreCase("GET")){
					path = parser.getPath();
                    System.out.println("path: " + path);
					estado = buscarFichero(path);
                    System.out.println("Estado= " + estado);
                    //byte[] bytes = parser.getBody().getBytes(Charset.forName("UTF-8" ));
                    //String v = new String(bytes,Charset.forName("UTF-8") );
                    //body = new String(parser.getBody().array());

				} else if (parser.getMethod().equalsIgnoreCase("POST")){
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
					estado += "501 Not Implemented";
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
        for(int i=0; i<subDirs.length;i++ ){
            System.out.println(subDirs[i]);
        }
		if(subDirs.length>2){
            body = " <html><head>\n" +
                    "<title>403 Forbidden</title>\n" +
                    "</head><body>\n" +
                    "<h1>Forbidden</h1>\n" +
                    "</body></html>";
			return "403 Forbidden";
		}else if(subDirs.length == 0) {
            body = " <html><head>\n" +
                    "<title>404 Not Found</title>\n" +
                    "</head><body>\n" +
                    "<h1>Not Found</h1>\n" +
                    "</body></html>";
            return "404 Not Found";
        }else{
            String p = System.getProperty("user.dir") + path;
            File dir = new File(p); //obtiene path actual
            if (dir.exists()) {
                System.out.println(parser.getBody().flip());
                body = new String(parser.getBody().array());
                return "200 OK";
            } else {
                body = " <html><head>\n" +
                        "<title>404 Not Found</title>\n" +
                        "</head><body>\n" +
                        "<h1>Not Found</h1>\n" +
                        "</body></html>";
                return "404 Not Found";
            }
        }
	}
}