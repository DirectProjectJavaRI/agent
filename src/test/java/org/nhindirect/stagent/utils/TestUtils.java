package org.nhindirect.stagent.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;

import org.apache.commons.io.FileUtils;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.common.crypto.impl.AbstractPKCS11TokenKeyStoreProtectionManager;
import org.nhindirect.common.crypto.impl.BootstrappedPKCS11Credential;
import org.nhindirect.common.crypto.impl.StaticPKCS11TokenKeyStoreProtectionManager;
import org.nhindirect.stagent.DefaultNHINDAgent;
import org.nhindirect.stagent.NHINDAgentTest;
import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.cert.CertificateResolver;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.cert.impl.KeyStoreCertificateStore;
import org.nhindirect.stagent.cert.impl.UniformCertificateStore;
import org.nhindirect.stagent.cryptography.SMIMECryptographerImpl;
import org.nhindirect.stagent.trust.DefaultTrustAnchorResolver;
import org.nhindirect.stagent.trust.TrustModel;

public class TestUtils 
{
	// base directory for test certificates
	private static final String certBasePath = "src/test/resources/certs/"; 
	
	// base directory for test crls
	private static final String crlBasePath = "src/test/resources/crl/"; 
	
	// use a local key store for tests
	private static KeyStore keyStore;
	
	private static final String internalStorePassword = "h3||0 wor|d";	
	private static final String pkPassword = "pKpa$$wd";
	
	
	static
	{
		try
		{	
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			File fl = new File("testfile");
			int idx = fl.getAbsolutePath().lastIndexOf("testfile");
			
			String path = fl.getAbsolutePath().substring(0, idx);
			
			File internalKeystoreFile = new File(path + "src/test/resources/keystores/internalKeystore");			
			
			FileInputStream inStream = new FileInputStream(internalKeystoreFile);
			
			keyStore.load(inStream, internalStorePassword.toCharArray());	
			
			inStream.close();
		}
		catch (Exception e)
		{
			
		}
	}
	
	public static X509CertificateEx getInternalCert(String alias) throws Exception
	{
		X509Certificate cert = (X509Certificate)keyStore.getCertificate(alias);
		
		return X509CertificateEx.fromX509Certificate(cert, (PrivateKey)keyStore.getKey("user1", pkPassword.toCharArray()));
	}
	
	
	public static X509Certificate getExternalCert(String alias) throws Exception
	{
		return  (X509Certificate)keyStore.getCertificate(alias);		
	}	
	
	public static X509Certificate getInternalCACert(String alias) throws Exception
	{
		return  (X509Certificate)keyStore.getCertificate(alias);		
	}	
	
	public static X509Certificate getExternalCACert(String alias) throws Exception
	{
		return  (X509Certificate)keyStore.getCertificate(alias);		
	}	
	
	 
	public static DefaultNHINDAgent getStockAgent(Collection<String> domains) throws Exception
	{
		File fl = new File("testfile");
		int idx = fl.getAbsolutePath().lastIndexOf("testfile");
		
		String path = fl.getAbsolutePath().substring(0, idx);
		
		String internalKeystoreFile = path + "src/test/resources/keystores/internalKeystore";		
		
		X509Certificate caCert = TestUtils.getExternalCert("cacert");
		X509Certificate externCaCert = TestUtils.getExternalCert("externCaCert");
		X509Certificate secureHealthEmailCACert = TestUtils.getExternalCert("secureHealthEmailCACert");
		X509Certificate msCACert = TestUtils.getExternalCert("msanchor");
		X509Certificate cernerDemos = TestUtils.getExternalCert("cernerDemosCaCert");
		
		// anchors cert validation
		Collection<X509Certificate> anchors = new ArrayList<X509Certificate>();
		anchors.add(caCert);
		anchors.add(externCaCert);
		anchors.add(secureHealthEmailCACert);
		anchors.add(msCACert);
		anchors.add(cernerDemos);	
		

		
		final CertificateResolver resolver = new KeyStoreCertificateStore(internalKeystoreFile, internalStorePassword, pkPassword);
		final Collection<CertificateResolver> certResolvers = Arrays.asList(resolver);
		
		final DefaultTrustAnchorResolver trustAnchorResolvers = new DefaultTrustAnchorResolver(new UniformCertificateStore(anchors));

		final DefaultNHINDAgent agent = new DefaultNHINDAgent(domains, resolver, certResolvers, trustAnchorResolvers, 
				TrustModel.Default, SMIMECryptographerImpl.Default);
		
		return agent;
	}
	
