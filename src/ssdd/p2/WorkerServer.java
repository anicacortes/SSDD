package ssdd.p2;


import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Servidor de calculo
 */
public class WorkerServer implements Worker{
    String IP;
    int contador = 0;

    public WorkerServer(String IP) {
        this.IP = IP;
    }
    public WorkerServer() {

    }

    public void registrar () {
        try{
            Registry registry = LocateRegistry.getRegistry(IP);
            Worker w = new WorkerServer();
            Worker stub = (Worker) UnicastRemoteObject.exportObject(w, 0);
            String[] listNames = registry.list();
            contador = listNames.length+1;
            String name = "WorkerServer" + contador;
            registry.bind(name, stub);

        } catch (RemoteException re) {
            System.out.println();
        }
        catch (AlreadyBoundException e) {

        }
    }

    public ArrayList<Integer> encuentraPrimos(int min, int max) {
        ArrayList<Integer> in = null;

        return in;
    }
}
