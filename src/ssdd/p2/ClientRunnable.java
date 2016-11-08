/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: RMI.java
* TIEMPO: 1h
* DESCRIPCIÓN: Thread que ejecuta la llamada para encontrar los primos en un subintervalo mientras queda trabajo pendiente.
*/
package ssdd.p2;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class ClientRunnable implements Runnable{

    private final int client;
    private Balancer b;
    private Worker worker;
    private ArrayList<Integer> primos;

    ClientRunnable(Balancer b, Worker worker,int i){
        this.b = b;
        this.worker = worker;
        this.client = i;
    }
    public void run(){
        try {
            //Si queda rangos por calcular, sigue calculando primos en subintervalos
            long inicioTime = System.currentTimeMillis();
            while (b.quedaTrabajo()){
                int[] rango = b.dividirTrabajo();
                System.out.println("Soy el cliente "+client+" y voy a calcular el intervalo "+rango[0]+" - "+rango[1]);
                primos = worker.encuentraPrimos(rango[0],rango[1]);
                b.anadirPrimos(primos);
            }
            long finalTime = System.currentTimeMillis();
            long diferencia = finalTime - inicioTime;
            System.out.println("Cliente " + client + " ha tardado en toda su ejecución " + diferencia + "milisegundos");
        }catch (RemoteException e){
            System.out.printf("exception: "+e);
        }
    }

}
