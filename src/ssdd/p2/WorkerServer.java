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
            registry.bind(name, stub);
            System.out.println("se ha registrado workerserver");
        } catch (RemoteException re) {
            System.out.println();
        }
        catch (AlreadyBoundException e) {

        }
    }

    /**
     * Devuelve los primos que se encuentran en el intervalo min-max
     */
    public ArrayList<Integer> encuentraPrimos(int min, int max) {
        ArrayList<Integer> primos = null;
        if (min<=2) {
            primos.add(2);
            min=3;
        }
        for(int i=min; i<=max; i++){
            for(int j = 2; j*j <=i; i+=2){
                if(i % j != 0){
                    primos.add(i);
                }
            }
        }
        return primos;
    }
}
