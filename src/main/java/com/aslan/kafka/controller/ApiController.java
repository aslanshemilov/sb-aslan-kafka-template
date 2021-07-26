package com.aslan.kafka.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
//import com.fasterxml.jackson.databind.JsonSerializer;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.aslan.kafka.config.KafkaProducerConfig;
import com.aslan.kafka.model.*;

@RestController
@RequestMapping("/api")
public class ApiController {
	//private final Logger logger = LoggerFactory.getLogger(getClass());
    private Logger logger = LoggerFactory.getLogger(ApiController.class.getName());
    private ObjectMapper objMapper;
    private Gson gson;
    private static final int DELAY_PER_ITEM_MS = 100;
    private KafkaProducerConfig producerConfig;
   
    // "A Letter to my Nephew"
    private String[] speech = {
            "Well,","you","were","born;","here","you","came,","something","like","fifteen","years","ago,","and","though",
            "your","father","and","mother","and","grandmother,","looking","about","the","streets","through","which","they",
            "were","carrying","you,","staring","at","the","walls","into","which","they","brought","you,","had","every",
            "reason","to","be","heavy-hearted,","yet","they","were","not,","for","here","you","were,","big","James,",
            "named","for","me.","You","were","a","big","baby.","I","was","not.","Here","you","were","to","be","loved.",
            "To","be","loved,","baby,","hard","at","once","and","forever","to","strengthen","you","against","the",
            "loveless","world.","Remember","that.","I","know","how","black","it","looks","today","for","you.","It","looked",
            "black","that","day","too.","Yes,","we","were","trembling.","We","have","not","stopped","trembling","yet,",
            "but","if","we","had","not","loved","each","other,","none","of","us","would","have","survived,","and","now",
            "you","must","survive","because","we","love","you","and","for","the","sake","of","your","children","and",
            "your","children's","children.",
    };

    @Value("${spring.application.name}")
    private String appname;

    private String bootstrapServers = "127.0.0.1:9092";
    private String topic = "customer-topic-1";
    
    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    public ApiController() {
        this.objMapper = new ObjectMapper();
        this.objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.gson = new Gson();
        this.producerConfig = new KafkaProducerConfig();
    }

    @GetMapping("")
    public String Default() {
        return "This is API controller. " + appname;
    }

    @RequestMapping(path = {"/health", "/health.html"}, method = RequestMethod.GET, produces = {MediaType.TEXT_HTML_VALUE, "text/html"})
    public String healthyText() {
        return "health";
    }

    @RequestMapping(path = {"/mono"}, method = RequestMethod.GET, produces = {MediaType.TEXT_HTML_VALUE, "application/json; charset=utf-8"})
    public Mono<String> getMono() {
    	Mono<String> str = Mono.just("Welcome to Web Mono");
        return str;
    }
    
