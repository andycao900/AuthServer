package com.web.auth.service.processor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AssertionIDRef;
import org.opensaml.saml2.core.AssertionIDRequest;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.schema.XSString;

import com.web.auth.service.cache.CacheAdapter;
import com.web.auth.service.cache.CacheHandler;
import com.work.saml2.constants.SamlConstants;
import com.work.saml2.util.SAML2RequestUtil;
import com.work.saml2.util.SAML2ResponseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Description:
 * SAMLAssertionRequestHandler is ... a complete piece of, well, erm, you know.
 * <p>
 * Created on  05 28, 2008
 * @author <a href="mailto:kiran.sanivarapu@lfg.com">Kiran Sanivarapu</a>
 * @version 1.0
 * 
 * Common usage:
 * <code> 
 *   SAMLAssertionRequestProcessor object = (new SAMLAssertionRequestProcessor().
 *   .processSAMLRequest(assertionRequestXML));
 * 
 * <code>
 * 
 * @knownbugs  Method should not throw exceptions.Write a generic class that takes in a message and returns proper saml status
 * 
 * ***********************************************************************************
 */
public class SAMLAssertionRequestProcessor implements SamlProcessor,SamlConstants 
{
	/**
	 * logger for this class
	 */
	private static Logger logger = LoggerFactory
			.getLogger(SAMLAssertionRequestProcessor.class);
	
	SAML2ResponseUtil responseUtil = new SAML2ResponseUtil();
	
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
		if (!(xmlObject instanceof AssertionIDRequest))
		{
			logger.error("Incoming XML object is not an AssertionIDRequest");
			return null;
		}
		AssertionIDRequest assertionIdRequest = (AssertionIDRequest) xmlObject;
		logger.debug("AssertionIDRequest converted from samlAsserRequestXML: [" + assertionIdRequest + "]");

		// get the assertion id from request message
		String assertionRequestId = assertionIdRequest.getID();
		logger.debug("Assertion request id: [" + assertionRequestId + "]");
		
		// Get the list of assertion id references
		List assertionIDRefList = assertionIdRequest.getAssertionIDRefs();
		if(assertionIDRefList == null || assertionIDRefList.size() <= 0)
		{
			logger.error("Assertions retrieved from AssertionIDRequest are empty!");
			return responseUtil.createSAMLResponseForStatus(assertionRequestId,SAML2_SC_REQUESTER, null);
		}

		String strResponseXML = null;
    	try 
    	{
			
    		// Loop over the assertions
    		for(int a=0; a < assertionIDRefList.size(); a++)
    		{

    			// Get a list item and retrieve the assertion id
    			AssertionIDRef assertionIDRef = (AssertionIDRef) assertionIDRefList.get(a);					
    			String assertionId = assertionIDRef.getAssertionID();
    			logger.debug("Assertion id reference being worked on : "+assertionId);

    			// Retrieve the assertion from cache
    			String strAssertionXML = (String) CacheHandler.getInstance().getCacheAdapterImpl().getCachedObject(assertionId);
    			if(strAssertionXML == null)
    			{
    				// we are terminating the process regardless of what assertion we failed to retrieve
    				// and return with an error back
    				logger.error("Assertion retrieved from cache is null");
    				strResponseXML = responseUtil.createSAMLResponseForStatus(assertionRequestId,SAML2_SC_NOAUTNCTX,null);
    				break;
    			}
    			
    			Assertion assertion = (Assertion) responseUtil.buildXMLObject(strAssertionXML);		
    			logger.debug("Assertion retrieved from cache: [" + strAssertionXML + "]");
						
				// Check the timestamp on assertion; if not valid yet or no longer valid, abort
    			DateTime now = new DateTime(ISOChronology.getInstanceUTC());
    			now = now.minusMinutes(3);
				DateTime notOnOrAfter = assertion.getConditions().getNotOnOrAfter();
				DateTime notBefore = assertion.getConditions().getNotBefore();
		        if(notOnOrAfter.isBeforeNow() || notBefore.isAfterNow())
	        	{
	        		// we are terminating the process regardless of what assertion we failed to retrieve
    				// and return with an error back
	        		logger.error("The Assertion is not valid ");
	        		strResponseXML = responseUtil.createSAMLResponseForStatus(assertionRequestId,SAML2_SC_AUTHNFAIL,null);
	        		break;
				}
				        	
		        // Update assertion timestamp and put it back in cache				        
		        assertion.getConditions().setNotOnOrAfter(now.plusMinutes(33));
		        strAssertionXML = responseUtil.getXMLForSAMLObject(assertion);
		        CacheHandler.getInstance().getCacheAdapterImpl().cacheObject(assertionId, strAssertionXML);

		        // Get the user ID out of the assertion 
		        String subjectNameId = assertion.getSubject().getNameID().getValue();
		        
		        
		        // Create the attribute statement
		        //QName attributeStatementQName = new QName(SAMLConstants.SAML20_NS, AttributeStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
		        AttributeStatement attributeStatement = (AttributeStatement) responseUtil.buildXMLObject(AttributeStatement.DEFAULT_ELEMENT_NAME);		            

		        
                Set groupNames = null;//lincolnUsers.getLincolnGroups();

                if(groupNames != null && !groupNames.isEmpty())
                {

                	//QName groupNamesQName = new QName(SAMLConstants.SAML20_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
                	Attribute groupNamesAttribute = (Attribute) responseUtil.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
					            
                	groupNamesAttribute.setName("urn:lfg:Groups");
                	groupNamesAttribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
                	groupNamesAttribute.setFriendlyName("Groups");	
					            
                	//XMLObjectBuilder groupNamesBuilder = responseUtil.getBuilderFactory().getBuilder(XSString.TYPE_NAME);

                	for(Iterator groupNamesIterator = groupNames.iterator(); groupNamesIterator.hasNext();) 
                	{

                		XSString groupNameValue = (XSString) responseUtil.buildXMLObject(XSString.TYPE_NAME); // groupNamesBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);

                		groupNameValue.setValue((String) groupNamesIterator.next());				            
                		groupNamesAttribute.getAttributeValues().add(groupNameValue);
                	}
					            
                	attributeStatement.getAttributes().add(groupNamesAttribute);		            
                }
                
                addAttribute(attributeStatement, "FirstName", "test");
                addAttribute(attributeStatement, "LastName", "su");

                assertion.getAttributeStatements().add(attributeStatement);
				            
                Response response = (Response) responseUtil.buildXMLObject(Response.DEFAULT_ELEMENT_NAME);				        
                response.setSchemaLocation("urn:oasis:names:tc:SAML:2.0:protocol http://docs.oasis-open.org/security/saml/v2.0/saml-schema-protocol-2.0.xsd urn:oasis:names:tc:SAML:2.0:assertion http://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd");
                response.addNamespace(new Namespace("http://www.w3.org/2001/XMLSchema-instance", "xsi"));       
                response.addNamespace(new Namespace("urn:oasis:names:tc:SAML:2.0:assertion", "saml")); 
                response.getAssertions().add(assertion);

                if(a == (assertionIDRefList.size() - 1))
                {
				            	
                	String responseId = responseUtil.generateRandomId();        
                	response.setID(responseId);        
                	response.setInResponseTo(assertionRequestId);
                	response.setIssueInstant(now);
				            	
                	QName issuerQName = new QName(SAMLConstants.SAML20_NS, Issuer.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
                	Issuer rIssuer = (Issuer)responseUtil.buildXMLObject(issuerQName);	      
                	rIssuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
                	rIssuer.setValue("authendpoint");
				            	
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
                }
    		}
			
		} 
    	catch (Exception e) 
    	{
			logger.error("Exception while handling saml assertion profile request", e);
			strResponseXML = responseUtil.createSAMLResponseForStatus(assertionRequestId,SAML2_SC_RESPONDER,null);
		}	
			
		logger.debug("final saml assertion returned: [" + strResponseXML + "]");
		return strResponseXML;
		
	}

