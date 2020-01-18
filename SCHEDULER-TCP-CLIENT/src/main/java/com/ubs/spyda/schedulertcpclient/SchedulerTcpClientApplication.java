package com.ubs.spyda.schedulertcpclient;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@SpringBootApplication
@Log4j2
public class SchedulerTcpClientApplication {
    private static Socket socket = null;
    @Value("${tcp.server.url}")
    String address;
    @Value("${tcp.server.port}")
    String port;
    private DataInputStream input = null;
    private DataOutputStream out = null;

    public static void main(String[] args) {
        SpringApplication.run(SchedulerTcpClientApplication.class, args);
    }

    @Bean
    public void setupConnection() {
        try {
            socket = new Socket(address, Integer.parseInt(port));
            log.info(String.format("Connected :%s", socket));
            input = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("REGISTER_TO_SERVER:CLEANUP");
        } catch (IOException u) {
            System.out.println(u);
        }
        String line = "";
        while (!line.endsWith(":OVER:")) {
            try {
                line = input.readUTF();
                if (!line.endsWith(":OVER:")) {
                    log.info(line);
                } else {
                    log.error(line.replace(":OVER:", "").trim());
                }
            } catch (IOException i) {
                log.warn("Will try to reconnect ");
                setupConnection();
                break;
            }
        }
        try {
            if (line.equals("Over")) {
                out.writeUTF(String.format("CLOSING CONNECTION TO %s", socket));
            }
            input.close();
            out.close();
            socket.close();
        } catch (IOException i) {
            System.out.println(i);
        }
    }
}
