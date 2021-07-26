package com.aslan.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.stereotype.Service;

import com.aslan.kafka.model.*;

@Service
public class KafkaConsumer {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	public KafkaConsumer() {
		// TODO Auto-generated constructor stub
	}
	
	
	//@KafkaListener(topics = "customer-topic-1", groupId = "group_json", containerFactory = "userKafkaListenerFactory")
    public void consumeJson(DataModel dataModel) {
		logger.info("KafkaConsumer::consume(customer-topic-1):Consumed JSON Message: " + dataModel.getName() + ". Message: "+ dataModel.getMessage());
    }
	

//	@KafkaListener(topics = {"customer-topic-1", "customer-topic-2"}, containerFactory = "kafkaListenerContainerFactory")
//    public void consume(String message) {
//		logger.info("KafkaConsumer::consume(customer-topic-2): Consumed message: " + message);
//    }
	
	/*
	 * Since the initialOffset has been set to 0 in this listener, all the previously consumed messages from partitions 0 and 3 will be re-consumed every time this listener is initialized.
	 * If we don't need to set the offset, we can use the partitions property of @TopicPartition annotation to set only the partitions without the offset:
	 */
    /*
	@KafkaListener(topics = "customer-topic-2", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    @KafkaListener(
			topicPartitions = @TopicPartition(topic = "customer-topic-1", 
			//partitions = { "0", "1", "2", "3" }, // either this
			partitionOffsets = { // or this
					@PartitionOffset(partition = "0", initialOffset = "0"), 
				    @PartitionOffset(partition = "3", initialOffset = "0")	
			}),
			groupId = "group_json", containerFactory = "userKafkaListenerFactory")
	*/
    //@KafkaListener(topics = "customer-topic-1", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message) {
		logger.info("KafkaConsumer::consume(customer-topic-1): Consumed message: " + message);
    } 

} //end class
