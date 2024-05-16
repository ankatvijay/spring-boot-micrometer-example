package com.ankat.consumer.listener;

import ch.qos.logback.core.testUtil.RandomUtil;
import com.ankat.consumer.entity.Trace;
import com.ankat.consumer.model.Employee;
import com.ankat.consumer.repository.CommitRepository;
import com.ankat.consumer.service.ProducerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ConsumerListener {

    private final CommitRepository repository;
    private final ObservationRegistry registry;
    private final ProducerService producerService;
    private final Tracer tracer;
    private final ObjectMapper objectMapper;

    // PublishToConsume
    @Observed
    @KafkaListener(topics = "${topic.scenarios[0].scenario.[0].name}", groupId = "${topic.scenarios[0].scenario.[0].consumer-group}")
    public void sendMessageToLog(@Payload ConsumerRecord<String, String> consumerRecord) {
        String trace_name = new String(Arrays.stream(consumerRecord.headers().toArray()).filter(header -> header.key().equals("trace_name")).findFirst().get().value(), StandardCharsets.UTF_8);
        log.info("Message consumeFromTopic successfully for the key: {} and value: {} on topic: {} in partition: {} with header:{} and Time to consume {} ms", consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.partition(), trace_name, System.currentTimeMillis() - consumerRecord.timestamp());
    }

    @Observed
    @KafkaListener(topics = "${topic.scenarios[1].scenario.[0].name}", groupId = "${topic.scenarios[1].scenario.[0].consumer-group}")
    @SendTo(value = "${topic.scenarios[1].scenario.[1].name}")
    public Message<String> sendMessageToProducer(@Payload ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        log.info("Message consumeFromAsync successfully for the key: {} and value: {} on topic: {} in partition: {}", consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.partition());
        Employee employee = objectMapper.readValue(consumerRecord.value(), Employee.class);
        employee.setEmpAddress("Stockholm");
        return MessageBuilder
                .withPayload(employee.toString())
                .copyHeaders(Arrays.stream(consumerRecord.headers().toArray()).collect(Collectors.toMap(Header::key, Header::value)))
                .build();
    }

    // PublishToDatabase
    @Observed
    @KafkaListener(topics = "${topic.scenarios[2].scenario.[0].name}", groupId = "${topic.scenarios[2].scenario.[0].consumer-group}")
    public void sendMessageToDatabase(@Payload ConsumerRecord<String, String> consumerRecord) {
        String trace_name = new String(Arrays.stream(consumerRecord.headers().toArray()).filter(header -> header.key().equals("trace_name")).findFirst().get().value(), StandardCharsets.UTF_8);
        log.info("Message consumeFromTopic successfully for the key: {} and value: {} on topic: {} in partition: {} with header:{} and Time to consume {} ms", consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.partition(), trace_name, System.currentTimeMillis() - consumerRecord.timestamp());
        Trace traceSaved = repository.save(new Trace(tracer.currentSpan().context().spanId(), trace_name, consumerRecord.value()));
        log.info("Trace message is '{}'", traceSaved);
    }

    // PublishToCustomObservationLog
    @KafkaListener(topics = "${topic.scenarios[3].scenario.[0].name}", groupId = "${topic.scenarios[3].scenario.[0].consumer-group}")
    public void sendMessageToCustomObservationLog(@Payload ConsumerRecord<String, String> consumerRecord) {
        String trace_name = new String(Arrays.stream(consumerRecord.headers().toArray()).filter(header -> header.key().equals("trace_name")).findFirst().get().value(), StandardCharsets.UTF_8);
        var observation = Observation.createNotStarted(trace_name, registry).start();
        try (var ignored = observation.openScope()) {
            long now = System.currentTimeMillis();
            log.info("Message consumeFromTopic successfully for the key: {} and value: {} on topic: {} in partition: {} with header:{} and Time to consume {} ms", consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.partition(), trace_name, now - consumerRecord.timestamp());
            Trace commitSaved = repository.save(new Trace(tracer.currentSpan().context().spanId(),trace_name, consumerRecord.value()));
            observation.highCardinalityKeyValue("commit.message", consumerRecord.value());
            observation.event(Observation.Event.of("event-consumed"));
            log.info("Trace message is '{}'", commitSaved);
        } finally {
            observation.stop();
        }
    }
}