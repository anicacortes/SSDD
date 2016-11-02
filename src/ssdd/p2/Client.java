/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: Cliente.java
* TIEMPO: 3h
* DESCRIPCIÓN: Cliente que pide n servidores de calculo al servidor de asignacion, divide el intervalo en varios y
*       pide a los diferentes workers que calcules los primos de los subintervalos
*/
package ssdd.p2;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Client {
    private int min;
    private int max;
    private int n;
    private String IP;

        public Client(int min, int max, int n, String IP) {
            this.min = min;
            this.max = max;
            this.n = n;
            this.IP = IP;
        }

    /**
     * Realiza la interacción del cliente con los servidores de asignación
     * para pedir lso servidores de calculo, realiza la particion del intervalo
     * de numeros y se los pasa a los diferentes servidores de calculo
     * para que cada uno realice el calculo de un subintervalo
     */
    public void interaccion() {
            try {
                Registry registry = LocateRegistry.getRegistry(IP,2001);
                WorkerFactory stub = (WorkerFactory) registry.lookup("WorkerFactoryServer");

                ArrayList<Worker> listWorkers = stub.dameWorkers(n);
                if(listWorkers == null){
                    System.out.println("No hay suficientes workers");
                    //fin
                }
                else{
                    System.out.println("antes de dividir, se han encontrado los workers");
                    Balancer b = new Balancer(min,max,n); //Dividira la carga
                    ArrayList<Thread> t = new ArrayList<>();
                    int i=0;
                    while (i<n){
                        System.out.println("Lanzamos thread " + i);
                        t.add(new Thread(new ClientRunnable(b,listWorkers.get(i),i)));
                        t.get(i).start();
                        i++;
                    }

                    for(int j=0; j<t.size();j++){
                        t.get(j).join();
                    }

                    ArrayList<Integer> primos = b.getPrimos();
                    Collections.sort(primos);   //Ordenar la lista
                    System.out.println("Lista de primos en el intervalo: ");
                    for(int j = 0; j<primos.size(); j++){
                        System.out.printf(primos.get(j)+", ");
                    }

                }
            } catch (Exception e) {
                System.err.println("Client exception: " + e.toString());
                e.printStackTrace();
            }
        }

}
