package org.nhindirect.stagent.trust;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;


import org.apache.commons.io.FileUtils;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.cert.CertificateResolver;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.cert.impl.KeyStoreCertificateStore;
import org.nhindirect.stagent.cert.impl.UniformCertificateStore;

public class TrustChainValidator_IntermidiateCert_Test
{
	@BeforeEach
	public void setUp()
	{
    	CryptoExtensions.registerJCEProviders();
	}
	
	protected byte[] getCertificateFileData(String file) throws Exception
	{
		File fl = new File("src/test/resources/certs/" + file);
		
		return FileUtils.readFileToByteArray(fl);
	}	
	
    private X509Certificate certFromData(byte[] data)
    {
    	X509Certificate retVal = null;
        try 
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            
            // lets try this a as a PKCS12 data stream first
            try
            {
            	KeyStore localKeyStore = KeyStore.getInstance("PKCS12", CryptoExtensions.getJCEProviderName());
            	
            	localKeyStore.load(bais, "".toCharArray());
            	Enumeration<String> aliases = localKeyStore.aliases();


        		// we are really expecting only one alias 
        		if (aliases.hasMoreElements())        			
        		{
        			String alias = aliases.nextElement();
        			X509Certificate cert = (X509Certificate)localKeyStore.getCertificate(alias);
        			
    				// check if there is private key
    				Key key = localKeyStore.getKey(alias, "".toCharArray());
    				if (key != null && key instanceof PrivateKey) 
    				{
    					retVal = X509CertificateEx.fromX509Certificate(cert, (PrivateKey)key);
    				}
    				else
    					retVal = cert;
    					
        		}
            }
            catch (Exception e)
            {
            	// must not be a PKCS12 stream, go on to next step
            }
   
            if (retVal == null)            	
            {
            	//try X509 certificate factory next       
                bais.reset();
                bais = new ByteArrayInputStream(data);

                retVal = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(bais);            	
            }
            bais.close();
        } 
        catch (Exception e) 
        {
            throw new NHINDException("Data cannot be converted to a valid X.509 Certificate", e);
        }
        
        return retVal;
    }
    
    @Test
    public void testValidateCertAgainstNonRootCA_OpenSSLCerts() throws Exception
    {
    	X509Certificate anchor = certFromData(getCertificateFileData("cert-b.der"));
    	X509Certificate certToValidate = certFromData(getCertificateFileData("cert-a.der"));
    	
    	TrustChainValidator validator = new TrustChainValidator();
    	
    	boolean isTrusted = false;
    	try
    	{	
    		isTrusted = validator.isTrusted(certToValidate, Arrays.asList(anchor));
    	}
    	catch (Exception e) {}
    	
    	assertTrue(isTrusted);
    }    
    
    @Test
    public void testValidateCertAgainstNonRootCA_CAInPublicResolver_OpenSSLCerts() throws Exception
    {
    	X509Certificate anchor = certFromData(getCertificateFileData("cert-b.der"));
    	X509Certificate certToValidate = certFromData(getCertificateFileData("cert-a.der"));
    	
    	// uniform cert store that will just spit out whatever we put in it
    	// will put the anchor in the public resolver... validator should hit it
    	CertificateResolver publicResolver = new UniformCertificateStore(anchor);
    	
    	TrustChainValidator validator = new TrustChainValidator();
    	validator.setCertificateResolver(Arrays.asList(publicResolver));
    	
    	boolean isTrusted = false;
    	try
    	{	
    		isTrusted = validator.isTrusted(certToValidate, Arrays.asList(anchor));
    	}
    	catch (Exception e) {}
    	
    	assertTrue(isTrusted);
    }   
    
    @Test
    public void testValidateCertMissingIntermediateCert_OpenSSLCerts() throws Exception
    {
    	X509Certificate anchor = certFromData(getCertificateFileData("cert-c.der"));
    	X509Certificate certToValidate = certFromData(getCertificateFileData("cert-a.der"));
    	
    	TrustChainValidator validator = new TrustChainValidator();
    	
    	boolean isTrusted = false;
    	try
    	{	
    		isTrusted = validator.isTrusted(certToValidate, Arrays.asList(anchor));
    	}
    	catch (Exception e) {}
    	
    	assertFalse(isTrusted);
    }       
    
    @Test
    public void testValidateCertAgainstSelf_NotSelfSigned_CertGenToolCerts() throws Exception
    {
    	X509Certificate anchor = certFromData(getCertificateFileData("greg@messaging.cerner.com.p12"));
    	X509Certificate certToValidate = certFromData(getCertificateFileData("greg@messaging.cerner.com.p12"));
    	
    	TrustChainValidator validator = new TrustChainValidator();
    	
    	boolean isTrusted = validator.isTrusted(certToValidate, Arrays.asList(anchor));
    	
    	assertTrue(isTrusted);
    }
    
    @Test
    public void testValidateCertAgainstSelf_NotSelfSigned_OpenSSLCerts() throws Exception
    {
    	X509Certificate anchor = certFromData(getCertificateFileData("cert-a.der"));
    	X509Certificate certToValidate = certFromData(getCertificateFileData("cert-a.der"));
    	
    	TrustChainValidator validator = new TrustChainValidator();
    	
    	boolean isTrusted = validator.isTrusted(certToValidate, Arrays.asList(anchor));    	
    	assertTrue(isTrusted);
    }        
    
    @Test
    public void testValidateCertAgainstSelf_SelfSigned_OpenSSLCerts() throws Exception
    {
    	X509Certificate anchor = certFromData(getCertificateFileData("cert-c.der"));
    	X509Certificate certToValidate = certFromData(getCertificateFileData("cert-c.der"));
    	
    	TrustChainValidator validator = new TrustChainValidator();
    	
    	boolean isTrusted = false;
    	try
    	{	
    		isTrusted = validator.isTrusted(certToValidate, Arrays.asList(anchor));
    	}
    	catch (Exception e) {}
    	
    	assertTrue(isTrusted);
    }        
    
    @Test
    public void testValidateCert_FindIntermediateByAltName_AssertValidated() throws Exception
    {
    	X509Certificate anchor = certFromData(getCertificateFileData("Test Alt Name CA ROO.der"));
    	X509Certificate certToValidate = certFromData(getCertificateFileData("altNameOnly.der"));

		CertificateResolver publicCertResolver = new KeyStoreCertificateStore("src/test/resources/keystores/internalKeystore",
				"h3||0 wor|d", "pKpa$$wd");
    	
    	TrustChainValidator validator = new TrustChainValidator();
    	validator.setCertificateResolver(Arrays.asList(publicCertResolver));
    	
    	boolean isTrusted = false;
    	try
    	{	
    		isTrusted = validator.isTrusted(certToValidate, Arrays.asList(anchor));
    	}
    	catch (Exception e) {}
    	
    	assertTrue(isTrusted);
    }       
    
}
