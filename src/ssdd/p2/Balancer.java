/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: RMI.java
* TIEMPO: 2h
* DESCRIPCIÓN: Monitor que realiza el reparto de carga y actualiza la lista de los
*       primos sin problemas de exclusion mutua.
*/
package ssdd.p2;

import java.util.ArrayList;

public class Balancer {

    private ArrayList<Integer> primos = new ArrayList<>();
    private int min;
    private int max;
    private int nWorkers;
    private int factor = 200; //Numero de enteros a asignar a cada worker inicialmente

    public Balancer(int min, int max, int n){
        this.min = min;
        this.max = max;
        this.nWorkers = n;
    }

    /**
     * Devuelve la lista de primos que se han calculado
     */
    public synchronized ArrayList<Integer> getPrimos() {
        return primos;
    }

    /**
     * Devuelve true si quedan intervalo de numeros por calcular
     * Devuelve false en caso contrario.
     */
    public synchronized boolean quedaTrabajo() {
        return min<max;
    }

    /**
     * Devuelve una lista con minimo y maximo del subintervalo que
     * va a calcular el cliente
     */
    public synchronized int[] dividirTrabajo(){
        int[] rango = new int[2];
        while(((max-min)/nWorkers)<factor){
            factor=factor/2;
        }
        rango[0]=min;
        rango[1]=min+factor;
        min+=(factor+1);
        return rango;
    }

    /**
     * Añade los elementos de p que son los primos de un subintervalo
     * a la lista que contiene todos los primos
     */
    public synchronized void anadirPrimos(ArrayList<Integer> p) {
        primos.addAll(p);
    }
}
