package com.web.auth.service.processor;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnQuery;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.web.auth.service.cache.CacheHandler;
import com.web.auth.service.cache.CacheAdapter;
import com.work.saml2.constants.SamlConstants;
import com.work.saml2.util.SAML2RequestUtil;
import com.work.saml2.util.SAML2ResponseUtil;



/**
 * 
 * @TODO Method should not throw exceptions
 * write a generic class that takes in a message and returns proper saml status
 *
 */


public class SamlAuthnValidationProcessor implements SamlProcessor,SamlConstants 
{

	private static Logger logger = LoggerFactory
			.getLogger(SamlAuthnValidationProcessor.class);

	public String processSAMLRequest(XMLObject xmlObject) 
	{
		String strResponseXML = null;
		String assertionRequestId = null;
		SAML2ResponseUtil responseUtil = new SAML2ResponseUtil();
		//SAML2RequestUtil requestUtil = new SAML2RequestUtil();
		
    	try 
    	{
    		 
    		AuthnQuery authnQuery = (AuthnQuery)xmlObject;
    		String subjectToBeAsserted = authnQuery.getSubject().getNameID().getValue();
    		logger.debug("AuthQuery for subject : "+subjectToBeAsserted);
    		assertionRequestId = authnQuery.getID();
    		
			//CacheObject cacheObject = (CacheObject)CacheHandler.getInstance().getCacheAdapterImpl().getCachedObject(subjectToBeAsserted);
			//if(cacheObject != null)
			//{
    		String strAssertionId = (String) CacheHandler.getInstance().getCacheAdapterImpl().getCachedObject(subjectToBeAsserted);
    		if(strAssertionId != null)
    			{

				String assertionId = strAssertionId;//(String)cacheObject.getCachedObject();
				if(assertionId != null)
				{
					//cacheObject = (CacheObject)CacheHandler.getInstance().getCacheAdapterImpl().getCachedObject(assertionId);
					//if(cacheObject != null)
					//{
	    			String strAssertionXML = (String) CacheHandler.getInstance().getCacheAdapterImpl().getCachedObject(assertionId);
	    			if(strAssertionXML != null)
	    			{
					
				        Response response = (Response) responseUtil.buildXMLObject(Response.DEFAULT_ELEMENT_NAME);
				        response.setSchemaLocation("urn:oasis:names:tc:SAML:2.0:protocol http://docs.oasis-open.org/security/saml/v2.0/saml-schema-protocol-2.0.xsd urn:oasis:names:tc:SAML:2.0:assertion http://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd");
				        response.addNamespace(new Namespace("http://www.w3.org/2001/XMLSchema-instance", "xsi"));       
				        response.addNamespace(new Namespace("urn:oasis:names:tc:SAML:2.0:assertion", "saml")); 
				        
				        DateTime now = new DateTime(ISOChronology.getInstanceUTC());
				        now = now.minusMinutes(3);

						//String strAssertionXML = (String)cacheObject.getCachedObject();
						logger.debug("Assertion retrieved from cache : "+strAssertionXML);
						Assertion assertion = (Assertion)responseUtil.buildXMLObject(strAssertionXML);				        
					
						//make sure we did not get a tampered message
						
						DateTime notOnOrAfter = assertion.getConditions().getNotOnOrAfter();
						logger.debug("Assertion notOnOrAfter : "+notOnOrAfter);
						DateTime notBefore = assertion.getConditions().getNotBefore();
						logger.debug("Assertion notBefore : "+notBefore);
						
						String subjectNameId = assertion.getSubject().getNameID().getValue();
						logger.debug("Assertion subjectNameId : "+subjectNameId);	
						
				        if(notOnOrAfter.isAfterNow() && notBefore.isBeforeNow())
				        {
				        	
					        //assertion is valid so send back all assertions related to the request
				        	logger.debug("The Assertion is Valid and send the Assertion back to the client");
				        	
				        	//update the assertion and place in back in cache				        
				        	assertion.getConditions().setNotOnOrAfter(now.plusMinutes(33));
				        	response.getAssertions().add(assertion);
				        	
				        	strAssertionXML = responseUtil.getXMLForSAMLObject(assertion);
				        	
				        	//@TODO : should also update jboss cache time to 30 minutes from now..
				        	//Note: need to investigate on how to do this?				     
				        	CacheHandler.getInstance().getCacheAdapterImpl().cacheObject(assertionId,strAssertionXML);
				        	CacheHandler.getInstance().getCacheAdapterImpl().cacheObject(subjectToBeAsserted,assertionId);
				        	
					        String responseId = responseUtil.generateRandomId();        
					        response.setID(responseId);        
					        response.setInResponseTo(assertionRequestId);
					        response.setIssueInstant(now);
			            	
			            	
				            QName issuerQName = new QName(SAMLConstants.SAML20_NS, Issuer.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
				            Issuer rIssuer = (Issuer)responseUtil.buildXMLObject(issuerQName);	      
				            rIssuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
				            rIssuer.setValue("auth_endpoint_url");//(AuthUtil.getIDPUrl());
			            	
				            Status status = (Status) responseUtil.buildXMLObject(Status.DEFAULT_ELEMENT_NAME);
				            
				            StatusCode statusCode = (StatusCode) responseUtil.buildXMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
				            statusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
				            status.setStatusCode(statusCode);
				            StatusMessage statusMessage = (StatusMessage) responseUtil.buildXMLObject(StatusMessage.DEFAULT_ELEMENT_NAME);
				            statusMessage.setMessage("Success");         
				            status.setStatusMessage(statusMessage);
			            	
				            response.setIssuer(rIssuer);        
				            response.setStatus(status);
				            
				            strResponseXML = responseUtil.getXMLForSAMLObject(response);
				        	
				        	
				        }else{
				        	logger.error("The Assertion is not valid ");
				        	strResponseXML = responseUtil.createSAMLResponseForStatus(assertionRequestId,SAML2_SC_AUTHNFAIL,null);
						}
					}else{
			        	logger.error("Assertion retrieved from cache is null");
			        	strResponseXML = responseUtil.createSAMLResponseForStatus(assertionRequestId,SAML2_SC_NOAUTNCTX,null);
					}					
					
				}
			}else{			
	        	logger.error("Subject name id retrieved from cache is null");
	        	strResponseXML = responseUtil.createSAMLResponseForStatus(assertionRequestId,SAML2_SC_NOAUTNCTX,null);
			}
		}
    	catch (Exception e) 
    	{
			logger.error("Exception while handling saml authentication query request", e);
			strResponseXML = responseUtil.createSAMLResponseForStatus(assertionRequestId,SAML2_SC_RESPONDER,null);
		}	
		return strResponseXML;
	}
	
	

}
