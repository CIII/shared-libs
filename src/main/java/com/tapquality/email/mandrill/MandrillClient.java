package com.tapquality.email.mandrill;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tapquality.TapQualityException;
import com.tapquality.dao.TapQualityDataException;
import com.tapquality.email.mandrill.endpoints.GetMessageEndpoint;
import com.tapquality.email.mandrill.endpoints.MandrillEndpoint;
import com.tapquality.email.mandrill.endpoints.SearchMessagesEndpoint;
import com.tapquality.email.mandrill.endpoints.SendMessageEndpoint;

/**
 * Class for interacting with the Mandrill REST services.
 * 
 * @author dmcguire
 *
 */
public class MandrillClient {
	private static final Log LOG = LogFactory.getLog(MandrillClient.class);
	private static final String URL_BASE = "https://mandrillapp.com/api/1.0/";
	private ObjectMapper mapper = new ObjectMapper();
	private final String key;

	private static final String MANDRILL_REJECT_ERR = "Mandrill rejected or bounced the email";
	private static final String MANDRILL_POST_ERR = "Exception posting to Mandrill";
	private static final String MANDRILL_FAILURE = "Mandrill returned a non 200 exit code";
	private static final String MANDRILL_PARSE_ERR = "Unable to parse response from Mandrill";
	private static final String MANDRILL_INVALID_URI_ERR = "Invalid Mandrill Uri";
	
	@Inject
	protected MandrillClient(@Named("mandrill.key") String key) {
		this.key = key;
	}
	
	/**
	 * Performs an http get, specifying the url based on the endpoint.  Params are passed as query
	 * parameters.
	 * 
	 * @param endpoint
	 * @param params
	 * @return
	 * @throws URISyntaxException
	 * @throws TapQualityException
	 */
	private <T extends MandrillEndpoint> HttpResponse doGet(HttpClient client, T endpoint, Map<String, String> params) throws URISyntaxException, TapQualityException {
		HttpGet get = new HttpGet(formatUrl(endpoint));
		URIBuilder builder = new URIBuilder(get.getURI());
		for(Entry<String, String> param : params.entrySet()) {
			builder.addParameter(param.getKey(), param.getValue());
		}
		
		get.setURI(builder.build());
		return execute(client, get);
	}
	
	/**
	 * Performs an http post, specifying the url based on the endpoing.  The body is passed with the request.
	 * 
	 * @param endpoint
	 * @param body
	 * @return
	 * @throws TapQualityException
	 * @throws UnsupportedEncodingException
	 */
	private <T extends MandrillEndpoint> HttpResponse doPost(HttpClient client, T endpoint, String body) throws TapQualityException, UnsupportedEncodingException {
		HttpPost post = new HttpPost(formatUrl(endpoint));
		StringEntity postbody = new StringEntity(body);
		post.setEntity(postbody);
		return execute(client, post);
	}
	
	/**
	 * Formats a url for Mandrill based on the endpoint. 
	 * 
	 * @param endpoint
	 * @return
	 */
	private <T extends MandrillEndpoint> String formatUrl(T endpoint) {
		return URL_BASE + endpoint.getRoute();
	}
	
	/**
	 * Execute an http method, defined by the request.  
	 * 
	 * @param request
	 * @return
	 * @throws TapQualityException
	 */
	private HttpResponse execute(HttpClient client, HttpUriRequest request) throws TapQualityException {
		try {
			return client.execute(request);
		} catch (IOException e) {
			String errMsg = String.format("Unable to execute request, %s", request.toString());
			LOG.error(errMsg, e);
			throw new TapQualityException(errMsg, e);
		}
	}
	
	/**
	 * Given an http response, if the request was successful get the body as a string.  If it was
	 * not successful, a tapquality exception is thrown.
	 * 
	 * @param response
	 * @return
	 * @throws TapQualityException
	 * @throws IOException
	 */
	private String getResponseBody(HttpResponse response) throws TapQualityException, IOException {
		if(response.getStatusLine().getStatusCode() == 200) {
			return EntityUtils.toString(response.getEntity());
		} else {
			throw new TapQualityException(MANDRILL_FAILURE + " " + response.getStatusLine().getStatusCode() + "\n" + EntityUtils.toString(response.getEntity()));
		}
	}
	
	/**
	 * Construct the body for a post call given the email, template, and attributes to fill out the template.
	 * Will also specify the key for the post, as well as trackOpens = true.
	 * 
	 * @param toEmail
	 * @param attributes
	 * @param templateName
	 * @return
	 * @throws JSONException
	 */
	private JSONObject constructPostBody(String toEmail, Map<String, String> attributes, String templateName) throws JSONException {
		Map<String, JSONArray> message = new HashMap<>();
		// recipient
		Map<String, String> recipient = new HashMap<>();
		recipient.put("email", toEmail);
		JSONObject recipientObject = new JSONObject(recipient);
		List<JSONObject> recipients = new ArrayList<>();
		recipients.add(recipientObject);
		JSONArray recipientsArray = new JSONArray(recipients);
		message.put("to", recipientsArray);
		
		List<JSONObject> mergeVars = new ArrayList<>();
		for(Entry<String, String> attribute : attributes.entrySet()) {
			Map<String, String> var = new HashMap<>();
			var.put("name", attribute.getKey());
			var.put("content", attribute.getValue());
			mergeVars.add(new JSONObject(var));
		}
		
		JSONArray mergeVarsArray = new JSONArray(mergeVars);
		message.put("global_merge_vars", mergeVarsArray);
		JSONObject messageObject = new JSONObject(message);
		messageObject.put("track_opens", true);
		
		Map<String, String> envelope = new HashMap<>();
		envelope.put("key", key);
		envelope.put("template_name", templateName);
		JSONObject envelopeObject = new JSONObject(envelope);
		envelopeObject.put("template_content", new JSONArray());
		envelopeObject.put("message", messageObject);

		return envelopeObject;
	}
	
