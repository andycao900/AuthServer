package com.web.auth.service.processor;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.xml.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.web.auth.service.cache.CacheHandler;
import com.web.auth.service.model.AuthnResponse;
import com.web.auth.service.model.UserCredentials;
import com.work.saml2.util.SAML2ResponseUtil;


public class SamlResponseProcessor {

	private static Logger logger = LoggerFactory
	.getLogger(SamlResponseProcessor.class);

    
	public static String constructAuthenticationResponse(AuthnResponse respbean)
	{
		String strAuthResponseXML = null;
		
		SAML2ResponseUtil responseUtil = new SAML2ResponseUtil();
		UserCredentials credentials = respbean.getUserData();
		
        Response response = (Response) responseUtil.buildXMLObject(Response.DEFAULT_ELEMENT_NAME);
        response.setSchemaLocation("urn:oasis:names:tc:SAML:2.0:protocol http://docs.oasis-open.org/security/saml/v2.0/saml-schema-protocol-2.0.xsd urn:oasis:names:tc:SAML:2.0:assertion http://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd");        
        response.addNamespace(new Namespace("http://www.w3.org/2001/XMLSchema-instance", "xsi"));        
        response.addNamespace(new Namespace("urn:oasis:names:tc:SAML:2.0:assertion", "saml")); 
        
        
        DateTime now = new DateTime(ISOChronology.getInstanceUTC());
        now = now.minusMinutes(3);
        
        String responseId = responseUtil.generateRandomId();        
        response.setID(responseId);        
        response.setInResponseTo(respbean.getInResponseTo());
        response.setIssueInstant(now);

        
        //QName qname = new QName(SAMLConstants.SAML20_NS, Assertion.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);        
        //Assertion assertion = (Assertion) SAMLUtil.buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME);
	    Assertion assertion = (Assertion) responseUtil.buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME);	    
	    

	    //assertion.setID(respbean.getToken());	  
	    String assertionId = responseUtil.generateRandomId();	    
	    assertion.setID(assertionId);
	    assertion.setVersion(SAMLVersion.VERSION_20);
        assertion.setIssueInstant(now);        
//      assertion.setSchemaLocation("urn:oasis:names:tc:SAML:2.0:assertion http://docs.oasis-open.org/security/saml/v2.0/saml-schema-assertion-2.0.xsd");
//      assertion.addNamespace(new Namespace("http://www.w3.org/2001/XMLSchema-instance", "xsi"));        
        
    	logger.debug("Assertion crearted using qname: "+assertion);

    	

        

