package org.nhindirect.stagent.cryptography;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;

import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.stagent.SignatureValidationException;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.utils.TestUtils;

import java.util.Arrays;

public class SMIMECryptographerImpl_checkSignature_Test
{
	private X509Certificate sigCertA;
	private X509Certificate sigCertAPrivate;
	private X509Certificate sigCertB;
	private X509Certificate sigCertBPrivate;
	private X509Certificate otherCert;
	private X509Certificate sigCertAnchor;
	
	private SignedEntity signedEntity;
	private SMIMECryptographerImpl cryptographer;
	
	@BeforeEach
	public void setUp() throws Exception
	{
    	CryptoExtensions.registerJCEProviders();
		
		// load sigCert A
		sigCertA = TestUtils.loadCertificate("certCheckA.der");
		
		// load sigCert A private certificate
		sigCertAPrivate = TestUtils.loadCertificate("certCheckA.p12");		
		
		// load sigCert B
		sigCertB = TestUtils.loadCertificate("certCheckB.der");

		// load sigCert B
		sigCertBPrivate = TestUtils.loadCertificate("certCheckB.p12");

		
		// load sigCert anchor
		sigCertAnchor = TestUtils.loadCertificate("Check Signature CA.der");

		// load other anchor
		otherCert = TestUtils.loadCertificate("gm2552.der");

		
		// load the message that will be encrypted
		String testMessage = TestUtils.readResource("MultipartMimeMessage.txt");
		cryptographer = new SMIMECryptographerImpl();
		
		signedEntity = cryptographer.sign(new Message(new ByteArrayInputStream(testMessage.getBytes())), sigCertAPrivate);
		
	}
	
	@Test
	public void testValidateSig_sameSignAndValidationCert_assertValidSignature() throws Exception
	{
		cryptographer.checkSignature(signedEntity, sigCertA, Arrays.asList(sigCertAnchor));

	}
	
	@Test
	public void testValidateSig_differentSignAndValidationCert_sameCA_assertInValidSignature() throws Exception
	{
		boolean exceptionOccured = false;
		try
		{
			cryptographer.checkSignature(signedEntity, sigCertB, Arrays.asList(sigCertAnchor));
		}
		catch (SignatureValidationException e)
		{
			exceptionOccured = true;
		}
		
		assertTrue(exceptionOccured);
	}
	
	@Test
	public void testValidateSig_diffSignAndValidationCert_assertInvalidSignature() throws Exception
	{
		boolean exceptionOccured = false;
		try
		{
			cryptographer.checkSignature(signedEntity, otherCert, Arrays.asList(sigCertAnchor));
		}
		catch (SignatureValidationException e)
		{
			exceptionOccured = true;
		}
		
		assertTrue(exceptionOccured);
	}

	@Test
	public void testMutlipleSigs_sameSignAndValidationCert_assertValidSignature() throws Exception
	{
		// load the message that will be encrypted
		String testMessage = TestUtils.readResource("MultipartMimeMessage.txt");
		cryptographer = new SMIMECryptographerImpl();
		
		signedEntity = cryptographer.sign(new Message(new ByteArrayInputStream(testMessage.getBytes())), Arrays.asList(sigCertAPrivate, sigCertBPrivate));

		
		cryptographer.checkSignature(signedEntity, sigCertA, Arrays.asList(sigCertAnchor));
		cryptographer.checkSignature(signedEntity, sigCertB, Arrays.asList(sigCertAnchor));

	}

	
}