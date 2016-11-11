package ssdd.ms;

public class MailBox extends Thread {

    
    private int port;
    private ServerSocket socket;
    private MonitorType<Envelope> queue; // queue es un monitor, asignadle el nombre que queráis
    
	public MailBox(int p) {
		port = p;
		queue = new MonitorType<Envelope>();
	}
	
	public void run() { // ¿Servidor secuencial o concurrente?
		try {
            
            // TO DO
            
			} catch (SocketException e) {
			System.err.println("Cerrando buzón.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    
    public Envelope getNextMessage() {
        // TO DO
        return null;
    }

	
}