	public static String readResource(String _rec) throws Exception
	{
		
		int BUF_SIZE = 2048;		
		int count = 0;
	
		BufferedInputStream imgStream = new BufferedInputStream(NHINDAgentTest.class.getResourceAsStream(_rec));
				
		ByteArrayOutputStream ouStream = new ByteArrayOutputStream();

		byte buf[] = new byte[BUF_SIZE];
		
		while ((count = imgStream.read(buf)) > -1)
		{
			ouStream.write(buf, 0, count);
		}
		
		try 
		{
			imgStream.close();
		} 
		catch (IOException ieo) 
		{
			throw ieo;
		}
		catch (Exception e)
		{
			throw e;
		}					


		return new String(ouStream.toByteArray());		
	}
	
	public static X509Certificate loadCertificate(String certFileName) throws Exception
	{
		File fl = new File(certBasePath + certFileName);
		
		byte[] data =  FileUtils.readFileToByteArray(fl);
		
		X509Certificate retVal = certFromData(data);//(X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(str);
		
		
		return retVal;
	}	
	
	public static CRL loadCRL(String certFileName) throws Exception
	{
		File fl = new File(crlBasePath + certFileName);
		
		InputStream str =  FileUtils.openInputStream(fl);
		
		CRL retVal = CertificateFactory.getInstance("X.509").generateCRL(str);
		
		str.close();
		
		return retVal;
	}	
	
    public static X509Certificate certFromData(byte[] data)
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
    
    /**
     * used for testing with a pkcs11 token
     * @return The Security provider name if the token is loaded successfully... an empty string other wise 
     * @throws Exception
     */
	@SuppressWarnings("restriction")
	public static String setupSafeNetToken() throws Exception
	{	
		final CallbackHandler handler = new CallbackHandler()
		{
			public void	handle(Callback[] callbacks)
			{
				for (Callback callback : callbacks)
				{
					if (callback instanceof PasswordCallback)
					{		
						
						 ((PasswordCallback)callback).setPassword("1Kingpuff".toCharArray());
					
					}
				}
			}
		};
		
		sun.security.pkcs11.SunPKCS11 p = null;
		

		try
		{
			final String configName = "./src/test/resources/pkcs11Config/pkcs11.cfg";
			p = new sun.security.pkcs11.SunPKCS11(configName);
			Security.addProvider(p);
			p.login(null, handler);

		}
		catch (Exception e)
		{
			return "";
		}

		return p.getName();
	}
    
	
    /**
     * used for testing with a Luna HSM token
     * @return The Security provider name if the token is loaded successfully... an empty string other wise 
     * @throws Exception
     */
	public static String setupLunaToken() throws Exception
	{	

		  final StaticPKCS11TokenKeyStoreProtectionManager mgr = (StaticPKCS11TokenKeyStoreProtectionManager)getLunaKeyStoreMgr();
		  
		  return mgr.getKS().getProvider().getName();
	}	
	
    /**
     * used for testing with a Luna HSM token
     * @throws Exception
     */
	public static AbstractPKCS11TokenKeyStoreProtectionManager getLunaKeyStoreMgr() throws Exception
	{	
		  final BootstrappedPKCS11Credential cred = new BootstrappedPKCS11Credential("1kingpuff");
		  final StaticPKCS11TokenKeyStoreProtectionManager mgr = new StaticPKCS11TokenKeyStoreProtectionManager();
		  mgr.setCredential(cred);
		  mgr.setKeyStoreType("Luna");
		  mgr.setKeyStoreSourceAsString("slot:0");
		  mgr.setKeyStoreProviderName("com.safenetinc.luna.provider.LunaProvider");
		  mgr.setKeyStorePassPhraseAlias("keyStorePassPhrase");
		  mgr.setPrivateKeyPassPhraseAlias("privateKeyPassPhrase");
		  
		  mgr.initTokenStore();
		  
		  return mgr;
	}	
	
}