package ssdd.p2;


import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Servidor de asignación
 */
public class WorkerFactoryServer implements WorkerFactory{

    String IP;

    public WorkerFactoryServer(String IP) {
        this.IP = IP;
    }
    public WorkerFactoryServer() { //necesario?
    }

    public void registrar () {
        try{
            Registry registry = LocateRegistry.getRegistry(IP);
            WorkerFactory w = new WorkerFactoryServer();
            WorkerFactory stub = (WorkerFactory) UnicastRemoteObject.exportObject(w, 0); //PUERTO DE RMI?
            registry.bind("WorkerFactoryServer", stub);

        } catch (RemoteException re) {
            System.out.println();
        }
        catch (AlreadyBoundException e) {

        }
    }

    public ArrayList<Worker> dameWorkers (int n) throws RemoteException {
        ArrayList<Worker> listWorkers = null;
        try {

            Registry registry = LocateRegistry.getRegistry(IP);
            if (registry.list().length - 1 < n) {
                System.out.println("No hay suficientes workers lanzados");
                return null;
            }
            else {
                String[] listNames = registry.list();
                for (int i = 0; i < n; i++) {
                    if (!listNames[i].contains("Factory")) {
                        Worker stub = (Worker) registry.lookup(listNames[i]);
                    }
                }
                //listWorkers.add();
                return listWorkers;

            }
        }
        catch (NotBoundException e) {

        }
        return listWorkers;
    }
}
