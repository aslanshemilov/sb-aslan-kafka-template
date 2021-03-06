package com.aslan.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Configuration
public class KafkaProducerConfig {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private String bootstrapServers = "127.0.0.1:9092";
	
	public KafkaProducerConfig() {}
		
	private Map<String, Object> producerConfigs() {		 
		 try {
			 Map<String, Object> props = new HashMap<String, Object>();
			 
			// create Producer properties
			props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); //Assign host
			props.put(ProducerConfig.ACKS_CONFIG, "all"); //"1" or "all" //Set acknowledgements for producer requests.
			props.put(ProducerConfig.RETRIES_CONFIG, "10"); // If the request fails, the producer can automatically retry
	        //props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
	        //props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	        //props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
	        //props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
			return props;
		 } catch(Exception ex){
			 logger.error("KafkaProducerConfig::producerConfigs(): Exception: {}", ex.getMessage());
			 return new HashMap<String, Object>();
	     } 
	}
	
	public final Map<String, Object> getProducerProps() { 
		return producerConfigs(); 
	}
	
	@Bean
    public ProducerFactory<Object, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }


    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

} //end class

