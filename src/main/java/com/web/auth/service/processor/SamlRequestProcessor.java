package com.web.auth.service.processor;



public class SamlRequestProcessor
{
/*	private static Logger logger = new Logger(SamlRequestProcessor.class);

	private Document doc;

	public SamlRequestProcessor(String samlRequest)
	{
		logger.logDebug("SamlRequestProcessor constructor start ...");
		logger.logDebug("Input XML = [" + samlRequest + "]");

		try
		{
			doc = XMLUtil.loadXMLString(samlRequest, false);
			if (doc == null)
			{
				logger.logError("Could not load input string.");
			}
		}
		catch(InvalidMessageException ime)
		{
			logger.logError("Invalid Message Exception loading input string: " + ime.getMessage(), ime);
		}
		catch (Exception e1)
		{
			logger.logError("Exception loading input string: " + e1.getMessage(), e1);
		}
		logger.logDebug("SamlRequestProcessor constructor end.");

	}

	public UserCredentials getUserCredentials()
	{
		logger.logDebug("getUserCredentials started...");
		if (doc == null)
		{
			logger.logError("SamlRequestProcessor not valid - no XML Document object!");
			return null;
		}

		String userName = XMLUtil.getString(doc, "/samlp:AuthnRequest/saml:Subject/saml:NameID");
		logger.logDebug("userName = [" + userName + "]");
		
		String passWord = XMLUtil.getString(doc, "/samlp:AuthnRequest/saml:Subject/saml:SubjectConfirmation/saml:SubjectConfirmationData");		
		logger.logDebug("password = [" + passWord + "]");

		UserCredentials userdata = new UserCredentials();
		userdata.setUserName(userName);
		userdata.setPassord(passWord);
		logger.logDebug("UserCredentials created.");

		logger.logDebug("getUserCredentials end.");
		return userdata;

	}

	public Version getVersion()
	{
		logger.logDebug("getVersion started...");
		if (doc == null)
		{
			logger.logError("SamlRequestProcessor not valid - no XML Document object!");
			return null;
		}

		Version version = new Version();
		String versionStr = XMLUtil.getString(doc, "/samlp:AuthnRequest/@Version");
		version.setVersion(versionStr);
		return version;
	}

	public String getRequestId()
	{
		logger.logDebug("getRequestId started...");
		if (doc == null)
		{
			logger.logError("SamlRequestProcessor not valid - no XML Document object!");
			return null;
		}

		return XMLUtil.getString(doc, "/samlp:AuthnRequest/@ID");

	}

	public UserCredentials getUserData()
	{
		logger.logDebug("getUserData started...");
		if (doc == null)
		{
			logger.logError("SamlRequestProcessor not valid - no XML Document object!");
			return null;
		}

		String userName = XMLUtil.getString(doc, "/samlp:AttributeQuery/saml:Subject/saml:NameID");
		logger.logDebug("User Name = [" + userName + "]");

		String passWord = XMLUtil.getString(doc, "/samlp:AttributeQuery/saml:Subject/saml:SubjectConfirmation/saml:SubjectConfirmationData");
		logger.logDebug("password = [" + passWord + "]");

		UserCredentials userdata = new UserCredentials();
		userdata.setUserName(userName);
		userdata.setPassord(passWord);
		logger.logDebug("UserCredentials created.");

		logger.logDebug("getUserData end.");
		return userdata;

	}
*/
}
