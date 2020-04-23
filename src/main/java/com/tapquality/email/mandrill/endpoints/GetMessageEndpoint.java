package com.tapquality.email.mandrill.endpoints;

/**
 * Endpoint class for the message info Mandrill API. 
 * 
 * @author dmcguire
 *
 */
public class GetMessageEndpoint implements MandrillEndpoint{

	@Override
	public String getRoute() {
		return "messages/info.json";
	}
	
}
