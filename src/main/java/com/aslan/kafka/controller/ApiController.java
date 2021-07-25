package com.aslan.kafka.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aslan.kafka.model.DataModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ApiController {
	//private final Logger logger = LoggerFactory.getLogger(getClass());
    private Logger logger = LoggerFactory.getLogger(ApiController.class.getName());
    private ObjectMapper objMapper;
    private Gson gson;
    private static final int DELAY_PER_ITEM_MS = 100;
   
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
    
    @RequestMapping(path = {"producer"},
            method = {RequestMethod.GET},
            produces = {MediaType.APPLICATION_JSON_VALUE, "application/json; charset=utf-8"},
            consumes = {MediaType.ALL_VALUE})
    @ResponseBody
    public ResponseEntity<?> producer() {
    	Map<String, Object> config = new HashMap<>();
        logger.info("ApiController::producer(): {}", "Start");
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
				
				logger.info("ApiController::producer(): Key: " + key); // log the key
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
			        	logger.info("Sent message=[" + dataModel + 
			              "] with offset=[" + result.getRecordMetadata().offset() + "]");
			        }
			        @Override
			        public void onFailure(Throwable ex) {
			        	logger.error("Unable to send message=[" 
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
            logger.error("ApiController::producer(): Exception: {}", ex.getMessage());
        } finally {
            logger.info("ApiController::producer(): {}", "End");
        }

        return new ResponseEntity<>(config, HttpStatus.OK);
    }

} // end class
