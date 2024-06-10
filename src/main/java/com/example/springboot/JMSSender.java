package com.example.springboot;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.JmsDestinationAccessor;
import org.springframework.stereotype.Component;

@Component
public class JMSSender {
	private static final Logger logger = LoggerFactory.getLogger(JMSSender.class);

	private final JmsMessagingTemplate topicJmsMessagingTemplate;
	private final JmsMessagingTemplate queueJmsMessagingTemplate;
	private final MessageConverter messageConverter;

	@Autowired
	public JMSSender(
			@Qualifier("topicJmsMessagingTemplate") JmsMessagingTemplate topicJmsMessagingTemplate,
			@Qualifier("queueJmsMessagingTemplate") JmsMessagingTemplate queueJmsMessagingTemplate,
			MessageConverter messageConverter) {
		this.topicJmsMessagingTemplate = topicJmsMessagingTemplate;
		this.queueJmsMessagingTemplate = queueJmsMessagingTemplate;
		this.messageConverter = messageConverter;
	}

	public void sendToTopic(String topic, String message) {
		logger.debug("Sending to the topic: {} the message: {}", topic, message);
		topicJmsMessagingTemplate.convertAndSend(topic, message);
	}

	public void sendToTopic(String topic, Object message) {
		logger.debug("Sending to the topic: {} the message: {}", topic, message);
		topicJmsMessagingTemplate.convertAndSend(topic, message);
	}

	public void sendToQueue(String queue, Object message) {
		logger.debug("Sending to the queue: {} the message: {}", queue, message);
		queueJmsMessagingTemplate.convertAndSend(queue, message);
	}

	public void sendToQueue(String queue, String message, long delay, TimeUnit timeUnit) {
		logger.debug("Sending to the queue: {} the message: {} with delay: {}", queue, message, delay);
		queueJmsMessagingTemplate.getJmsTemplate().send(queue, session -> {
			Message mex = messageConverter.toMessage(message, session);
			mex.setLongProperty(org.apache.activemq.artemis.api.core.Message.HDR_SCHEDULED_DELIVERY_TIME.toString(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, timeUnit));
			return mex;
		});
	}

	public <M extends JMSMessage> void sendToTopic(String topic, M message) {
		logger.debug("Sending to the topic: {} the message: {}", topic, message);
		topicJmsMessagingTemplate.convertAndSend(topic, message);
	}

	public <M extends JMSMessage> void sendToQueue(String queue, M message) {
		logger.debug("Sending to the queue: {} the message: {}", queue, message);
		queueJmsMessagingTemplate.convertAndSend(queue, message);
	}

	public <M extends JMSMessage> JMSReply sendToQueueSynchronously(String queue, M message) {
		if (JmsDestinationAccessor.RECEIVE_TIMEOUT_INDEFINITE_WAIT == Objects.requireNonNull(queueJmsMessagingTemplate.getJmsTemplate()).getReceiveTimeout()) {
			throw new IllegalArgumentException("Cannot send Synchronously messages without timeout");
		}

		logger.debug("Sending to the queue synchronously: {} the message: {}", queue, message);

		final JMSReply reply = queueJmsMessagingTemplate.convertSendAndReceive(queue, message, JMSReply.class);

		if (reply == null || reply.getType() == null) {
			logger.error("unknown JMS reply type: {}", reply);
		} else if (reply.getType() == JMSReply.Type.ERROR) {
			logger.warn("JMS error reply: {}", reply.getPayload());
		}
		return reply;
	}

	public <M extends JMSMessage> void sendToQueue(String queue, M message, boolean hasPriority) {
		logger.debug("Sending to the queue: {} the message: {}", queue, message);
		queueJmsMessagingTemplate.getJmsTemplate().send(queue, session -> {
			ObjectMessage mex = session.createObjectMessage(message);
			mex.setJMSRedelivered(hasPriority);
			return mex;
		});
	}

	public <M extends JMSMessage> void sendToQueue(String queue, M message, long delay) {
		logger.debug("Sending to the queue: {} the message: {} with delay: {}", queue, message, delay);
		queueJmsMessagingTemplate.getJmsTemplate().send(queue, session -> {
			Message mex = messageConverter.toMessage(message, session);
			mex.setLongProperty(org.apache.activemq.artemis.api.core.Message.HDR_SCHEDULED_DELIVERY_TIME.toString(), System.currentTimeMillis() + delay);
			return mex;
		});
	}

	public <M extends JMSMessage> void sendToTopic(String topic, M message, long delay) {
		logger.debug("Sending to the queue: {} the message: {} with delay: {}", topic, message, delay);
		topicJmsMessagingTemplate.getJmsTemplate().send(topic, session -> {
			Message mex = messageConverter.toMessage(message, session);
			mex.setLongProperty(org.apache.activemq.artemis.api.core.Message.HDR_SCHEDULED_DELIVERY_TIME.toString(), System.currentTimeMillis() + delay);
			return mex;
		});
	}

}
