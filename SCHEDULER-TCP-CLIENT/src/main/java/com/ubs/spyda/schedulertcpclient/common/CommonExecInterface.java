package com.ubs.spyda.schedulertcpclient.common;


import lombok.Cleanup;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public interface CommonExecInterface {

    default boolean run(ProcessBuilder processBuilder, String s) {
        try {
            Process process = processBuilder.start();
            LocalDateTime now = LocalDateTime.now();
            String getLogLocation = this.getValueForString(s, "logLocation");
            String getJobName = this.getValueForString(s, "jobName");
            String getTimeStampPattern = this.getValueForString(s, "timeStampPattern");
            File logLocation = new File(getLogLocation + File.separator + getJobName.trim().replace(" ", "_") + File.separator);
            if (!logLocation.exists()) {
                if (!logLocation.mkdirs()) {
                    System.out.println("Unable to Create log Directory, Please check permission");
                    return false;
                }
            }
            Path path = Paths.get(String.format("%s%s%s.log", logLocation.getAbsolutePath(), File.separator, now.format(DateTimeFormatter.ofPattern(getTimeStampPattern))));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            @Cleanup FileWriter fileWriter = new FileWriter(path.toFile());
            @Cleanup PrintWriter printWriter = new PrintWriter(fileWriter);
            String line;
            while ((line = reader.readLine()) != null) {
                printWriter.println(line);
            }
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                return true;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
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
