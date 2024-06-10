package com.example.springboot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JMSReply implements JMSMessage {
	private static final long serialVersionUID = 1L;

	public static final JMSReply OK = new JMSReply(Type.SUCCESS, null);

	public enum Type {
		SUCCESS, ERROR
	}
	
	private Type type;
	private String payload;
	
	@JsonCreator
	public JMSReply(@JsonProperty("type") Type type,
			@JsonProperty("payload") String payload) {
		this.type = type;
		this.payload = payload;
	}

	public Type getType() {
		return type;
	}

	public void setType(final Type type) {
		this.type = type;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(final String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "JMSReply{" +
				"type=" + type +
				", payload='" + payload + '\'' +
				'}';
	}
}
