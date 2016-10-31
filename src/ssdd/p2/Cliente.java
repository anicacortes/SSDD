package ssdd.p2;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Cliente {
    private int min;
    private int max;
    private int n;
    private String IP;

        public Cliente(int min, int max, int n, String IP) {
            this.min = min;
            this.max = max;
            this.n = n;
            this.IP = IP;
        }

        public void interaccion () {
            try {
                Registry registry = LocateRegistry.getRegistry(IP);
                WorkerFactory stub = (WorkerFactory) registry.lookup("WorkerFactoryServer");

                ArrayList<Worker> listWorkers = stub.dameWorkers(n);
            } catch (Exception e) {
                System.err.println("Client exception: " + e.toString());
                e.printStackTrace();
            }
        }

}
