package com.ankat.producer.config;

import com.ankat.producer.util.TopicUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(value = TopicProperties.class)
@Configuration(value = "replyingKafkaConfig")
public class ReplyingKafkaConfig {

    private final TopicProperties topicProperties;
    private final ProducerFactory<String, String> producerFactory;
    private final ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory;

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate() {
        ReplyingKafkaTemplate<String, String, String> template = new ReplyingKafkaTemplate<>(producerFactory, concurrentMessageListenerContainer());
        template.setObservationEnabled(true);
        template.setSharedReplyTopic(true);
        return template;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> concurrentMessageListenerContainer() {
        ConcurrentMessageListenerContainer<String, String> repliesContainer = concurrentKafkaListenerContainerFactory.createContainer(TopicUtil.getScenarioTopicName(topicProperties, 1, 1));
        repliesContainer.getContainerProperties().setGroupId(TopicUtil.getScenarioTopicGroupName(topicProperties, 1, 1)); // Overrides any `group.id` property provided by the consumer factory configuration
        repliesContainer.setAutoStartup(false);
        return repliesContainer;
    }
}
