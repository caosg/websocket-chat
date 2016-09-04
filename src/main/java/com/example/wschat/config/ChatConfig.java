package com.example.wschat.config;

import com.example.wschat.domain.SessionProfanity;
import com.example.wschat.event.ParticipantRepository;
import com.example.wschat.event.PresenceEventListener;
import com.example.wschat.util.ProfanityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class ChatConfig {

	public static class Destinations {
		private Destinations() {
		}

		private static final String LOGIN = "/topic/chat.login";
		private static final String LOGOUT = "/topic/chat.logout";
	}

	private static final int MAX_PROFANITY_LEVEL = 5;

	/*
	 * @Bean
	 * 
	 * @Description("Application event multicaster to process events asynchonously"
	 * ) public ApplicationEventMulticaster applicationEventMulticaster() {
	 * SimpleApplicationEventMulticaster multicaster = new
	 * SimpleApplicationEventMulticaster();
	 * multicaster.setTaskExecutor(Executors.newFixedThreadPool(10)); return
	 * multicaster; }
	 */
	@Bean
	@Description("Tracks user presence (join / leave) and broacasts it to all connected users")
	public PresenceEventListener presenceEventListener(SimpMessagingTemplate simpMessagingTemplate) {
		PresenceEventListener presence = new PresenceEventListener(simpMessagingTemplate, participantRepository());
		presence.setLoginDestination(Destinations.LOGIN);
		presence.setLogoutDestination(Destinations.LOGOUT);
		return presence;
	}

	@Bean
	@Description("Keeps connected users")
	public ParticipantRepository participantRepository() {
		return new ParticipantRepository();
	}

	@Bean
	@Scope(value = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
	@Description("Keeps track of the level of profanity of a websocket session")
	public SessionProfanity sessionProfanity() {
		return new SessionProfanity(MAX_PROFANITY_LEVEL);
	}

	@Bean
	@Description("Utility class to check the number of profanities and filter them")
	public ProfanityChecker profanityFilter() {
		Set<String> profanities = new HashSet<>(Arrays.asList("damn", "crap", "ass"));
		ProfanityChecker checker = new ProfanityChecker();
		checker.setProfanities(profanities);
		return checker;
	}
	
}
