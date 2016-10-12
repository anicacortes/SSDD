package ssdd.p1;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
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
				if (parser.getMethod() == "GET") {
					path = parser.getPath();
					estado = buscarFichero(path);
					body = new String(parser.getBody().array(), "UTF-8");

				} else if (parser.getMethod() == "POST") {

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
		String[] subDirs = path.split(Pattern.quote(File.separator));
		if(subDirs.length>1){
			return "403 Forbidden";
		}
		File dir = new File(path); //obtiene path actual
		if(dir.exists()){
			return "200 OK";
		}else{
			return "404 Not Found";
		}
	}
}