/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: Principal.java
* TIEMPO: 0.5h
* DESCRIPCIÓN: Lanza el proceso indicado según el pid intrducido como parámetro
*           con sus parámetros correspondientes introducidos por el usuario.
*/
package ssdd.p4;

import java.io.FileNotFoundException;

public class Principal {

    private static boolean debug;
    private static int idP;
    private static String fichero;
    private static boolean prueba = false;

    public static void main (String[]args) {

        //Inicializamos los atributos con los parametros de la ejecucion
        if(args.length == 4 && args[0].equals("-d") && args[3].equals("-p")){
            debug = true;
            idP = Integer.parseInt(args[1]);
            fichero = args[2];
            prueba = true;
        }
        else if(args.length == 3 && args[0].equals("-d")) {
            debug = true;
            idP = Integer.parseInt(args[1]);
            fichero = args[2];
        }
        else if(args.length == 2) {
            debug = false;
            idP = Integer.parseInt(args[0]);
            fichero = args[1];
        }
        else {
            System.out.println("Opción no soportada");
        }

        //Elegimos qué proceso se ejecuta según el identificador proporcionado
        try {
            if(prueba){
                /*if(idP==1 || idP==2){
                    EscuchaMsg e = new EscuchaMsg(debug, idP, fichero);
                    e.lanzarEjecucion();
                }else{*/
                    EnvioMasivo e = new EnvioMasivo(debug, idP, fichero);
                    e.lanzarEjecucion();
                //}
            }
            else{
                A pA = new A(debug, idP, fichero);
                pA.lanzarEjecucion();
            }
        }
        catch(FileNotFoundException e) {
            System.err.println("El fichero " + fichero + " no existe.");
        }
    }
}
