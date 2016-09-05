package com.example.wschat.event;

/**
 * 
 * @author caosg by 2016.09.04
 */
public class LogoutEvent {
	
	private String username;

	public LogoutEvent(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
