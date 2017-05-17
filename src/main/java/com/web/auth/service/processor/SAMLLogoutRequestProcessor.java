package com.web.auth.service.processor;

import java.rmi.RemoteException;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.web.auth.service.cache.CacheAdapter;
import com.web.auth.service.cache.CacheHandler;
import com.work.saml2.constants.SamlConstants;
import com.work.saml2.util.SAML2ResponseUtil;


/**
 * <p>
 * Description:
 * SAMLLogoutRequestHandler - handles logout requests.  
 * <p>
 * Created on  11 15, 2008
 * @author <a href="mailto:mike.rice@lfg.com">Mike Rice</a>
 * @version 1.0
 * 
 * Common usage:
 * <code> 
 *   SAMLLogoutRequestHandler object = (new SAMLLogoutRequestHandler().
 *   .processSAMLRequest(logoutRequestXML));
 * 
 * <code>
 * 
 * @knownbugs  Method should not throw exceptions.Write a generic class that takes in a message and returns proper saml status
 * 

 */
public class SAMLLogoutRequestProcessor implements SamlProcessor,SamlConstants 
{

	/**
	 * logger for this class
	 */
	private static Logger logger = LoggerFactory
			.getLogger(SAMLAssertionRequestProcessor.class);
	
	
	/**
	 * This method is an abomination.
	 *
	 * @param samlAsserRequestXML SAML assertion request message that contains 
	 * 							  assertion data to be retrieved
	 *
	 * @return String Assertion as string xml that is being requested - this assertion contains
	 * 				  autenticationa and attribute statements
     *
     * @throws RemoteException This should never be throwed should return error message as proper SAML status
	*/	
	public String processSAMLRequest(XMLObject xmlObject)
	{
		logger.debug("processSAMLRequest start.");

		// Check the incoming object, cast to appropriate class
		if (xmlObject == null)
		{
			logger.error("Incoming XML object is null!");
			return null;
		}
		if (!(xmlObject instanceof LogoutRequest))
		{
			logger.error("Incoming XML object is not an LogoutRequest");
			return null;
		}

		String responseXML = "";
		try
		{
			
			// Convert to logout request object
			LogoutRequest logoutRequest = (LogoutRequest) xmlObject;
			logger.debug("LogoutRequest converted from samlLogoutRequestXML: [" + logoutRequest + "]");

			// get the assertion id from request message
			String assertionId = logoutRequest.getID();
			logger.debug("Assertion id: [" + assertionId + "]");
		
			// Get the cache handler & remove assertion from cache
			CacheAdapter ca = CacheHandler.getInstance().getCacheAdapterImpl();
			ca.cacheObject(assertionId, new String("Ex-Assertion"));

			SAML2ResponseUtil responseUtil = new SAML2ResponseUtil();
			// Create the response
           	responseXML = responseUtil.createLogoutResponse(getIDPUrl());

		}
    	catch (Exception e) 
    	{
			logger.error("Exception processing logout", e);
		}	

    	// Done.
		logger.debug("logout response: [" + responseXML + "]");
		return responseXML;
		
	}

	private String getIDPUrl() {
		return "auth_endpoint_url";
	}
}
