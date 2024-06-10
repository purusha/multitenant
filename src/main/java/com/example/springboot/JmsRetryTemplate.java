package com.example.springboot;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;

import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;

public class JmsRetryTemplate extends JmsTemplate {

	private RetryTemplate retryTemplate;

	public JmsRetryTemplate() {
		super();
		this.retryTemplate = createRetryTemplate();
	}

	public JmsRetryTemplate(RetryTemplate retryTemplate) {
		super();
		this.retryTemplate = retryTemplate;
	}

	public JmsRetryTemplate(ConnectionFactory connectionFactory) {
		super(connectionFactory);
		this.retryTemplate = createRetryTemplate();
	}

	public JmsRetryTemplate(RetryTemplate retryTemplate, ConnectionFactory connectionFactory) {
		super(connectionFactory);
		this.retryTemplate = retryTemplate;
	}

	private RetryTemplate createRetryTemplate() {
		RetryTemplate template = new RetryTemplate();
		template.setThrowLastExceptionOnExhausted(true);
		return template;
	}

	// FIX execute

	@Override
	public <T> T execute(SessionCallback<T> action, boolean startConnection) throws JmsException {
		return retryTemplate.execute(retryContext -> {
			if (retryContext.getLastThrowable() instanceof IllegalStateException) {
				// Only in case of javax.jms.IllegalStateException: The Session is closed
				retryContext.setExhaustedOnly();
				throw (JmsException) retryContext.getLastThrowable();
			}
			return super.execute(action, startConnection);
		});
	}

	// FIX executeLocal (private method)

	@Override
	@Nullable
	public Message sendAndReceive(final Destination destination, final MessageCreator messageCreator) throws JmsException {
		return retryTemplate.execute(retryContext -> {
			if (retryContext.getLastThrowable() instanceof IllegalStateException) {
				// Only in case of javax.jms.IllegalStateException: The Session is closed
				retryContext.setExhaustedOnly();
				throw (JmsException) retryContext.getLastThrowable();
			}
			return super.sendAndReceive(destination, messageCreator);
		});
	}

	@Override
	@Nullable
	public Message sendAndReceive(final String destinationName, final MessageCreator messageCreator) throws JmsException {
		return retryTemplate.execute(retryContext -> {
			if (retryContext.getLastThrowable() instanceof IllegalStateException) {
				// Only in case of javax.jms.IllegalStateException: The Session is closed
				retryContext.setExhaustedOnly();
				throw (JmsException) retryContext.getLastThrowable();
			}
			return super.sendAndReceive(destinationName, messageCreator);
		});
	}

}
