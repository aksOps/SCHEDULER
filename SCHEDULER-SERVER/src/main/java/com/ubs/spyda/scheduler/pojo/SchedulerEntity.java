package com.ubs.spyda.scheduler.pojo;


import com.ubs.spyda.scheduler.constant.JobType;
import com.ubs.spyda.scheduler.constant.TimezoneEnum;
import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "scheduler_entity", uniqueConstraints = {
        @UniqueConstraint(columnNames = "job_name", name = "uki_scheduler_entity_job_name")}
)
public class SchedulerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NonNull
    @Column(name = "job_name")
    private String jobName;

    @NonNull
    private String cronExpression;

    @NonNull
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @NonNull
    private String appName;

    @NonNull
    private String functionToTrigger;

    @NonNull
    @Enumerated(EnumType.STRING)
    private TimezoneEnum timezoneEnum;

    @NonNull
    @Builder.Default
    private boolean isActive = true;

    @NonNull
    @Builder.Default
    private String dependentJobIds = "NONE";

    @NonNull
    @Builder.Default
    private String logLocation = System.getProperty("user.home");

    @NonNull
    @Builder.Default
    private String timeStampPattern = "yyyyMMddHHmmss";

}
