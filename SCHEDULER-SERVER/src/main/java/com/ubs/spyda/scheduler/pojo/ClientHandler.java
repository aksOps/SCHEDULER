package com.ubs.spyda.scheduler.pojo;

import com.ubs.spyda.scheduler.SchedulerApplication;
import lombok.extern.log4j.Log4j2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Log4j2
public class ClientHandler extends Thread {
    private static String applicationName = "";
    public final DataOutputStream dos;
    final Socket s;
    private final DataInputStream dis;
    private DateFormat forDate = new SimpleDateFormat("yyyy/MM/dd");
    private DateFormat forTime = new SimpleDateFormat("hh:mm:ss");

    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {
        String received;
        while (SchedulerApplication.runningFlag) {
            try {
                received = dis.readUTF();
                Date date = new Date();
                switch (received) {
                    case "Date":
                        log.info(forDate.format(date));
                        break;
                    case "Time":
                        log.info(forTime.format(date));
                        break;
                    default:
                        log.info(received);
                        break;
                }
                if (received.startsWith("REGISTER_TO_SERVER")) {
                    applicationName = received.split(":")[1];
                    List<ClientHandler> clientHandlers = new ArrayList<>();
                    if (SchedulerApplication.jobClientHandlerMapping.containsKey(applicationName)) {
                        clientHandlers = SchedulerApplication.jobClientHandlerMapping.get(applicationName);
                    }
                    clientHandlers.add(this);
                    SchedulerApplication.jobClientHandlerMapping.put(applicationName, clientHandlers);
                    log.info(String.format("REGISTERED APPLICATION %s TO %s", applicationName, this.getName()));
                    break;
                }
            } catch (IOException e) {
                try {
                    log.info("Client " + this.s + " sends exit...");
                    log.info("Closing this connection.");
                    this.interrupt();
                    this.s.close();
                    log.info("Connection closed");
                    this.dis.close();
                    this.dos.close();
                    ClientHandler clientHandlerToRemove = null;
                    SchedulerApplication.jobClientHandlerMapping.get(applicationName).forEach(clientHandler -> log.info(clientHandler.getName()));
                    for (ClientHandler i : SchedulerApplication.jobClientHandlerMapping.get(applicationName)) {
                        if (i.getName().equals(this.getName())) {
                            clientHandlerToRemove = i;
                            break;
                        }
                    }
                    SchedulerApplication.jobClientHandlerMapping.get(applicationName).remove(clientHandlerToRemove);
                    SchedulerApplication.jobClientHandlerMapping.get(applicationName).forEach(clientHandler -> log.info(clientHandler.getName()));
                    break;
                } catch (IOException ignored) {
                }
            }
        }
    }
}
