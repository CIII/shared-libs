package com.tapquality.email.mandrill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class to encapsulate a Mandrill Email
 * 
 * @author dmcguire
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MandrillEmail {
	@JsonIgnore
	private Long id;
	private String mandrillId;
	private String email;
	private String state;
	private String subject;
	private String sender;
	private String template;
	private int opens;
	private int clicks;

	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public int getOpens() {
		return opens;
	}
	public void setOpens(int opens) {
		this.opens = opens;
	}
	public int getClicks() {
		return clicks;
	}
	public void setClicks(int clicks) {
		this.clicks = clicks;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@JsonProperty("mandrillId")
	public String getMandrillId() {
		return mandrillId;
	}
	@JsonProperty("_id")
	public void setMandrillId(String mandrillId) {
		this.mandrillId = mandrillId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
