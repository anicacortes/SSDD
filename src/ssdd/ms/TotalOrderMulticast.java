/*
* AUTOR: Ana Roig Jimenez
* NIA: 686329
* AUTOR: Beatriz Pérez Cancer
* NIA: 683546
* FICHERO: TotalOrderMulticast.java
* TIEMPO: 8h
* DESCRIPCIÓN: Implementacion del algoritmo Ricart-Agrawala utilizando los relojes de Lamport
*           para el envio y recepcion de mensajes multicast.
*/
package ssdd.ms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TotalOrderMulticast {

    private MessageSystem msystem;
    private int pid;

    private boolean accesoSC;
    private ArrayList<Integer> Pretrasados;
    private final ACK ACK;
    private final REQ REQ;
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
        ACK = new ACK();
        REQ = new REQ();
    }

    public void sendMulticast(Serializable message) {
        mutex.lock();
        //si un mensaje ya se esta intentando enviar, espero
        try {
            while (accesoSC) {
                esperandoEnviar.await();
            }
            accesoSC = true;
            //envia peticion a todos para ver si puede entrar
            msystem.sendMulticast(new REQ());
            //espera a recibir todos acks
            while (ackPendientes > 0) {
                esperandoACKs.await();
            }

            msystem.setLamportClock(maxLamportClock);
            msystem.sendMulticast(message);
            //cuando envia mensaje --> restarua valor ack pendientes
            System.out.println("tamaño retrasados: " + Pretrasados.size());
            for (int i = 0; i < Pretrasados.size(); i++) {
                if (Pretrasados.get(i) != pid) {
                    System.out.println("despierto a " + Pretrasados.get(i));
                    msystem.send(Pretrasados.get(i), new ACK());
                }
            }
            Pretrasados.removeAll(Pretrasados); //elimina los elementos pendientes
            ackPendientes = msystem.getProcess() - 1;
            accesoSC = false;
            msystem.setLamportClock(msystem.getLamportClock() + 1);
            esperandoEnviar.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            mutex.unlock();
        }
    }

    public Envelope receiveMulticast() {
        while (true) {
            Envelope e = msystem.receive();
            System.out.println("RECIBE msg "+e.getPayload()+" de "+e.getSource()+" con reloj "+e.getLamportClock()+" LC es "+msystem.getLamportClock());
            if (e.getPayload() instanceof REQ) {
                //Si el proceso esta/espera acceder a SC
                if (accesoSC) {
                    if (e.getLamportClock() < msystem.getLamportClock()) {
                        msystem.send(e.getSource(), new ACK());
                    } else if (e.getLamportClock() > msystem.getLamportClock()) {
                        Pretrasados.add(e.getSource());
                        System.out.println("encolando: "+e.getSource());
                    } else {
                        if (e.getSource() <= pid) {
                            msystem.send(e.getSource(), new ACK());
                        } else {
                            Pretrasados.add(e.getSource());
                            System.out.println("encolando: "+e.getSource());
                        }
                    }
                } else {
                    msystem.send(e.getSource(), new ACK());
                }
            } else if (e.getPayload() instanceof ACK) {
                ackPendientes--;
                if(ackPendientes==0){
                    mutex.lock();
                    esperandoACKs.signal();
                    mutex.unlock();
                }
            }
            else{
                //Nos quedamos con el valor mayor entre el max reloj y el del mensaje
                if(e.getSource()!=pid) {
                    if (e.getLamportClock() >= maxLamportClock) {
                        maxLamportClock = e.getLamportClock() + 1;
                    } else {
                        maxLamportClock = msystem.getLamportClock() + 1;
                    }
                    if (!accesoSC) {
                        msystem.setLamportClock(maxLamportClock);
                    }
                }
            }
            return e;
        }
    }
}