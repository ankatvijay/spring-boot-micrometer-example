package com.ankat.producer.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(value = KafkaProperties.class)
@Configuration
public class KafkaConfig {

    private final SslBundles sslBundles;
    private final KafkaProperties kafkaProperties;
    private final MeterRegistry meterRegistry;

    /* Producer Config Start */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(sslBundles));
        //props.put(ProducerConfig.CLIENT_ID_CONFIG, "com.example.tracing-demo-producer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Serdes.String().serializer().getClass().getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Serdes.String().serializer().getClass().getName());
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        final ProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
        factory.addListener(new MicrometerProducerListener<>(meterRegistry)); // adds native Kafka producer metrics
        return factory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        var template = new KafkaTemplate<>(producerFactory());
        template.setObservationEnabled(true);
        return template;
    }
    /* Producer Config End */

    /* Consumer Config Start */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(sslBundles));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass().getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass().getName());
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        ConsumerFactory<String, String> factory = new DefaultKafkaConsumerFactory<>(consumerConfigs());
        factory.addListener(new MicrometerConsumerListener<>(meterRegistry)); // adds native Kafka consumer metrics
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(SslBundles sslBundles) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setConsumerFactory(consumerFactory());
        // Since Spring Kafka 2.3, container fails to start if topic is missing. This check is done by
        // initializing an Admin Client. We disable this to skip checking topic existence.
        factory.setMissingTopicsFatal(false);
        return factory;
    }
    /* Consumer Config End */
}