	/**
	 * Return a list of all Mandrill messages that falls between fromDate and toDate
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws TapQualityException
	 * @throws IOException 
	 */
	public MandrillEmailList searchMessages(DateTime fromDate, DateTime toDate) throws TapQualityException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			Map<String, String> params = new HashMap<>();
			params.put("key", key);
			params.put("date_from", fromDate.toString("yyyy-MM-dd"));
			params.put("date_to", toDate.toString("yyyy-MM-dd"));
			params.put("limit", "1000");
			HttpResponse response = doGet(client, new SearchMessagesEndpoint(), params);
			return mapper.readValue(getResponseBody(response), MandrillEmailList.class);
		} catch (IOException e) {
			LOG.error(MANDRILL_PARSE_ERR, e);
			throw new TapQualityException(MANDRILL_PARSE_ERR, e);
		} catch (URISyntaxException e) {
			LOG.error(MANDRILL_INVALID_URI_ERR, e);
			throw new TapQualityException(MANDRILL_INVALID_URI_ERR, e);
		} finally {
			client.close();
		}
	}
	
	/**
	 * Given a mandrill message id, return all other information associated with it.
	 * 
	 * @param id
	 * @return
	 * @throws URISyntaxException
	 * @throws TapQualityException
	 * @throws IOException 
	 */
	public MandrillEmail getMessage(String id) throws URISyntaxException, TapQualityException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			Map<String, String> params = new HashMap<>();
			params.put("key", key);
			params.put("id", id);
			HttpResponse response = doGet(client, new GetMessageEndpoint(), params);
			return mapper.readValue(getResponseBody(response), MandrillEmail.class);
		} catch (IOException e) {
			LOG.error(MANDRILL_PARSE_ERR, e);
			throw new TapQualityException(MANDRILL_PARSE_ERR, e);
		} finally {
			client.close();
		}
	}
	
	/**
	 * Send an email message immediately to toEmail, with template and attributes set accordingly.
	 * 
	 * @param toEmail
	 * @param attributes
	 * @param template
	 * @return
	 * @throws JSONException
	 * @throws TapQualityException
	 * @throws IOException 
	 */
	public String sendMessage(String toEmail, Map<String, String> attributes, String template) throws JSONException, TapQualityException, IOException {
		JSONObject body = constructPostBody(toEmail, attributes, template);
		return sendMessage(body);
	}
	
	/**
	 * Same as sendMessage but schedules it to be sent at a later time.  if sendAt is before now,
	 * it will send immediately.
	 * 
	 * @param toEmail
	 * @param attributes
	 * @param template
	 * @param sendAt
	 * @return
	 * @throws JSONException
	 * @throws TapQualityException
	 * @throws IOException 
	 */
	public String scheduleMessage(String toEmail, Map<String, String> attributes, String template, DateTime sendAt) throws JSONException, TapQualityException, IOException {
		JSONObject body = constructPostBody(toEmail, attributes, template);
		body.put("send_at", sendAt.toString("yyyy-MM-dd HH:mm:ss"));
		return sendMessage(body);		
	}
	
	/**
	 * Carry out the message send given a json body, and parse out the response.
	 * 
	 * @param body
	 * @return
	 * @throws TapQualityException
	 * @throws IOException 
	 */
	private String sendMessage(JSONObject body) throws TapQualityException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		try {
			HttpResponse response = doPost(client, new SendMessageEndpoint(), body.toString());
			if(response.getStatusLine().getStatusCode() == 200) {
				String responseBody = EntityUtils.toString(response.getEntity());
				JSONArray returnMessage = new JSONArray(responseBody);
				JSONObject returnItem = returnMessage.getJSONObject(0);
				String returnStatus = returnItem.getString("status");
				if("rejected".equals(returnStatus) || "bounced".equals(returnStatus)) {
					LOG.warn(MANDRILL_REJECT_ERR + "\n" + responseBody);
					throw new TapQualityDataException(MANDRILL_REJECT_ERR);
				}
				return responseBody;
			} else {
				throw new TapQualityException(MANDRILL_FAILURE + " " + response.getStatusLine().getStatusCode() + "\n" + EntityUtils.toString(response.getEntity()));
			}
		} catch (JSONException | IOException e) {
			LOG.error(MANDRILL_POST_ERR, e);
			throw new TapQualityException(MANDRILL_POST_ERR, e);
		} finally {
			client.close();
		}
	}
}
