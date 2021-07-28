package org.nhindirect.stagent.cryptography;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

import javax.mail.BodyPart;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.mail.smime.CMSProcessableBodyPart;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.common.options.OptionsManagerUtils;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.mail.MimeEntity;
import org.nhindirect.stagent.parser.EntitySerializer;
import org.nhindirect.stagent.utils.TestUtils;


public class SMIMECryptographerImpl_createSignatureEntityTest
{
	protected String pkcs11ProvName;
	
	
	@BeforeEach
	public void setUp() throws Exception
	{
		OptionsManagerUtils.clearOptionsManagerOptions();
		OptionsManagerUtils.clearOptionsManagerInstance();
    	CryptoExtensions.registerJCEProviders();
	}
	
	@Test
	public void tearDown()
	{
		OptionsManagerUtils.clearOptionsManagerOptions();
		OptionsManagerUtils.clearOptionsManagerInstance();
		CryptoExtensions.registerJCEProviders();
	}
	
	
	protected MimeEntity contentToMimeEntity(BodyPart part) throws Exception
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		part.writeTo(bos);
	    bos.flush(); 	    

        InputStream stream = new ByteArrayInputStream(bos.toByteArray());

        return new MimeEntity(stream);
	}
	
	/*
	 * This is the control test
	 */
	@Test
	public void testCreateSignatureEntity_defaultSigGenerator_assertEntityCreated() throws Exception
	{
		final SMIMECryptographerImpl impl = new SMIMECryptographerImpl();
		
		final String testMessage = TestUtils.readResource("MultipartMimeMessage.txt");
		
		final MimeEntity ent = new Message(new ByteArrayInputStream(testMessage.getBytes())).extractEntityForSignature(true);
		
		byte[] bytesToSign = EntitySerializer.Default.serializeToBytes(ent); 
		
		final X509Certificate sigCertBPrivate = TestUtils.loadCertificate("certCheckB.p12");
		
		final MimeMultipart mm = impl.createSignatureEntity(bytesToSign, Arrays.asList(sigCertBPrivate));

		assertNotNull(mm);
		assertEquals(2, mm.getCount());
		
		validatedSignatureHeaders(mm);
		
		validateSignature(deserializeSignatureEnvelope(mm), sigCertBPrivate);
	}
	

	@Test
	public void testCreateSignatureEntity_hsmSignatureGenerator_assertEntityCreatedAndMatchesControl() throws Exception
	{
		final String installedAlias = "JunitTestKey";
		
        /**
         * This test is only run if a specific SafeNet eToken Pro HSM is connected to the testing 
         * system.  This can be modified for another specific machine and/or token.
         */
		pkcs11ProvName = TestUtils.setupSafeNetToken();
		if (!StringUtils.isEmpty(pkcs11ProvName))
		{
			// get a certificate from the key store
			final KeyStore ks = KeyStore.getInstance("PKCS11");
			
			ks.load(null, "1Kingpuff".toCharArray());
			
			// delete the entry in case it exists
			try
			{
				ks.deleteEntry(installedAlias);
			}
			catch (Exception e) {/*no-op */}
			
			// add the signing cert and private key into the token
			final X509Certificate sigCertBPrivate = (X509CertificateEx)TestUtils.loadCertificate("certCheckB.p12");
				
			try
			{	
				ks.setKeyEntry(installedAlias, ((X509CertificateEx)sigCertBPrivate).getPrivateKey(), null, new Certificate[] {sigCertBPrivate});
				
				final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry)ks.getEntry(installedAlias, null);
	
				final X509Certificate signerCert = X509CertificateEx.fromX509Certificate((X509Certificate)entry.getCertificate(), entry.getPrivateKey());
				
				CryptoExtensions.setJCEProviderName(pkcs11ProvName);
	
				final SMIMECryptographerImpl impl = new SMIMECryptographerImpl();
				
				final String testMessage = TestUtils.readResource("MultipartMimeMessage.txt");
				
				final MimeEntity ent = new Message(new ByteArrayInputStream(testMessage.getBytes())).extractEntityForSignature(true);
				
				byte[] bytesToSign = EntitySerializer.Default.serializeToBytes(ent); 
				
				final MimeMultipart mm = impl.createSignatureEntity(bytesToSign, Arrays.asList(signerCert));
				
				assertNotNull(mm);
				assertEquals(2, mm.getCount());
				
				validatedSignatureHeaders(mm);
				
				// now create the control
				final SMIMECryptographerImpl controllImpl = new SMIMECryptographerImpl();
				final MimeMultipart controllmm = controllImpl.createSignatureEntity(bytesToSign, Arrays.asList(sigCertBPrivate));
				assertNotNull(controllmm);
				assertEquals(2, controllmm.getCount());
				
				// make sure the signatures can be verified
				// the actual byte data may not be the same due to 
				// randomness in the signature
				validateSignature(deserializeSignatureEnvelope(mm), sigCertBPrivate);
				validateSignature(deserializeSignatureEnvelope(controllmm), sigCertBPrivate);
				
			}
			finally
			{
				ks.deleteEntry(installedAlias);
			}
		}
	}
	protected void validateSignature(CMSSignedData data, X509Certificate signerCert) throws Exception
	{
    	assertNotNull(data);
    	    	
		assertEquals(1, data.getSignerInfos().getSigners().size());
		for (SignerInformation sigInfo : (Collection<SignerInformation>)data.getSignerInfos().getSigners())	
		{
    		assertTrue(sigInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(CryptoExtensions.getJCEProviderName()).build(signerCert)));
    		/*
    		 * explicit hash algorithm checking for compliance with Applicability
    		 * Statement v 1.2
    		 */    		
    		assertEquals(DigestAlgorithm.SHA256.getOID(), sigInfo.getDigestAlgOID());
		}
	}
	
	protected void validatedSignatureHeaders(MimeMultipart mm) throws Exception
	{
		/*
		 * explicit header checking for compliance with Applicability
		 * Statement v 1.2
		 */
		final ContentType type = new ContentType(mm.getContentType());
		assertTrue(type.match("multipart/signed"));
		assertEquals(SMIMEStandard.SignatureContentMediaType, type.getParameter("protocol"));
		assertEquals(CryptoAlgorithmsHelper.toDigestAlgorithmMicalg(DigestAlgorithm.SHA256), type.getParameter("micalg"));
		final ContentType signedTypetype = new ContentType(mm.getBodyPart(1).getContentType());
		assertTrue(signedTypetype.match(SMIMEStandard.SignatureContentMediaType));
	}
	
    protected CMSSignedData deserializeSignatureEnvelope(MimeMultipart mm) throws Exception
    {
    	final MimeEntity contentEntity = contentToMimeEntity(mm.getBodyPart(0));
    	
    	byte[] messageBytes = EntitySerializer.Default.serializeToBytes(contentEntity);
        MimeBodyPart signedContent = null;
            
        signedContent = new MimeBodyPart(new ByteArrayInputStream(messageBytes));
                     
        return new CMSSignedData(new CMSProcessableBodyPart(signedContent), mm.getBodyPart(1).getInputStream());
    }
}
