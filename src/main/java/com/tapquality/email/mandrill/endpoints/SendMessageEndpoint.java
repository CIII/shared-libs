package com.tapquality.email.mandrill.endpoints;

/**
 * Mandrill api for sending a message
 * 
 * @author dmcguire
 *
 */
public class SendMessageEndpoint implements MandrillEndpoint{

	@Override
	public String getRoute() {
		return "messages/send-template.json";
	}
	
}
