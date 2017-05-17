package com.web.auth.service;

import java.util.Iterator;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.work.saml2.constants.SamlConstants;
import com.work.saml2.util.SAML2RequestUtil;
import com.work.saml2.util.SAML2ResponseUtil;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;

import org.opensaml.saml2.core.AssertionIDRequest;
import org.opensaml.saml2.core.AuthnRequest;

import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.impl.AssertionIDRequestImpl;
import org.opensaml.saml2.core.impl.ResponseImpl;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.XMLHelper;
import org.apache.axiom.om.OMElement;

@WebService(name = "AuthService", serviceName = "AuthService", targetNamespace = "urn:oasis:names:tc:SAML:2.0:protocol")
@HandlerChain(file = "auth-handler-chain.xml")
public class AuthService {

	@Resource
	private WebServiceContext wsContext;
	private SAML2RequestUtil reqestUtil;
	private SAML2ResponseUtil responseUtil;

	// public void setContext(WebServiceContext wsContext) {
	// this.wsContext = wsContext;
	// }

	private Element getSOAPBodyElement() {
		wsContext.getMessageContext();
		SOAPBody soapBody = (SOAPBody) wsContext.getMessageContext().get("SOAPBODY");
		Element aElement = (Element) soapBody.getFirstChild();
		return aElement;
	}

	@WebMethod
	public String AuthnRequest() {

		String response = null;
		String requestId = null;
		
		AuthnRequest anAuthnRequest;
		try {
			reqestUtil = new SAML2RequestUtil();
			responseUtil = new SAML2ResponseUtil();
			anAuthnRequest = reqestUtil.getAuthnRequestFromElement(getSOAPBodyElement());
			System.out.println(com.work.xml.util.XMLHelper.toXMLString(anAuthnRequest.getDOM()));
			
			requestId = anAuthnRequest.getID();
			String userName = anAuthnRequest.getSubject().getNameID().getValue();
			SubjectConfirmation subjectConfirmation = (SubjectConfirmation)anAuthnRequest.getSubject().getSubjectConfirmations().get(0);
			XSString subjectConfirmationDataValue = (XSString)subjectConfirmation.getSubjectConfirmationData().getOrderedChildren().get(0);
			String passWord = subjectConfirmationDataValue.getValue();	
			
			if (userName.contains("andy") && passWord.equals("43cca4b3de2097b9558efefd0ecc3588")) {
				response = responseUtil.createSAMLResponseForStatus(requestId,SamlConstants.SAML2_SC_SUCCESS,null);	
			} else {
				response = responseUtil.createSAMLResponseForStatus(requestId,SamlConstants.SAML2_SC_AUTHNFAIL,null);	
			}
			
		} catch (UnmarshallingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response = responseUtil.createSAMLResponseForStatus(requestId,SamlConstants.SAML2_SC_AUTHNFAIL,null);	
		}
		
		System.out.println(response);
		wsContext.getMessageContext().put("ReturnMessge", responseUtil.getConsumeString(response));

		return "auth check";
		
	}

	@WebMethod
	public String AssertionIDRequest() {
		// String su = SAMLUtil.createSAMLResponseForStatus("abdy", "000",
		// "ok");
		// System.out.println(su);
		String response = null;

		try {

			AssertionIDRequest anAssertionIDRequest;
			reqestUtil = new SAML2RequestUtil();
			responseUtil = new SAML2ResponseUtil();
			anAssertionIDRequest = reqestUtil.getAssertionIdRequestFromElement(getSOAPBodyElement());

			// OMElement obj = reqUtil.toOmElement(anAssertionIDRequest);
			System.out.println(com.work.xml.util.XMLHelper.toXMLString(anAssertionIDRequest.getDOM()));

			QName qName = anAssertionIDRequest.getElementQName();
			String qLocalPartName = qName.getLocalPart();

			if (!qLocalPartName.equals(AssertionIDRequest.DEFAULT_ELEMENT_LOCAL_NAME)) {
				response = responseUtil.createSAMLResponseForStatus(null, SamlConstants.SAML2_SC_UNSUPPBIN, null);
			} else {
				response = responseUtil.createSAMLResponseForStatus(null, SamlConstants.SAML2_SC_SUCCESS, null);
			}

		} catch (UnmarshallingException e1) {
			e1.printStackTrace();
			response = responseUtil.createSAMLResponseForStatus(null, SamlConstants.SAML2_SC_RESPONDER, null);
		}

		// String response = sru.toStringWithConsume(obj);
		System.out.println(response);
		wsContext.getMessageContext().put("ReturnMessge", responseUtil.getConsumeString(response));

		return "auth check";
	}
	/*
	 * @WebMethod public ResponseImpl AssertionIDRequestII ( @WebParam
	 * AssertionIDRequestImpl aRequest) {
	 * 
	 * SAML2ResponseUtil sru = new SAML2ResponseUtil(); return
	 * (ResponseImpl)sru.createAuthenticationResponse("a"); }
	 */

}
