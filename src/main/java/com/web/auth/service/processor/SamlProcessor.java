package com.web.auth.service.processor;

import org.opensaml.xml.XMLObject;

public interface SamlProcessor {

	public String processSAMLRequest(XMLObject xmlObject);
}
