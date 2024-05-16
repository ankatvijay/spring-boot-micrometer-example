package com.ankat.consumer.controller;

import com.ankat.consumer.model.Employee;
import com.ankat.consumer.service.ProducerService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ConsumerController {

    private final ProducerService producerService;

    // PublishToHTTP
    @Observed
    @PostMapping(value = "/web/responseToHTTP", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> responseToHTTP(@RequestBody Employee employee) {
        log.info("#ConsumerController Message: {}", employee);
        Employee responseEmployee = producerService.sendMessage(employee);
        return ResponseEntity.ok(responseEmployee.toString());
    }
}