	/**
	 * Adds the attributes from attributeList to attributeStatement as attributeName
	 * @param attributeStatement
	 * @param attributeName
	 * @param attributeList
	 */
	private void addAttributes(AttributeStatement attributeStatement, String attributeName, List attributeList)
	{
		if (attributeStatement == null) return;
		if (attributeName == null || attributeName.trim().length() == 0) return;
        if (attributeList == null || attributeList.size() == 0) return;

       	QName qname = new QName(SAMLConstants.SAML20_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
       	Attribute attribute = (Attribute) responseUtil.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
       	attribute.setName("urn:lfg:" + attributeName );
       	attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
       	attribute.setFriendlyName(attributeName);	
			            
       	//XMLObjectBuilder xmlObjectBuilder = responseUtil.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
			            
       	for(int i = 0; i < attributeList.size(); i++)
       	{
       		XSString attributeValue = (XSString) responseUtil.buildXMLObject(XSString.TYPE_NAME);//xmlObjectBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);       		
            attributeValue.setValue((String) attributeList.get(i));				            
           	attribute.getAttributeValues().add(attributeValue);
       	}
			            
       	attributeStatement.getAttributes().add(attribute);		            

	}
	
	
	/**
	 * Adds the attributeValue  to attributeStatement as attributeName
	 * @param attributeStatement
	 * @param attributeName
	 * @param attributeText
	 */
	private void addAttribute(AttributeStatement attributeStatement, String attributeName, String attributeText)
	{
		if (attributeStatement == null) return;
		if (attributeName == null || attributeName.trim().length() == 0) return;
        if (attributeText == null || attributeText.trim().length() == 0) return;

       	QName qname = new QName(SAMLConstants.SAML20_NS, Attribute.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
       	Attribute attribute = (Attribute) responseUtil.buildXMLObject(Attribute.DEFAULT_ELEMENT_NAME);
       	attribute.setName("urn:lfg:" + attributeName );
       	attribute.setNameFormat("urn:oasis:names:tc:SAML:2.0:attrname-format:basic");
       	attribute.setFriendlyName(attributeName);	
			            
       //	XMLObjectBuilder xmlObjectBuilder = responseUtil.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
			            
       	XSString attributeValue = (XSString)responseUtil.buildXMLObject(XSString.TYPE_NAME);//xmlObjectBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attributeValue.setValue(attributeText);				            
        attribute.getAttributeValues().add(attributeValue);
			            
       	attributeStatement.getAttributes().add(attribute);		            

	}

}
