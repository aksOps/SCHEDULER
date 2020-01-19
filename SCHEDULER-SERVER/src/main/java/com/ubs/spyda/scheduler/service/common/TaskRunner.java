package com.ubs.spyda.scheduler.service.common;


import com.ubs.spyda.scheduler.pojo.ClientHandler;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.ubs.spyda.scheduler.SchedulerApplication.jobClientHandlerMapping;

public interface TaskRunner {

    default boolean exec(String s) throws IOException {
        String application = this.getValueForString(s, "appName");
        sendCommand(application, s);
        return true;
    }

    default void sendCommand(String appName, String message) throws IOException {
        ClientHandler clientHandler = getRandomClientHandler(appName);
        clientHandler.dos.writeUTF(message);
    }

    default ClientHandler getRandomClientHandler(String appName) {
        int length = jobClientHandlerMapping.get(appName).size();
        Random random = new Random();
        List<ClientHandler> clientHandlers = jobClientHandlerMapping.get(appName);
        return clientHandlers.get(random.nextInt(length));
    }

    default String getValueForString(String stringWithNewLine, String fieldToFind) {
        String value = null;
        for (String s : stringWithNewLine.split("\n")) {
            if (s.startsWith(String.format("%s=", fieldToFind))) {
                value = s.split("=")[1];
            }
        }
        return Objects.requireNonNull(value).replaceAll("[\\r\\n]", "");
    }
}
