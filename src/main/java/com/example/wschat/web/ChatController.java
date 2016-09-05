package com.example.wschat.web;

import com.example.wschat.domain.ChatMessage;
import com.example.wschat.domain.SessionProfanity;
import com.example.wschat.event.LoginEvent;
import com.example.wschat.event.ParticipantRepository;
import com.example.wschat.exception.TooMuchProfanityException;
import com.example.wschat.util.ProfanityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Collection;

/**
 * Controller that handles WebSocket chat messages
 * 
 * @author
 */
@Controller
public class ChatController {

	@Autowired
    private ProfanityChecker profanityFilter;
	
	@Autowired
    private SessionProfanity profanity;
	
	@Autowired
    private ParticipantRepository participantRepository;
	
	@Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
	
	
	@SubscribeMapping("/chat.participants")
	public Collection<LoginEvent> retrieveParticipants() {
		return participantRepository.getActiveSessions().values();
	}
	
	@MessageMapping("/chat.message")
	public ChatMessage filterMessage(@Payload ChatMessage message, Principal principal) {
		checkProfanityAndSanitize(message);
		message.setUsername(principal.getName());
		return message;
	}
	
	@MessageMapping("/chat.private.{username}")
	public void filterPrivateMessage(@Payload ChatMessage message, @DestinationVariable("username") String username, Principal principal) {
		checkProfanityAndSanitize(message);
		
		message.setUsername(principal.getName());

		simpMessagingTemplate.convertAndSend("/user/" + username + "/exchange/amq.direct/chat.message", message);
	}

	private void checkProfanityAndSanitize(ChatMessage message) {
		long profanityLevel = profanityFilter.getMessageProfanity(message.getMessage());
		profanity.increment(profanityLevel);
		message.setMessage(profanityFilter.filter(message.getMessage()));
	}
	
	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}
}