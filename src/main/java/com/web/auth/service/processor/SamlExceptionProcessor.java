package com.web.auth.service.processor;

import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.work.saml2.constants.SamlConstants;
import com.work.saml2.util.SAML2ResponseUtil;

public class SamlExceptionProcessor implements SamlProcessor, SamlConstants
{

	public String processSAMLRequest(XMLObject xmlObject)
	{
		SAML2ResponseUtil responseUtil = new SAML2ResponseUtil();
		return responseUtil.createSAMLResponseForStatus(null,SamlConstants.SAML2_SC_REQUNSUPP,null);
	}

}