    @RequestMapping(path = {"/flux"}, method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, "application/json; charset=utf-8"})
    public Flux<String> getFlux() {
    	//Flux<String> str = Flux.just("Welcome ", "to ", "Web Flux").delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));
    	Flux<String> str = Flux.just("Welcome ", "to ", "Web Flux").delayElements(Duration.ofSeconds(1)).log();
        return str;
    }
    
    @GetMapping(value="/speech", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getSpeech() {
        return Flux
                .fromArray(speech)
                .delayElements(Duration.ofSeconds(1))
                .repeat()
                .log();
    }
    
    @RequestMapping(path = {"/start", "/starting"},
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/json; charset=utf-8"},
            consumes = {MediaType.ALL_VALUE})
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void start() {
        logger.info("ApiController::start(): {}", "Start");
    }
    
    @RequestMapping(path = {"produce-kafka-template"},
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/json; charset=utf-8"},
            consumes = {MediaType.ALL_VALUE})
    @ResponseBody
    public ResponseEntity<?> ProduceKafkaTemplate() {
        logger.info("ApiController::ProduceKafkaTemplate(): {}", "Start");
        Map<String, Object> config = new HashMap<>();
        try {
        	// create message object		
        	DataModel dataModel = new DataModel();
			dataModel.setName("message");
			//dataModel.setMessage("message-1");
			
			// Or
			//JSONObject msg = new JSONObject();
			//msg.put("message-1", "Aslan is very good guy");
        				
        	// publish message using KafkaTemplate
            /////ListenableFuture<SendResult<Object, Object>> result = kafkaTemplate.send(topic, partition, timestamp, key, data);
        	//ListenableFuture<SendResult<Object, Object>> result = kafkaTemplate.send(topic, gson.toJson(msg));
			//ListenableFuture<SendResult<Object, Object>> result = kafkaTemplate.send(topic, dataModel);
			
			// lets assume we have topic with ten partitions
			for (int i=0; i<10; i++ ) {
				
				// prepare params
				long timestamp = System.currentTimeMillis() + 60 * 1000;
				int partition = i;
				String key = "id_" + Integer.toString(i);
				dataModel.setMessage("message-" + Integer.toString(i));
				
				logger.info("ApiController::ProduceKafkaTemplate(): Key: " + key); // log the key
	            // id_0 is going to partition 1
	            // id_1 partition 0
	            // id_2 partition 2
	            // id_3 partition 0
	            // id_4 partition 2
	            // id_5 partition 2
	            // id_6 partition 0
	            // id_7 partition 2
	            // id_8 partition 1
	            // id_9 partition 2
				
				ListenableFuture<SendResult<Object, Object>> result = kafkaTemplate.send(topic, partition, timestamp, key, dataModel);
	        	
	        	result.addCallback(new ListenableFutureCallback<SendResult<Object, Object>>() {

			        @Override
			        public void onSuccess(SendResult<Object, Object> result) {
			        	logger.info("ApiController::ProduceKafkaTemplate(): Sent message=[" + dataModel + 
			              "] with offset=[" + result.getRecordMetadata().offset() + "]");
			        }
			        @Override
			        public void onFailure(Throwable ex) {
			        	logger.error("ApiController::ProduceKafkaTemplate(): Unable to send message=[" 
			              + dataModel + "] due to : " + ex.getMessage());
			        }
			    });
	        	//kafkaTemplate.metrics();
	        	//assertThat(metrics).isNotNull();
	        	//List<PartitionInfo> partitions = template.partitionsFor(INT_KEY_TOPIC);
	        	//assertThat(partitions).isNotNull();
	        	//assertThat(partitions).hasSize(2);
	        	
	        	/*
	        	 * Here is my approach to add a custom header:
					var record = new ProducerRecord<String, String>(topicName, "Hello World");
					record.headers().add("foo", "bar".getBytes());
					kafkaTemplate.send(record);

	        	 */

			} //end loop      	
        	
        } catch(Exception ex){
            logger.error("ApiController::ProduceKafkaTemplate(): Exception: {}", ex.getMessage());
        } finally {
            logger.info("ApiController::ProduceKafkaTemplate(): {}", "End");
        }

        return new ResponseEntity<>(config, HttpStatus.OK);
    }
    
    @RequestMapping(path = {"produce-kafka-producer"},
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/json; charset=utf-8"},
            consumes = {MediaType.ALL_VALUE})
    @ResponseBody
    public ResponseEntity<?> ProduceKafkaProducer() {
        logger.info("ApiController::ProduceKafkaProducer(): {}", "Start");
        Map<String, Object> response = new HashMap<>();
        try {
        	// create message object		
        	DataModel dataModel = new DataModel();
			dataModel.setName("message");
			//dataModel.setMessage("message-1");
			
			// Or
			//JSONObject msg = new JSONObject();
			//msg.put("message-1", "Aslan is very good guy");
        				
			if(StringUtils.isBlank(topic)){
                logger.error("ApiController::ProduceKafkaProducer(): {}", "The topic is blank");
            }
			
			// create the producer
			Producer<String, Object> kafkaProducer = new KafkaProducer<String, Object>(producerConfig.getProducerProps());
			
			// lets assume we have topic with ten partitions
			for (int i=0; i<10; i++ ) {
				
				// prepare params
				long timestamp = System.currentTimeMillis() + 60 * 1000;
				int partition = i;
				String key = "id_" + Integer.toString(i);
				dataModel.setMessage("message-" + Integer.toString(i));
				
				logger.info("ApiController::ProduceKafkaProducer(): Key: " + key); // log the key
	            // id_0 is going to partition 1
	            // id_1 partition 0
	            // id_2 partition 2
	            // id_3 partition 0
	            // id_4 partition 2
	            // id_5 partition 2
	            // id_6 partition 0
	            // id_7 partition 2
	            // id_8 partition 1
	            // id_9 partition 2
				
				
				/*
	             * Note:
	             * topic − user defined topic name that will appended to record.
	             * partition − partition count
	             * key − The key that will be included in the record.
	             * value − Record contents
	             */
	            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, value) , callback);
	            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, key, value) , callback);
	            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, partition, key, value) , callback);
	            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, partition, timestamp, key, value), callback);
	            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, partition, key, value, headers), callback);
	            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, partition, timestamp, key, value, headers), callback);

	            // create a producer record
	            //ProducerRecord<String, Object> record = new ProducerRecord<String, Object>(
	            //        topic, null, null, gson.toJson(dataModel)
	            //);
	            
	            ProducerRecord<String, Object> producerRecord = new ProducerRecord<String, Object>(
	            		topic, null, timestamp, key, dataModel
	            );
	            //logger.info("ApiController::ProduceKafkaAvroProducer(): dataModel=>{}", dataModel);
	            
	            // send data - synchronous
	            //kafkaProducer.send(record);
	            
	            // send data - asynchronous
	            kafkaProducer.send(producerRecord, new Callback() {
	                @Override
	                public void onCompletion(RecordMetadata metadata, Exception e) {
	                	/*
	                    if (Objects.nonNull(exception)) {
	                        //System.out.println(metadata);
	                        logger.info("ApiController::ProduceKafkaProducer(): Result=>{}", "Success!");
	                        logger.info("ApiController::ProduceKafkaProducer(): metadata=>{}", metadata.toString());
	                    } else {
	                        logger.error("ApiController::ProduceKafkaProducer(): exception=>{}", exception.getMessage());
	                        exception.printStackTrace();
	                    }*/
	                	
	                	 // executes every time a record is successfully sent or an exception is thrown
	                	if (e==null) {
	                		// the record was successfully sent
	                		logger.info("ApiController::ProduceKafkaProducer(): Successfully received the details as: \n" +  
	                                "Topic:" + metadata.topic() + "\n" +  
	                                "Partition:" + metadata.partition() + "\n" +  
	                                "Offset" + metadata.offset() + "\n" +  
	                                "Timestamp" + metadata.timestamp());   
	                	} else {
	                		//logger.error("Can't produce,getting error", exception);
	                		logger.error("ApiController::ProduceKafkaProducer(): exception=>{}", e.getMessage());
	                        e.printStackTrace();
	                	}
	                	
	                }
	            }).get(); // block the .send() to make it synchronous - don't do this in production!

				

			} //end loop   
			
			// flush data
			kafkaProducer.flush();
	        // flush and close producer
			kafkaProducer.close();
        	
        } catch(Exception ex){
            logger.error("ApiController::ProduceKafkaProducer(): Exception: {}", ex.getMessage());
        } finally {
            logger.info("ApiController::ProduceKafkaProducer(): {}", "End");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    
    @RequestMapping(path = {"produce-kafka-avro-producer"},
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/json; charset=utf-8"},
            consumes = {MediaType.ALL_VALUE})
    @ResponseBody
    public ResponseEntity<?> ProduceKafkaAvroProducer() {
        logger.info("ApiController::ProduceKafkaAvroProducer(): {}", "Start");
        Map<String, Object> response = new HashMap<>();
        try {
        	// create message object		
        	DataModel dataModel = new DataModel();
			dataModel.setName("message");
			//dataModel.setMessage("message-1");
			
			// Or
			//JSONObject msg = new JSONObject();
			//msg.put("message-1", "Aslan is very good guy");
        				
			if(StringUtils.isBlank(topic)){
                logger.error("ApiController::ProduceKafkaAvroProducer(): {}", "The topic is blank");
            }
			
			Properties properties = new Properties();
			// create Producer properties
			properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); //Assign host
			properties.put(ProducerConfig.ACKS_CONFIG, "all"); //"1" or "all" //Set acknowledgements for producer requests.
			properties.put(ProducerConfig.RETRIES_CONFIG, "10"); // If the request fails, the producer can automatically retry
			//properties.put("key.serializer", StringSerializer.class.getName());
            //properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
            //properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            //properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
            //properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			//properties.put("value.serializer", ByteArraySerializer.class.getName());
			properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");
			properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerializer");
			properties.put(ProducerConfig.METRICS_RECORDING_LEVEL_CONFIG, "DEBUG");
			//properties.put("value.serializer", StringSerializer.class.getName());
            //properties.put("schema.registry.url", "http://127.0.0.1:8081");
			
			
			//StringSerializer serializer = new StringSerializer();
            
			// create the producer
			KafkaProducer<String, JSONObject> kafkaProducer = new KafkaProducer<String, JSONObject>(properties);
			
			// copied from avro examples
            Customer customer = Customer.newBuilder()
                    .setAge(34)
                    .setAutomatedEmail(false)
                    .setFirstName("Aslan")
                    .setLastName("Shemilov")
                    .setHeight(178f)
                    .setWeight(85f)
                    .build();
            
            logger.info("ApiController::ProduceKafkaAvroProducer(): customer=>{}", customer); //objMapper.convertValue(customer, Customer.class)

            /*
             * Note:
             * topic − user defined topic name that will appended to record.
             * partition − partition count
             * key − The key that will be included in the record.
             * value − Record contents
             */
            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, value) , callback);
            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, key, value) , callback);
            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, partition, key, value) , callback);
            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, partition, timestamp, key, value), callback);
            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, partition, key, value, headers), callback);
            // e.g. producer.send(new ProducerRecord<byte[],byte[]>(topic, partition, timestamp, key, value, headers), callback);

            // create a producer record
            ProducerRecord<String, JSONObject> producerRecord = new ProducerRecord<String, JSONObject>(
            		topic, gson.fromJson(gson.toJson(customer), JSONObject.class)
            );
            
            //logger.info("ApiController::ProduceKafkaAvroProducer(): customer=>{}", customer);

            
            // send data - asynchronous
            kafkaProducer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {               	
                	 // executes every time a record is successfully sent or an exception is thrown
                	if (exception==null) {
                		// the record was successfully sent
                		logger.info("ApiController::ProduceKafkaAvroProducer(): Successfully received the details as: \n" +  
                                "Topic:" + metadata.topic() + "\n" +  
                                "Partition:" + metadata.partition() + "\n" +  
                                "Offset" + metadata.offset() + "\n" +  
                                "Timestamp" + metadata.timestamp());   
                	} else {
                		//logger.error("Can't produce,getting error", exception);
                		logger.error("ApiController::ProduceKafkaAvroProducer(): exception=>{}", exception.getMessage());
                        exception.printStackTrace();
                	}
                	
                }
            }).get(); // block the .send() to make it synchronous - don't do this in production!
            
            //kafkaProducer.send(producerRecord);

            kafkaProducer.flush(); // flush data
            kafkaProducer.close(); // flush and close producer
				
        	
        } catch(Exception ex){
            logger.error("ApiController::ProduceKafkaAvroProducer(): Exception: {}", ex.getMessage());
        } finally {
            logger.info("ApiController::ProduceKafkaAvroProducer(): {}", "End");
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

} // end class
