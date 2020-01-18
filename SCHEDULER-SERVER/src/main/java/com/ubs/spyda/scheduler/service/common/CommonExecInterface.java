package com.ubs.spyda.scheduler.service.common;

import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import lombok.Cleanup;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface CommonExecInterface {

    default boolean run(ProcessBuilder processBuilder, SchedulerEntity schedulerEntity) {
        try {
            Process process = processBuilder.start();
            LocalDateTime now = LocalDateTime.now();
            File logLocation = new File(schedulerEntity.getLogLocation() + File.separator + schedulerEntity.getJobName().trim().replace(" ", "_") + File.separator);
            if (!logLocation.exists()) {
                if (!logLocation.mkdirs()) {
                    System.out.println("Unable to Create log Directory, PLease check permission");
                    return false;
                }
            }
            Path path = Paths.get(String.format("%s%s%s.log", logLocation.getAbsolutePath(), File.separator, now.format(DateTimeFormatter.ofPattern(schedulerEntity.getTimeStampPattern()))));
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
}
