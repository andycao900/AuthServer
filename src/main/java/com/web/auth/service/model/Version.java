package com.web.auth.service.model;

public class Version {

	private int MajorVersion;
	private int MinorVersion;
	private String version;
	public int getMajorVersion() {
		return MajorVersion;
	}
	public void setMajorVersion(int majorVersion) {
		MajorVersion = majorVersion;
	}
	public int getMinorVersion() {
		return MinorVersion;
	}
	public void setMinorVersion(int minorVersion) {
		MinorVersion = minorVersion;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	

}
