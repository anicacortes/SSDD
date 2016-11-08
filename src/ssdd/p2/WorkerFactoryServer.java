/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: WorkerFactoryServer.java
* TIEMPO: 1h
* DESCRIPCIÓN: Servidor de asignación que devuelve los n servidores de calculo al cliente, si hay suficientes.
*/
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

    /**
     * Registra en el registroRMI el servidor de asignación que se lanza
     */
    public void registrar () {
        try{
            LocateRegistry.createRegistry(2001);
            Registry registry = LocateRegistry.getRegistry(IP,2001);
            WorkerFactory w = new WorkerFactoryServer();
            WorkerFactory stub = (WorkerFactory) UnicastRemoteObject.exportObject(w, 0); //PUERTO DE RMI?
            registry.rebind("WorkerFactoryServer", stub);

        } catch (RemoteException re) {
            System.out.println(re);
        }
        /*catch (AlreadyBoundException e) {

        }*/
    }

    /**
     * Devuelve n servidores de calculo si estan en ejecucion.
     * En caso contrario devuelve null y un mensaje que indica que no hay tantos
     * servidores en ejecución
     */
    public ArrayList<Worker> dameWorkers (int n) throws RemoteException {
        ArrayList<Worker> listWorkers = new ArrayList<>();
        try {
            Registry registry = LocateRegistry.getRegistry(IP,2001);
            if (registry.list().length - 1 < n) {
                return null;
            }
            else {
                String[] listNames = registry.list();   //guarda workers registrados
                int i=0; int added = 0;
                while(i<=n && added < n){
                    if (!listNames[i].contains("Factory")) {
                        Worker stub = (Worker) registry.lookup(listNames[i]);
                        listWorkers.add(stub);
                        added++;
                    }
                    i++;
                }
                return listWorkers;
            }
        }
        catch (NotBoundException e) {
            System.out.println("Excepcion: "+e);
            return null;
        }
    }
}
