package com.ubs.spyda.scheduler.repository;

import com.ubs.spyda.scheduler.pojo.SchedulerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerEntity, Integer> {

    @Query("select U from SchedulerEntity U where jobName=?1")
    SchedulerEntity findByJobName(String jobName);
}
