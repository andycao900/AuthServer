package com.web.auth.service.model;


public class AuthnResponse {

	private String token;
	private Version version;
	private String inResponseTo;
	private Conditions conditions;
	private String authenticationInstant;
	private String issuer;
	private UserCredentials userData;
	/**
	 * @return the authenticationInstant
	 */
	public String getAuthenticationInstant() {
		return authenticationInstant;
	}
	/**
	 * @param authenticationInstant the authenticationInstant to set
	 */
	public void setAuthenticationInstant(String authenticationInstant) {
		this.authenticationInstant = authenticationInstant;
	}
	/**
	 * @return the conditions
	 */
	public Conditions getConditions() {
		return conditions;
	}
	/**
	 * @param conditions the conditions to set
	 */
	public void setConditions(Conditions conditions) {
		this.conditions = conditions;
	}
	/**
	 * @return the inResponseTo
	 */
	public String getInResponseTo() {
		return inResponseTo;
	}
	/**
	 * @param inResponseTo the inResponseTo to set
	 */
	public void setInResponseTo(String inResponseTo) {
		this.inResponseTo = inResponseTo;
	}
	/**
	 * @return the issuer
	 */
	public String getIssuer() {
		return issuer;
	}
	/**
	 * @param issuer the issuer to set
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * @return the version
	 */
	public Version getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(Version version) {
		this.version = version;
	}
	/**
	 * @return the userData
	 */
	public UserCredentials getUserData() {
		return userData;
	}
	/**
	 * @param userData the userData to set
	 */
	public void setUserData(UserCredentials userData) {
		this.userData = userData;
	}
	
	
	
}
