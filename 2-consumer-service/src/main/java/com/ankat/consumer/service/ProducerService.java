package com.ankat.consumer.service;

import com.ankat.consumer.model.Employee;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProducerService {

    @Observed
    public Employee sendMessage(Employee employee) {
        log.info("#ConsumerService Message: {}", employee);
        employee.setEmpAddress("Stockholm");
        return employee;
    }
}