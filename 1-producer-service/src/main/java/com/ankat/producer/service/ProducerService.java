package com.ankat.producer.service;

import com.ankat.producer.config.TopicProperties;
import com.ankat.producer.model.Employee;
import com.ankat.producer.model.PendingRequest;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(value = TopicProperties.class)
@Service
public class ProducerService {

    private final ObservationRegistry registry;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    private final RestTemplate restTemplate;
    private final Tracer tracer;

    @Observed
    public void sendMessage(String traceName, String topicName, String json) throws ExecutionException, InterruptedException {
        log.info("#trace_id: {}", tracer.currentSpan().context().spanId().getBytes());
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, UUID.randomUUID().toString(), json);
        record.headers().add(new RecordHeader("trace_id", tracer.currentSpan().context().spanId().getBytes()));
        record.headers().add(new RecordHeader("trace_name", traceName.getBytes()));
        SendResult<String, String> result = kafkaTemplate.send(record).get();
        log.info("Message sent successfully for the key: {} and value: {} on topic: {} in partition: {}", result.getProducerRecord().key(), result.getProducerRecord().value(), result.getRecordMetadata().topic(), result.getRecordMetadata().partition());
    }

    @Observed
    public String sendAsyncMessage(String traceName, String topicName, String json) throws ExecutionException, InterruptedException, TimeoutException {
        log.info("#trace_id: {}", tracer.currentSpan().context().spanId().getBytes());
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, UUID.randomUUID().toString(), json);
        record.headers().add(new RecordHeader("trace_id", tracer.currentSpan().context().spanId().getBytes()));
        record.headers().add(new RecordHeader("trace_name", traceName.getBytes()));

        RequestReplyFuture<String, String, String> replyFuture = replyingKafkaTemplate.sendAndReceive(record);
        SendResult<String, String> sendResult = replyFuture.getSendFuture().get(10, TimeUnit.SECONDS);
        log.info("Sent ok: {}", sendResult.getRecordMetadata());
        ConsumerRecord<String, String> consumerRecord = replyFuture.get(10, TimeUnit.SECONDS);
        log.info("Return value: {}", consumerRecord.value());
        return consumerRecord.value();
    }

    @Observed
    public String sendMessage(Employee employee) {
        log.info("#trace_id: {}", tracer.currentSpan().context().spanId().getBytes());
        HttpEntity<Employee> httpEntity = new HttpEntity<>(employee);
        return restTemplate.postForObject("http://localhost:7102/micrometer/web/responseToHTTP", httpEntity, String.class);
    }
}