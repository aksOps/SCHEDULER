package com.ubs.spyda.schedulertcpclient;

import com.ubs.spyda.schedulertcpclient.common.TaskRunner;
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
public class SchedulerTcpClientApplication implements TaskRunner {
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
            log.error(u);
        }
        String line = "";
        while (!line.endsWith(":OVER:")) {
            try {
                line = input.readUTF();
                if (line.endsWith(":OVER:")) {
                    log.error(line.replace(":OVER:", "").trim());
                } else if (line.startsWith("HEALTH : ")) {
                    log.debug(line);
                } else {
                    if (this.exec(line)) {
                        log.info("Job Completed successfully");
                        out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF("Job Completed Successfully");
                    } else {
                        out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF("Job Failed");
                    }
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
            log.error(i);
        }
    }
}
