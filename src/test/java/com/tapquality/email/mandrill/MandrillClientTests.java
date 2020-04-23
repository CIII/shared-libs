package com.tapquality.email.mandrill;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapquality.TapQualityException;
import com.tapquality.email.mandrill.MandrillClient;
import com.tapquality.email.mandrill.MandrillEmail;
import com.tapquality.email.mandrill.MandrillEmailList;

public class MandrillClientTests {
	private String testKey = "hSaI0j3dVCSILJPVA_gEwA";
	private Map<String, String> attributes = new HashMap<String, String>();
	private ObjectMapper mapper = new ObjectMapper();
	private MandrillClient client = new MandrillClient(testKey);
	
	public MandrillClientTests() {
		attributes.put("first_name", "Jon");
		attributes.put("last_name", "Card");
		attributes.put("street", "69 Merriam St");
		attributes.put("city", "Somerville");
		attributes.put("state", "MA");
		attributes.put("zip", "02143");
		attributes.put("electric_bill", "100-150");
		attributes.put("electric_company", "eversource");
		attributes.put("phone_home", "303-916-3966");
		attributes.put("lead_email", "test@tapquality.com");
	}
	
	@Ignore
	@Test
	public void testPostToMandrill() throws Exception {
		// Given private members
		// When...
		String response = client.sendMessage("dave@tapquality.com", attributes, "lead-delivery-011117");
		System.out.println(response);
		// Then...
		// This doesn't have much tests; it's really just to exercise the code.
	}
	
	/**
	 * Test the scheduling of emails.  As with the post, this is an exercise rather than a test
	 * @throws TapQualityException 
	 * @throws JSONException 
	 * @throws IOException 
	 */
	@Ignore
	@Test
	public void testScheduleMandrill() throws TapQualityException, JSONException, IOException {
		String response = client.scheduleMessage("dave@tapquality.com", attributes, "lead-delivery-011117", DateTime.now().plusMinutes(1).withZone(DateTimeZone.UTC));
		System.out.println(response);
	}
	
	@Ignore
	@Test
	public void testGetMandrillMessageInfo() throws TapQualityException, URISyntaxException, IOException {
		MandrillEmail info = client.getMessage("01116d2fcf044f4d841d79943f6e2120");
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(info));
	}
	
	@Ignore
	@Test
	public void testSearchMandrillMessages() throws TapQualityException, IOException {
		MandrillEmailList emails = client.searchMessages(DateTime.parse("2017-04-01"), DateTime.now());
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(emails));
	}
}
