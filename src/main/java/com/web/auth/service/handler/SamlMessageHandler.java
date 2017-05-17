package com.web.auth.service.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;


public class SamlMessageHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public boolean handleMessage(SOAPMessageContext context) {

		System.out.println("Server : handleMessage()......");

		Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		// for response message only, true for outbound messages, false for
		// inbound
		if (!isRequest) {

			System.out.println("Server : inbound message()......");
			try {
				SOAPMessage soapMsg = context.getMessage();
				//SOAPEnvelope soapEnv = soapMsg.getSOAPPart().getEnvelope();
				//SOAPHeader soapHeader = soapEnv.getHeader();
				//SOAPBody soapBody = soapEnv.getBody();
				
				//soapBody.getFirstChild();
				
				
				//System.out.println(soapMsg.toString());
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				soapMsg.writeTo(stream);
				System.out.println(new String(stream.toByteArray(), "utf-8"));
				
				//System.out.println(soapBody.toString());
/*				// if no header, add one
				if (soapHeader == null) {
					soapHeader = soapEnv.addHeader();
					System.out.println("No SOAP header.");
					// throw exception
					// generateSOAPErrMessage(soapMsg, "No SOAP header.");
				}
*/
				context.put("SOAPBODY", soapMsg.getSOAPBody());
				context.setScope("SOAPBODY", MessageContext.Scope.APPLICATION);

			} catch (Exception e) {
				System.err.println(e);
			}

		} else {
			try {
				SOAPMessage soapMsg = context.getMessage();
				String oldReturnMsg = "<ns2:AssertionIDRequestResponse xmlns:ns2=\"urn:oasis:names:tc:SAML:2.0:protocol\"><return>auth check</return></ns2:AssertionIDRequestResponse>";
				String samlResponse = (String) context.get("ReturnMessge");
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				soapMsg.writeTo(stream);
				String newSoapMsg = new String(stream.toByteArray(), "utf-8");
				
				if (samlResponse != null){
					newSoapMsg = newSoapMsg.substring(0, newSoapMsg.indexOf("<S:Body>")+"<S:Body>".length())+samlResponse+newSoapMsg.substring(newSoapMsg.indexOf("</S:Body>"));
				}
				
				SOAPMessage message = MessageFactory.newInstance().createMessage(null, new StringBufferInputStream(newSoapMsg));
				context.setMessage(message);
				//System.out.println(soapMsg.toString());
				//ByteArrayOutputStream stream = new ByteArrayOutputStream();
				//soapMsg.writeTo(stream);
				//String newSoapMsg = new String(stream.toByteArray(), "utf-8");
				//newSoapMsg.replace(oldReturnMsg, samlResponse);
				//System.out.println(samlResponse.substring(samlResponse.indexOf("<samlp:Response")));
				//System.out.println(newSoapMsg.substring(0, newSoapMsg.indexOf("<S:Body>")+"<S:Body>".length()));
				//System.out.println(newSoapMsg.substring(newSoapMsg.indexOf("</S:Body>")));
				//newSoapMsg = newSoapMsg.substring(0, newSoapMsg.indexOf("<S:Body>")+"<S:Body>".length())+samlResponse.substring(samlResponse.indexOf("<samlp:Response"))+newSoapMsg.substring(newSoapMsg.indexOf("</S:Body>"));
				//System.out.println(newSoapMsg);
				//SOAPMessage message = MessageFactory.newInstance()
				//	    .createMessage(null, new StringBufferInputStream(newSoapMsg));
				
				//
				System.out.println(context.get("ReturnMessge"));
			} catch (SOAPException | IOException e) {
				System.err.println(e);
			}
			
		}

		// continue other handler chain
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {

		System.out.println("Server : handleFault()......");

		return true;
	}

	@Override
	public void close(MessageContext context) {
		System.out.println("Server : close()......");
	}

	@Override
	public Set<QName> getHeaders() {
		System.out.println("Server : getHeaders()......");
		return null;
	}

	private void generateSOAPErrMessage(SOAPMessage msg, String reason) {
		try {
			SOAPBody soapBody = msg.getSOAPPart().getEnvelope().getBody();
			SOAPFault soapFault = soapBody.addFault();
			soapFault.setFaultString(reason);
			throw new SOAPFaultException(soapFault);
		} catch (SOAPException e) {
		}
	}

}
