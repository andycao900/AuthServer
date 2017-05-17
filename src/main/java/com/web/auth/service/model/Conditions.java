package com.web.auth.service.model;

public class Conditions 
{
	private String NotBefore;
	private String NotOnOrAfter;
	public String getNotBefore() {
		return NotBefore;
	}
	public void setNotBefore(String notBefore) {
		NotBefore = notBefore;
	}
	public String getNotOnOrAfter() {
		return NotOnOrAfter;
	}
	public void setNotOnOrAfter(String notOnOrAfter) {
		NotOnOrAfter = notOnOrAfter;
	}
}