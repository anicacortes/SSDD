/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: WorkerServer.java
* TIEMPO: 1h
* DESCRIPCIÓN: Servidor de calculo que realiza el calculo de los primos en el intervalo indicado.
*/
package ssdd.p2;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class WorkerServer implements Worker{
    String IP;
    int contador = 0;

    public WorkerServer(String IP) {
        this.IP = IP;
    }
    public WorkerServer() {    }

    /**
     * Registra en el registroRMI el servidor de calculo que se lanza
     */
    public void registrar () {
        try{
            Registry registry = LocateRegistry.getRegistry(IP,2001);
            Worker w = new WorkerServer();
            Worker stub = (Worker) UnicastRemoteObject.exportObject(w, 0);
            String[] listNames = registry.list();
            contador = listNames.length+1;
            String name = "WorkerServer" + contador;
            registry.rebind(name, stub);
        } catch (RemoteException re) {
            System.out.println(re);
        }
    }

    /**
     * Devuelve los primos que se encuentran en el intervalo min-max
     */
    public ArrayList<Integer> encuentraPrimos(int min, int max) {
        ArrayList<Integer> primos = new ArrayList<>();
        if (min <= 2) {
            primos.add(2);
            min = 3;
        }
        for (int i = min; i <= max; i++) {
            if (esPrimo(i)) {
                primos.add(i);
            }
        }
        System.out.println("He terminado la busqueda de primos con min " + min + " y max " + max);
        return primos;
    }

    private boolean esPrimo(int i) {
        for (int j = 2; j * j <= i; j++) {
            if ((i % j) == 0) {
                return false;
            }
        }
        return true;
    }
}
