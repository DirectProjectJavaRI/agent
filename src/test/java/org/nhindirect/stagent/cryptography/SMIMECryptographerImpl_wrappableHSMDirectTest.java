package org.nhindirect.stagent.cryptography;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.common.crypto.impl.AbstractPKCS11TokenKeyStoreProtectionManager;
import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.common.options.OptionsParameter;
import org.nhindirect.stagent.DefaultNHINDAgent;
import org.nhindirect.stagent.OutgoingMessage;
import org.nhindirect.stagent.cert.CertificateResolver;
import org.nhindirect.stagent.cert.WrappedOnDemandX509CertificateEx;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.trust.TrustAnchorResolver;
import org.nhindirect.stagent.utils.TestUtils;

/**
 * These test are designed for testing out an HSM implementation that support key wrapping.  It consists of a skeleton
 * that is generic for use with a keystore manager.  
 * @author Greg Meyer
 *
 */
public class SMIMECryptographerImpl_wrappableHSMDirectTest
{
	static
	{
		CryptoExtensions.registerJCEProviders();
		
	}
	
	protected String oldSensitiveProvider;
	
	
	@BeforeEach
	public void setUp()
	{
		oldSensitiveProvider = CryptoExtensions.getJCESensitiveProviderName();
		
	}
	
	@AfterEach
	public void done()
	{
		OptionsManager.getInstance().setOptionsParameter(
				new OptionsParameter(OptionsParameter.JCE_SENTITIVE_PROVIDER, oldSensitiveProvider));
	}
	
	protected boolean shouldRunTest()
	{
		try
		{
			final String pkcs11ProvName = TestUtils.setupLunaToken();
			if (StringUtils.isEmpty(pkcs11ProvName))
			{
				System.out.println("No HSM detected.  Skipping test.");
				return false;
			}
			
			OptionsManager.getInstance().setOptionsParameter(
					new OptionsParameter(OptionsParameter.JCE_SENTITIVE_PROVIDER, pkcs11ProvName));
			
		}
		catch (Exception e)
		{
			System.out.println("No HSM detected.  Skipping test.");
			return false;
		}
		

		
		return true;
	}
	
	protected AbstractPKCS11TokenKeyStoreProtectionManager getKeyStoreMgr() throws Exception
	{
		AbstractPKCS11TokenKeyStoreProtectionManager mgr = TestUtils.getLunaKeyStoreMgr();
		
		return mgr;
	}
	
	@Test
	public void testEncryptSingDecryptWithWrappedKey() throws Exception
	{
		if (shouldRunTest())
		{
			
			// Load a certificate and private key and create wrapped representation of it
        	KeyStore localKeyStore = KeyStore.getInstance("PKCS12", CryptoExtensions.getJCEProviderName());
        	
        	InputStream inStream = FileUtils.openInputStream(new File("./src/test/resources/certs/hsmtest.p12"));	
        	localKeyStore.load(inStream, "".toCharArray());
        	Enumeration<String> aliases = localKeyStore.aliases();

			String alias = aliases.nextElement();
			X509Certificate theCert = (X509Certificate)localKeyStore.getCertificate(alias);
			
			// get the private key
			PrivateKey thePrivKey = (PrivateKey)localKeyStore.getKey(alias, "".toCharArray());		
			
			// wrap it up
			final AbstractPKCS11TokenKeyStoreProtectionManager keyMgr = getKeyStoreMgr();
			byte[] wrappedKey = keyMgr.wrapWithSecretKey((SecretKey)keyMgr.getPrivateKeyProtectionKey(), thePrivKey);
			
			// Create an X509CertificateEx with the wrapped key
			X509CertificateEx wrappedCert = WrappedOnDemandX509CertificateEx.fromX509Certificate(keyMgr, theCert, wrappedKey);
			
			// now create an agent instance with mocked stores
			final CertificateResolver resolver = mock(CertificateResolver.class);
			when(resolver.getCertificates(any())).thenReturn(Arrays.asList(wrappedCert));
			
			TrustAnchorResolver trustResolver = mock(TrustAnchorResolver.class);
			when(trustResolver.getIncomingAnchors()).thenReturn(resolver);
			when(trustResolver.getOutgoingAnchors()).thenReturn(resolver);
			
			DefaultNHINDAgent agent = new DefaultNHINDAgent("messaging.cerner.com", 
					resolver, resolver, trustResolver);
			
			
			MimeMessage msg = new MimeMessage((Session)null);
			msg.setFrom(new InternetAddress("hsmtest@messaging.cerner.com"));
			msg.addRecipient(RecipientType.TO, new InternetAddress("hsmtest@messaging.cerner.com"));
			msg.setText("Hello");
			msg.saveChanges();
			
			OutgoingMessage encrytpedMsg =  agent.processOutgoing(new OutgoingMessage(new Message(msg)));
			
			agent.processIncoming(encrytpedMsg.getMessage());
		}
	}
}
