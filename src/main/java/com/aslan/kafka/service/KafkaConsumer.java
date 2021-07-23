package com.aslan.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.aslan.kafka.model.*;

@Service
public class KafkaConsumer {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	public KafkaConsumer() {
		// TODO Auto-generated constructor stub
	}
	
	@KafkaListener(topics = "customer-topic-1", groupId = "group_json", containerFactory = "userKafkaListenerFactory")
    public void consumeJson(DataModel dataModel) {
		logger.info("KafkaConsumer::consume(customer-topic-1):Consumed JSON Message: " + dataModel.getName() + ". Message: "+ dataModel.getMessage());
    }
	
	@KafkaListener(topics = "customer-topic-2", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message) {
		logger.info("KafkaConsumer::consume(customer-topic-2): Consumed message: " + message);
    }

} //end class
