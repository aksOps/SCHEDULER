package com.ubs.spyda.scheduler;

import com.ubs.spyda.scheduler.constant.StatusEnum;
import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import com.ubs.spyda.scheduler.repository.SchedulerRepository;
import com.ubs.spyda.scheduler.service.SchedulerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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
}
