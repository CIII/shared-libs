package com.tapquality.email.mandrill.endpoints;

/**
 * Mandrill endpoint for searching messages.
 * 
 * @author dmcguire
 *
 */
public class SearchMessagesEndpoint implements MandrillEndpoint{

	@Override
	public String getRoute() {
		return "messages/search.json";
	}
	
}
