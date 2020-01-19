package com.ubs.spyda.schedulertcpclient;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PreDestroy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
@Log4j2
@EnableScheduling
public class SchedulerTcpServerApplication {

    static boolean runningFlag = true;
    static volatile Map<String, List<ClientHandler>> jobClientHandlerMapping = new HashMap<>();

    @Value("${tcp.port}")
    String port;
    private ServerSocket ss = null;

    public static void main(String[] args) {
        SpringApplication.run(SchedulerTcpServerApplication.class, args);
    }

    @Bean
    public void startTCPServer() {
        Runnable runnable = () -> {
            try {
                ss = new ServerSocket(Integer.parseInt(port));
                while (runningFlag) {
                    Socket s = ss.accept();
                    log.info("A new client connected : " + s);
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    log.info("Assigning new thread for this client");
                    Thread t = new ClientHandler(s, dis, dos);
                    t.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    @PreDestroy
    public void closeSocket() throws IOException {
        runningFlag = false;
        log.info("Closing connection");
        ss.close();
        log.info("Connection closed");
    }

    @Scheduled(fixedDelay = 1000)
    public void checkSocket() {
        try {
            SchedulerTcpServerApplication.jobClientHandlerMapping.forEach((s, clientHandlers) -> {
                        clientHandlers.forEach(clientHandler -> {
                            try {
                                clientHandler.dos.writeUTF("HEALTH : PING FROM SERVER FOR HEALTH CHECK");
                            } catch (Exception e) {
                                List<ClientHandler> handlers = SchedulerTcpServerApplication.jobClientHandlerMapping.get(s);
                                handlers.remove(clientHandler);
                                jobClientHandlerMapping.put(s, handlers);
                            }
                        });
                    }
            );
        } catch (ConcurrentModificationException ignored) {
        }
    }
}

@Log4j2
class ClientHandler extends Thread {
    private static String applicationName = "";
    public final DataOutputStream dos;
    private final DataInputStream dis;
    final Socket s;
    private DateFormat forDate = new SimpleDateFormat("yyyy/MM/dd");
    private DateFormat forTime = new SimpleDateFormat("hh:mm:ss");
    ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {
        String received;
        while (SchedulerTcpServerApplication.runningFlag) {
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
                    if (SchedulerTcpServerApplication.jobClientHandlerMapping.containsKey(applicationName)) {
                        clientHandlers = SchedulerTcpServerApplication.jobClientHandlerMapping.get(applicationName);
                    }
                    clientHandlers.add(this);
                    SchedulerTcpServerApplication.jobClientHandlerMapping.put(applicationName, clientHandlers);
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
                    SchedulerTcpServerApplication.jobClientHandlerMapping.get(applicationName).forEach(clientHandler -> log.info(clientHandler.getName()));
                    for (ClientHandler i : SchedulerTcpServerApplication.jobClientHandlerMapping.get(applicationName)) {
                        if (i.getName().equals(this.getName())) {
                            clientHandlerToRemove = i;
                            break;
                        }
                    }
                    SchedulerTcpServerApplication.jobClientHandlerMapping.get(applicationName).remove(clientHandlerToRemove);
                    SchedulerTcpServerApplication.jobClientHandlerMapping.get(applicationName).forEach(clientHandler -> log.info(clientHandler.getName()));
                    break;
                } catch (IOException ignored) {
                }
            }
        }

    }


}
