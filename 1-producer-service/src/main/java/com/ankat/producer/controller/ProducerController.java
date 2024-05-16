package com.ankat.producer.controller;

import com.ankat.producer.config.TopicProperties;
import com.ankat.producer.model.Employee;
import com.ankat.producer.service.ProducerService;
import com.ankat.producer.util.TopicUtil;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProducerController {
    private final ProducerService producerService;
    private final TopicProperties topicProperties;

    @Observed
    @PostMapping(value = "/kafka/publishToConsume", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> publishToConsume(HttpEntity<String> json) throws ExecutionException, InterruptedException {
        producerService.sendMessage("publishToConsume", TopicUtil.getScenarioTopicName(topicProperties, 0, 0), json.getBody());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/kafka/publishToAsync", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> publishToAsync(HttpEntity<String> json) throws ExecutionException, InterruptedException, TimeoutException {
        String response = producerService.sendAsyncMessage("publishToAsync", TopicUtil.getScenarioTopicName(topicProperties, 1, 0), json.getBody());
        return ResponseEntity.ok(response);
    }

    @Observed
    @PostMapping(value = "/kafka/publishToDatabase", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> publishToDatabase(HttpEntity<String> json) throws ExecutionException, InterruptedException {
        producerService.sendMessage("publishToDatabase", TopicUtil.getScenarioTopicName(topicProperties, 2, 0), json.getBody());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Observed
    @GetMapping(value = "/web/publishToHTTP", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> publishToHTTP(@RequestBody Employee employee) {
        log.info("#ProducerController Message: {}", employee);
        String response = producerService.sendMessage(employee);
        return ResponseEntity.ok(response);
    }

    @Observed
    @PostMapping(value = "/kafka/publishToCustomObservationLog", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> publishToCustomObservationLog(HttpEntity<String> json) throws ExecutionException, InterruptedException {
        producerService.sendMessage("publishToCustomObservationLog", TopicUtil.getScenarioTopicName(topicProperties, 3, 0), json.getBody());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}


