package ssdd.ms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TotalOrderMulticast {

    private MessageSystem msystem;
    private int pid;

    private boolean accesoSC;
    private ArrayList<Integer> Pretrasados;
    private final String ACK = "ACK";
    private final String REQ = "REQ";
    private int ackPendientes;
    private int maxLamportClock;

    private Lock mutex = new ReentrantLock();

    private Condition esperandoACKs = mutex.newCondition();
    private Condition esperandoEnviar = mutex.newCondition();


    public TotalOrderMulticast(MessageSystem ms, int idP){
        msystem = ms;
        pid = idP;
        Pretrasados = new ArrayList<>();
        accesoSC = false;
        ackPendientes = msystem.getProcess()-1;
        maxLamportClock=1;
    }

    public void sendMulticast(Serializable message){
        mutex.lock();
        //si un mensaje ya se esta intentando enviar, espero
        try {
            while(accesoSC){
                System.out.println("esta intentando acceder mensaje : "+((MessageValue)message).getValue());
                esperandoEnviar.await();
            }
            accesoSC = true;
            //envia peticion a todos para ver si puede entrar
            msystem.sendMulticast(new MessageValue(REQ));
            //espera a recibir todos acks
            while(ackPendientes>0){
                System.out.println("espero ACK en bucle");
                esperandoACKs.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("me han despertado pq he recibido acks");
        msystem.sendMulticast(message);
        //cuando envia mensaje --> restarua valor ack pendientes
        for(int i=0; i<Pretrasados.size(); i++){
            if(Pretrasados.get(i)!=pid){
                msystem.send(Pretrasados.get(i),new MessageValue(ACK));
                Pretrasados.remove(i);
            }
        }
        ackPendientes = msystem.getProcess()-1;
        accesoSC = false;
        msystem.setLamportClock(maxLamportClock+1);
        esperandoEnviar.signal();
        mutex.unlock();
    }

    public Envelope receiveMulticast() {
        while (true) {
            Envelope e = msystem.receive();
            String m = ((MessageValue) e.getPayload()).getValue();
            System.out.println("LEE mensaje: "+m+" ClockLocal: "+msystem.getLamportClock()+" ClockMsg "+
                    e.getLamportClock());
            if (m.equals(REQ)) {
                //Si el proceso esta/espera acceder a SC
                if (accesoSC) {
                    System.out.println("quiero entrar y me han pedido REQ");
                    if (e.getLamportClock() < msystem.getLamportClock()) {
                        System.out.println("doy ACK a pequeño");
                        msystem.send(e.getSource(), new MessageValue(ACK));
                    } else if (e.getLamportClock() > msystem.getLamportClock()) {
                        System.out.println("me toca a mi");
                        Pretrasados.add(e.getSource());
                    } else {
                        if (e.getSource() <= pid) {
                            System.out.println("pid mayor el mio");
                            msystem.send(e.getSource(), new MessageValue(ACK));
                        } else {
                            System.out.println("pid mayor el suyo");
                            Pretrasados.add(e.getSource());
                        }
                    }
                } else {
                    System.out.println("me piden entrar y yo no quiero entrar");
                    msystem.send(e.getSource(), new MessageValue(ACK));
                }
            } else if (m.equals(ACK)) {
                System.out.println("recibo ACK, bien uno menos");
                ackPendientes--;
                System.out.println("ACKpend: "+ackPendientes);
                if(ackPendientes==0){
                    mutex.lock();
                    esperandoACKs.signal();
                    mutex.unlock();
                }
            }
            else{
                System.out.println("recalcular rejoj");
                //Aumentamos el valor del reloj si el mensaje no es req ni ack
                if(e.getLamportClock()>maxLamportClock){
                    maxLamportClock=e.getLamportClock();
                }
            }
            //else{
                return e;
            //}
        }
    }
}