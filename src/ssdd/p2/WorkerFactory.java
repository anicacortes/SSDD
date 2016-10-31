package ssdd.p2;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface WorkerFactory extends Remote {
    // Devuelve un vector de hasta n referencias a objetos Worker.
    ArrayList<Worker> dameWorkers(int n) throws RemoteException;
}
