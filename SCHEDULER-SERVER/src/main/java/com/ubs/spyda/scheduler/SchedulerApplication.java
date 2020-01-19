package com.ubs.spyda.scheduler;

import com.ubs.spyda.scheduler.constant.StatusEnum;
import com.ubs.spyda.scheduler.pojo.ClientHandler;
import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import com.ubs.spyda.scheduler.repository.SchedulerRepository;
import com.ubs.spyda.scheduler.service.SchedulerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PreDestroy;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ubs.spyda.scheduler.service.SchedulerService.jobStatusMap;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@Log4j2
public class SchedulerApplication {

    @Autowired
    SchedulerRepository schedulerRepository;

    @Autowired
    SchedulerService schedulerService;

    public static boolean runningFlag = true;
    public static volatile Map<String, List<ClientHandler>> jobClientHandlerMapping = new HashMap<>();

    @Value("${tcp.port}")
    String port;

    private ServerSocket ss = null;

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public void loadScheduler() {
        schedulerRepository.findAll().stream().filter(SchedulerEntity::isActive).forEach(schedulerEntity -> jobStatusMap.put(schedulerEntity.getId(), StatusEnum.STALE));
        schedulerService.loadScheduler();
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
            jobClientHandlerMapping.forEach((s, clientHandlers) -> {
                        clientHandlers.forEach(clientHandler -> {
                            try {
                                clientHandler.dos.writeUTF("HEALTH : PING FROM SERVER FOR HEALTH CHECK");
                            } catch (Exception e) {
                                List<ClientHandler> handlers = jobClientHandlerMapping.get(s);
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