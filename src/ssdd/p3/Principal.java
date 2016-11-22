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
package ssdd.p3;

import java.io.FileNotFoundException;

public class Principal {

    private static boolean debug;
    private static int idP;
    private static String fichero;

    public static void main (String[]args) {

        //Inicializamos los atributos con los parametros de la ejecucion
        if(args.length == 3 && args[0].equals("-d")) {
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
            switch (idP) {
                case 1:
                    A pA = new A(debug, idP, fichero);
                    pA.lanzarEjecucion();
                    break;
                case 2:
                    B pB = new B(debug, idP, fichero);
                    pB.lanzarEjecucion();
                    break;
                case 3:
                    C pC = new C(debug, idP, fichero);
                    pC.lanzarEjecucion();
                    break;
            }
        }
        catch(FileNotFoundException e) {
            System.err.println("El fichero " + fichero + " no existe.");
        }
    }
}
