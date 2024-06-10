package com.example.springboot;

import java.text.MessageFormat;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.jms.JmsHealthIndicator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableJms
@Slf4j
public class JmsConfig {

	@Autowired
	private ArtemisConfig artemisConfig;

	// JMS HEALTH CHECK (CUSTOM)
	
	@Bean
	public JmsHealthIndicator jmsHealthIndicator(ConnectionFactory connectionFactory) {
		return new JmsHealthIndicator(connectionFactory);
	}

	// JMS LISTENER

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerQueueContainerFactory(ConnectionFactory connectionFactory) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setConcurrency(MessageFormat.format("{0}-{1}", artemisConfig.getQueueContainerFactory().getMinConnections(), artemisConfig.getQueueContainerFactory().getMaxConnections()));
		factory.setMessageConverter(messageConverter());
		factory.setErrorHandler(t -> log.error("Listener error handler: ", t));

		return factory;
	}

	@Bean
	public DefaultJmsListenerContainerFactory jmsListenerTopicContainerFactory(ConnectionFactory connectionFactory) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter());
		factory.setPubSubDomain(true); // This parameter in true indicates that the listener is for a topic
		factory.setErrorHandler(t -> log.error("Listener error handler: ", t));

		return factory;
	}

	// JMS TEMPLATE

	@Bean
	public JmsTemplate topicJmsTemplate(ConnectionFactory connectionFactory) {
		JmsTemplate template = new JmsRetryTemplate(connectionFactory);
		template.setPubSubDomain(true);
		template.setMessageConverter(messageConverter());
		return template;
	}

	@Bean
	public JmsMessagingTemplate topicJmsMessagingTemplate(@Qualifier("topicJmsTemplate") JmsTemplate topicJmsTemplate) {
		return new JmsMessagingTemplate(topicJmsTemplate);
	}

	@Bean
	public JmsTemplate queueJmsTemplate(ConnectionFactory connectionFactory) {
		JmsTemplate template = new JmsRetryTemplate(connectionFactory);
		template.setMessageConverter(messageConverter());
		return template;
	}

	@Bean
	public JmsMessagingTemplate queueJmsMessagingTemplate(@Qualifier("queueJmsTemplate") JmsTemplate queueJmsTemplate) {
		return new JmsMessagingTemplate(queueJmsTemplate);
	}

	// MESSAGE CONVERTER

	@Bean
	public MessageConverter messageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		converter.setObjectMapper(objectMapper());
		return converter;
	}

	@SuppressWarnings("deprecation")
	private ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}

	@Component
	@ConfigurationProperties(prefix = "spring.artemis")
	@Getter
	@Setter
	public static class ArtemisConfig {

		private ContainerFactory queueContainerFactory = new ContainerFactory(10, 50);

		@Getter
		@Setter
		@AllArgsConstructor
		public static class ContainerFactory {
			private int minConnections;
			private int maxConnections;
		}

	}

}
