package com.ankat.consumer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(TopicProperties.class)
@Configuration
public class TopicConfig {

    @Bean
    public KafkaAdmin.NewTopics createKafkaTopics(TopicProperties topicProperties) {
        NewTopic[] topics = topicProperties.getScenarios().stream().flatMap(scenario -> scenario.getScenario().stream()).map(TopicProperties.Topic::getName).map(topic -> TopicBuilder.name(topic).partitions(4).replicas(4).build()).toArray(NewTopic[]::new);
        return new KafkaAdmin.NewTopics(topics);
    }
}