package com.web.auth.service.util;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.*;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.work.saml2.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtils
{

    public SecurityUtils()
    {
    }

    public static String encrypt(String msg)
    {
        long t = System.currentTimeMillis() / 1000L;
        if (logger.isDebugEnabled())logger.info("time: [" + t + "]");
        StringBuffer sb = new StringBuffer(512);
        sb.append("time=").append(t);
        sb.append(msg);
        String sigData = sb.toString();
        byte raw[] = fromHexString("63ED0D18634D4c03A53AB622C7DD6810");
        Security.addProvider(new BouncyCastleProvider());
        String encryptData = null;
        Cipher cipher = null;
        try
        {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "Blowfish");
            if (logger.isDebugEnabled())logger.debug("Got secret key!");
            cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding", "BC");
            cipher.init(1, skeySpec);
            byte encryptedBytes[] = cipher.doFinal(sigData.getBytes());
            encryptData = (new BASE64Encoder()).encode(encryptedBytes);
        }
        catch(Exception e)
        {
            logger.info("something bad happened trying to get a cipher",e);
        }
        return encryptData;
    }

    public static String decrypt(String msg) throws MarshallingException
    {
    	if (logger.isDebugEnabled())logger.debug("EncMsg: " + msg);
        byte raw[] = fromHexString("63ED0D18634D4c03A53AB622C7DD6810");
        Security.addProvider(new BouncyCastleProvider());
        String decryptData = null;
        Cipher decipher = null;

        try {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "Blowfish");
            if (logger.isDebugEnabled())logger.debug("Got secret key!");
            decipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding", "BC");
            decipher.init(2, skeySpec);
            byte dec[] = (new BASE64Decoder()).decodeBuffer(msg);
            byte decryptedBytes[] = decipher.doFinal(dec);
            decryptData = new String(decryptedBytes, "UTF8");
           if (logger.isDebugEnabled()) logger.debug("decryptData: " + decryptData);
            
        } 
        catch (NoSuchAlgorithmException noSuchAlgorithmException) 
        {
            logger.error("an unexpected exception was thrown ", noSuchAlgorithmException);
            throw new MarshallingException("SecurityUtils",noSuchAlgorithmException.getMessage(),"10001",IWorkException.SEVERITY_FATAL);
        } 
        catch (NoSuchProviderException noSuchProviderException) 
        {
            logger.error("an unexpected exception was thrown ", noSuchProviderException);
            throw new MarshallingException("SecurityUtils",noSuchProviderException.getMessage(),"10001",IWorkException.SEVERITY_FATAL);            
        } 
        catch (NoSuchPaddingException noSuchPaddingException) 
        {
            logger.error("an unexpected exception was thrown ", noSuchPaddingException);
            throw new MarshallingException("SecurityUtils",noSuchPaddingException.getMessage(),"10001",IWorkException.SEVERITY_FATAL);              
        } 
        catch (InvalidKeyException invalidKeyException) 
        {
            logger.error("an unexpected exception was thrown ", invalidKeyException);
            throw new MarshallingException("SecurityUtils",invalidKeyException.getMessage(),"10001",IWorkException.SEVERITY_FATAL);               
        } 
        catch (IOException iOException) 
        {
            logger.error("an unexpected exception was thrown ", iOException);
            throw new MarshallingException("SecurityUtils",iOException.getMessage(),"10001",IWorkException.SEVERITY_FATAL);                
        } 
        catch (IllegalBlockSizeException illegalBlockSizeException) 
        {
            logger.error("an unexpected exception was thrown ", illegalBlockSizeException);
            throw new MarshallingException("SecurityUtils",illegalBlockSizeException.getMessage(),"10001",IWorkException.SEVERITY_FATAL);             
        }
        catch (BadPaddingException badPaddingException) 
        {
            logger.error("an unexpected exception was thrown ", badPaddingException);
            throw new MarshallingException("SecurityUtils",badPaddingException.getMessage(),"10001",IWorkException.SEVERITY_FATAL);            
        }

        return decryptData;
    }

    public static byte[] fromHexString(String s)
    {
        int stringLength = s.length();
        if((stringLength & 1) != 0)
            throw new IllegalArgumentException("fromHexString requires an even number of hex characters");
        byte b[] = new byte[stringLength / 2];
        int i = 0;
        for(int j = 0; i < stringLength; j++)
        {
            int high = charToNibble(s.charAt(i));
            int low = charToNibble(s.charAt(i + 1));
            b[j] = (byte)(high << 4 | low);
            i += 2;
        }

        return b;
    }

    private static int charToNibble(char c)
    {
        if('0' <= c && c <= '9')
            return c - 48;
        if('a' <= c && c <= 'f')
            return (c - 97) + 10;
        if('A' <= c && c <= 'F')
            return (c - 65) + 10;
        else
            throw new IllegalArgumentException("Invalid hex character: " + c);
    }

    public static String encodeURL(String url) throws UnsupportedEncodingException
    {
    	String output = "";
    	try
    	{
    		output = URLEncoder.encode(url, "UTF-8");
    	}
    	catch (UnsupportedEncodingException uee)
    	{
            logger.error("an unsupported encoding exception was thrown in Security Utils", uee);
            throw uee;
    	}
        return output;
    }

    private static Logger logger;

    static 
    {
        logger = LoggerFactory
		.getLogger(SecurityUtils.class);
    }

}