        QName issuerQName = new QName(SAMLConstants.SAML20_NS, Issuer.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Issuer aIssuer = (Issuer)responseUtil.buildXMLObject(issuerQName);	      
        //Issuer aIssuer = (Issuer) SAMLUtil.buildXMLObject(Issuer.DEFAULT_ELEMENT_NAME);
        aIssuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        aIssuer.setValue("authendpoint");//(AuthUtil.getIDPUrl());
        
        Issuer rIssuer = (Issuer)responseUtil.buildXMLObject(issuerQName);	      
        rIssuer.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        rIssuer.setValue("authendpoint");//(AuthUtil.getIDPUrl());        
        
        QName subjectQName = new QName(SAMLConstants.SAML20_NS, Subject.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Subject subject = (Subject)responseUtil.buildXMLObject(subjectQName);
//        Subject subject = (Subject) SAMLUtil.buildXMLObject(Subject.DEFAULT_ELEMENT_NAME);
        NameID nameID = (NameID) responseUtil.buildXMLObject(NameID.DEFAULT_ELEMENT_NAME);
        nameID.setFormat("urn:oasis:names:tc:1.1:nameid-format:unspecified");
        nameID.setValue(credentials.getUserName());
        
        QName subjectConfQName = new QName(SAMLConstants.SAML20_NS, SubjectConfirmation.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
//        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) SAMLUtil.buildXMLObject(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) responseUtil.buildXMLObject(subjectConfQName);
        subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        
        QName subjectConfDataQName = new QName(SAMLConstants.SAML20_NS, SubjectConfirmationData.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        SubjectConfirmationData subjectConfirmationData = (SubjectConfirmationData) responseUtil.buildXMLObject(subjectConfDataQName);
        //@TODO need to get this from request messagee
        subjectConfirmationData.setRecipient("https://www.lfg.com");
        subjectConfirmationData.setNotOnOrAfter(now.plusMinutes(33));
        //@TODO : When we change how the request comes - i.e., saml request coming inside request tag
        //below attribute should match the request id of the incoming request
        subjectConfirmationData.setInResponseTo("xxxxxx");      
        
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        
        QName conditionsQName = new QName(SAMLConstants.SAML20_NS, Conditions.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        Conditions conditions = (Conditions) responseUtil.buildXMLObject(conditionsQName);
//        Conditions conditions = (Conditions) SAMLUtil.buildXMLObject(Conditions.DEFAULT_ELEMENT_NAME);        
        conditions.setNotBefore(now);
        conditions.setNotOnOrAfter(now.plusMinutes(33));
        		

        QName authnStatementQName = new QName(SAMLConstants.SAML20_NS, AuthnStatement.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);        
        AuthnStatement authnStatement = (AuthnStatement) responseUtil.buildXMLObject(authnStatementQName);
//        AuthnStatement authnStatement = (AuthnStatement) SAMLUtil.buildXMLObject(AuthnStatement.DEFAULT_ELEMENT_NAME);        
        authnStatement.setAuthnInstant(now);

        QName authnContextQName = new QName(SAMLConstants.SAML20_NS, AuthnContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
       // AuthnContext authnContext = (AuthnContext) SAMLUtil.buildXMLObject(AuthnContext.DEFAULT_ELEMENT_NAME);
        AuthnContext authnContext = (AuthnContext) responseUtil.buildXMLObject(authnContextQName);
        
        QName authnContextClassRefQName = new QName(SAMLConstants.SAML20_NS, AuthnContextClassRef.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
//        AuthnContextClassRef classRef = (AuthnContextClassRef) SAMLUtil.buildXMLObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        AuthnContextClassRef classRef = (AuthnContextClassRef) responseUtil.buildXMLObject(authnContextClassRefQName);
        classRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
        
	      
      //   assertion.getAuthnStatements().add((AuthnStatement)SAMLUtil.buildXMLObject(authnStatementQName));        	 
        assertion.getAuthnStatements().add(authnStatement);
		
		
         assertion.setIssuer(aIssuer);
         subject.setNameID(nameID);
         subject.getSubjectConfirmations().add(subjectConfirmation);
         assertion.setSubject(subject);

//         audienceRestriction.getAudiences().add(audience);
//         conditions.getAudienceRestrictions().add(audienceRestriction);
         assertion.setConditions(conditions);
         
         authnContext.setAuthnContextClassRef(classRef);
         authnStatement.setAuthnContext(authnContext);
         assertion.getAuthnStatements().add(authnStatement);
         
         
     	//QName statusQname = new QName(SAMLConstants.SAML20_NS, Status.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
         Status status = (Status) responseUtil.buildXMLObject(Status.DEFAULT_ELEMENT_NAME);
     	//Status status = (Status) SAMLUtil.buildXMLObject(statusQname);
         StatusCode statusCode = (StatusCode) responseUtil.buildXMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
         statusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
         status.setStatusCode(statusCode);
         StatusMessage statusMessage = (StatusMessage) responseUtil.buildXMLObject(StatusMessage.DEFAULT_ELEMENT_NAME);
         statusMessage.setMessage("Success");         
         status.setStatusMessage(statusMessage);


         //CacheHandler.getInstance().cacheObject(assertionId,strAuthResponseXML);
         String strAssertionXML = responseUtil.getXMLForSAMLObject(assertion);
         CacheHandler.getInstance().getCacheAdapterImpl().cacheObject(assertionId,strAssertionXML);
         //CacheHandler.getInstance().getCacheAdapterImpl().cacheObject(credentials.getUserName(),assertionId);
         
         response.setIssuer(rIssuer);        
         response.setStatus(status);
         response.getAssertions().add(assertion);
         
         strAuthResponseXML = responseUtil.getXMLForSAMLObject(response);
         logger.debug("Authentication assertion created is:"+strAuthResponseXML);
         
         
         return strAuthResponseXML ;
         
	}
	
	

	
}
