package com.example.springboot;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class HelloController {
	
	@Autowired
	JMSSender sender;

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}
	
	@PostMapping("/{topic}")
	public ResponseEntity<UUID> sendMessage(@PathVariable(value = "topic") String topic, @RequestBody JsonNode body) {			
		sender.sendToQueue(topic, body);
		
		return ResponseEntity.ok(UUID.randomUUID());
	}

}
