package com.aslan.kafka;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class Application implements CommandLineRunner{
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	public static void main(String[] args) {
		System.out.printf("%s\n", "sb-aslan-kafka-template::main(): STARTING THE APPLICATION");
        //SpringApplication.run(Application.class, args);

        SpringApplication app = new SpringApplication(Application.class);
        app.setWebApplicationType(WebApplicationType.REACTIVE);

        String APP_CURRENT_ENV = "local";
        if(System.getenv("work.environment")!=null) {
            String env_value = System.getenv("work.environment").toLowerCase();
            if(env_value.trim().equals("dev")) {
                APP_CURRENT_ENV = "dev";
            }

            System.out.printf("%s\n", "sb-aslan-kafka-template::main(): work.environment: " + env_value);
        }

        // set active profile
        System.setProperty("spring.profiles.active", APP_CURRENT_ENV);

        // current directory
        System.out.printf("%s\n", "sb-aslan-kafka-template::main(): Current Directory= " + System.getProperty("user.dir"));

        // Disabling restart: Make this false when you push to cloud (means on cloud), otherwise comment it for local use
        //System.setProperty("spring.devtools.restart.enabled", "false");

        // run app
        app.run(args);
        System.out.printf("%s\n", "sb-aslan-kafka-template::main(): APPLICATION FINISHED");
        System.out.printf("%s\n", "sb-aslan-kafka-template::main(): env(): " + APP_CURRENT_ENV);
        
        logger.error("Error level is On");
        logger.warn("Warn level is On");
        logger.info("Info level is On");
        logger.debug("Debug level is On");
        logger.trace("Trace level is On");
        
	}
	
	
	@Override
    public void run(String... args) {
        System.out.printf("%s\n", "sb-aslan-kafka-template::run(): EXECUTING => command line runner");
        try {

        } catch (Exception ex) {
            System.out.printf("%s\n", "sb-aslan-kafka-template::run(): Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
	
	
	/*
	@Bean
	 CommandLineRunner start(){
	    return args -> {
	      System.out.println("Hello World!");
	    };
	 }
	 */

	/*
	@Bean
	//@Profile("default") // Don't run from test(s)
	public ApplicationRunner runner(/*KafkaTemplate<String, String> template) {
		return args -> {
			System.out.printf("%s\n", "sb-aslan-kafka-template::runner(): EXECUTING => command line runner");
			System.in.read();
	        try {

	        } catch (Exception ex) {
	            System.out.printf("%s\n", "sb-aslan-kafka-template::runner(): Exception: " + ex.getMessage());
	            ex.printStackTrace();
	        }
		};
	}*/


} //end class

