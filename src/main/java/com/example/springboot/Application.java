package com.example.springboot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.jms.Destination;
import javax.jms.JMSException;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class Application {
	
	public static final File STORAGE = new File("/tmp/queue");

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			log.info("Let's inspect the beans provided by Spring Boot:");

			final String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			
			for (String beanName : beanNames) {
				log.info("{}", beanName);
			}

		};
	}
	
	@Component
	public class StartupApplicationListenerExample implements ApplicationListener<ContextRefreshedEvent>, JmsListenerConfigurer {

	    @Override 
	    public void onApplicationEvent(ContextRefreshedEvent event) {
	    	if (! STORAGE.exists()) {
	    		log.info("create storage file on {}", STORAGE);
	    		
	    		try {
					STORAGE.createNewFile();
				} catch (IOException e) {
					log.error(null, e);
				}
	    	}
	    }
	    
	    @Override
		public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
	    	try {
				FileUtils.readLines(STORAGE, Charset.defaultCharset()).forEach(queue -> {
					
					final SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
					endpoint.setId(queue);
					endpoint.setDestination(queue);
					endpoint.setMessageListener(message -> {
						try {
							final String body = message.getBody(String.class);
							final Destination jmsDestination = message.getJMSDestination();
							
							log.info("received message on destination: " + jmsDestination);
							log.info("######### on thread {}", Thread.currentThread());
							log.info("{}", body);
							log.info("#########");
							log.info("{}", message);
							log.info("#########");
							
						} catch (JMSException e) {
							log.error(null, e);
						}
					});					
					
					registrar.registerEndpoint(endpoint);		
					
				});
			} catch (IOException e) {
				log.error(null, e);
			}
		}	    
	}	

}
