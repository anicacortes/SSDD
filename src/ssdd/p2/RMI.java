package ssdd.p2;


public class RMI {

    public static void main (String[]args) {
        String tipo;
        String IP = "localhost";
        tipo = args[1];
        switch (tipo) {
            //Lanza WorkerServer
            case "-c": if(args.length == 3) {
                //No tiene IP, se pone por defecto
                IP = args[2];
            }
                WorkerServer ws = new WorkerServer(IP);
                ws.registrar();

            //Lanza WorkerFactoryServer
            case "-a": if(args.length == 3) {
                //No tiene IP, se pone por defecto
                IP = args[2];
            }
                WorkerFactoryServer wfs = new WorkerFactoryServer();
                wfs.registrar();

            //Lanza cliente
            case "-u": int min,max,n;
                min = Integer.parseInt(args[2]);
                max = Integer.parseInt(args[3]);
                n = Integer.parseInt(args[4]);
                if(args.length == 6) {
                    IP = args[5];
                }
                Cliente c = new Cliente(min, max, n, IP);
            default:
                System.out.println("Opcion no soportada");
        }
    }
}
