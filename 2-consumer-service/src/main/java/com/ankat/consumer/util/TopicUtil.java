package com.ankat.consumer.util;

import com.ankat.consumer.config.TopicProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TopicUtil {
    public static String getScenarioTopicName(TopicProperties topicProperties, int scenarioIndex, int topicIndex){
        List<TopicProperties.Scenario> scenarios = topicProperties.getScenarios().stream().toList();
        return scenarios.get(scenarioIndex).getScenario().get(topicIndex).getName();
    }

    public static String getScenarioTopicGroupName(TopicProperties topicProperties, int scenarioIndex, int topicIndex){
        List<TopicProperties.Scenario> scenarios = topicProperties.getScenarios().stream().toList();
        return scenarios.get(scenarioIndex).getScenario().get(topicIndex).getConsumerGroup();
    }
}