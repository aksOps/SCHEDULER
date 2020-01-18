package com.ubs.spyda.schedulertcpclient;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@Log4j2
public class SchedulerTcpServerApplication {

    static boolean runningFlag = true;
    static volatile Map<String, ClientHandler> jobClientHandlerMapping = new HashMap<>();

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
                    log.info("A new client is connected : " + s);
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
}

@Log4j2
class ClientHandler extends Thread {
    private static String applicationName = "";
    public final DataOutputStream dos;
    private final DataInputStream dis;
    private final Socket s;
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
                    if (!SchedulerTcpServerApplication.jobClientHandlerMapping.containsKey(applicationName)) {
                        SchedulerTcpServerApplication.jobClientHandlerMapping.put(applicationName, this);
                        log.info(String.format("REGISTERED APPLICATION %s TO %s", applicationName, this.getName()));
                    } else {
                        log.warn("APPLICATION ALREADY REGISTERED");
                        dos.writeUTF(String.format("PLEASE CHECK YOUR APPLICATION NAME, %s IS ALREADY REGISTERED ON SERVER :OVER:", applicationName));
                        this.s.close();
                        this.dis.close();
                        this.dos.close();
                        break;
                    }

                }
            } catch (IOException e) {
                try {
                    System.out.println("Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.interrupt();
                    this.s.close();
                    System.out.println("Connection closed");
                    this.dis.close();
                    this.dos.close();
                    SchedulerTcpServerApplication.jobClientHandlerMapping.remove(applicationName);
                    break;
                } catch (IOException ignored) {
                }
            }
        }

    }
}
