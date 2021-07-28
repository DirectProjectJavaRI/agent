package org.nhindirect.stagent.cryptography;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMultipart;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.common.crypto.MutableKeyStoreProtectionManager;
import org.nhindirect.common.crypto.PKCS11Credential;
import org.nhindirect.common.crypto.impl.BootstrappedPKCS11Credential;
import org.nhindirect.common.crypto.impl.StaticPKCS11TokenKeyStoreProtectionManager;
import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.cert.impl.CacheableKeyStoreManagerCertificateStore;
import org.nhindirect.stagent.mail.MimeEntity;
import org.nhindirect.stagent.mail.MimeError;
import org.nhindirect.stagent.mail.MimeStandard;
import org.nhindirect.stagent.parser.EntitySerializer;
import org.nhindirect.stagent.utils.TestUtils;

public class CryptographerTest
{	
	@BeforeEach
	public void setUp()
	{
    	CryptoExtensions.registerJCEProviders();
	}
	
	@Test
	public void testEncryptAndDecryptMimeEntityAES128() throws Exception
	{
		testEncryptAndDecryptMimeEntity(EncryptionAlgorithm.AES128, true, false);
	}
	
	@Test
	public void testEncryptAndDecryptMimeEntityAES256() throws Exception
	{
		testEncryptAndDecryptMimeEntity(EncryptionAlgorithm.AES256, true, false);
	}	
	
	@Test
	public void testEncryptAndDecryptMimeEntityRSA_3DES() throws Exception
	{
		testEncryptAndDecryptMimeEntity(EncryptionAlgorithm.RSA_3DES, false, false);
	}		
	
	@Test
	public void testEncryptAndDecryptMimeEntityAES192() throws Exception
	{
		testEncryptAndDecryptMimeEntity(EncryptionAlgorithm.AES192, true, false);
	}	

	@Test
	public void testEncryptAndDecryptMimeEntityRSA_3DES_enforceStrongEncr_assertException() throws Exception
	{
		testEncryptAndDecryptMimeEntity(EncryptionAlgorithm.RSA_3DES, true, true);
	}	
	
	@Test
	public void testEncryptAndDecryptMimeEntityDefaultAlg() throws Exception
	{
		testEncryptAndDecryptMimeEntity(null, true, false);
	}	
	
	private void testEncryptAndDecryptMimeEntity(EncryptionAlgorithm encAlg, boolean enforceStrongEncryption, boolean expectDecException) throws Exception
	{
		X509Certificate cert = TestUtils.getExternalCert("user1");
		
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		if (encAlg != null)
			cryptographer.setEncryptionAlgorithm(encAlg);
		cryptographer.setStrongEncryptionEnforced(enforceStrongEncryption);
		
		
		MimeEntity entity = new MimeEntity();
		entity.setText("Hello world.");
		entity.setHeader(MimeStandard.ContentTypeHeader, "text/plain");
		entity.setHeader(MimeStandard.ContentTransferEncodingHeader, "7bit");
		
		
		MimeEntity encEntity = cryptographer.encrypt(entity, cert);
		assertNotNull(encEntity);
		/*
		 * explicit header checking for compliance with Applicability
		 * Statement v 1.2
		 */
		final ContentType type = new ContentType(encEntity.getContentType());
		assertTrue(type.match(SMIMEStandard.CmsEnvelopeMediaType));
		assertFalse(type.match(SMIMEStandard.CmsEnvelopeMediaTypeAlt));
		
		X509CertificateEx certex = TestUtils.getInternalCert("user1");
		
		if (expectDecException)
		{
			boolean exceptionOccured = false;
			try
			{
				cryptographer.decrypt(encEntity, certex);
			}
			catch (Exception e)
			{
				exceptionOccured = true;
			}
			assertTrue(exceptionOccured);
		}
		else
		{
			MimeEntity decryEntity = cryptographer.decrypt(encEntity, certex);
			
			assertNotNull(decryEntity);
			
			byte[] decryEntityBytes = EntitySerializer.Default.serializeToBytes(decryEntity);
			byte[] entityBytes = EntitySerializer.Default.serializeToBytes(entity);
			
			assertTrue(Arrays.equals(decryEntityBytes, entityBytes));
		}
	}
	
	protected String pkcs11ProviderName;
	
	@Test
	public void testEncryptAndDecryptMimeEntity_hsmDecryption() throws Exception
	{
		pkcs11ProviderName = TestUtils.setupSafeNetToken();
		if (!StringUtils.isEmpty(pkcs11ProviderName))
			testEncryptAndDecryptMimeEntity_hsmDecryption(EncryptionAlgorithm.AES128);
	}	
	
	private void testEncryptAndDecryptMimeEntity_hsmDecryption(EncryptionAlgorithm encAlg) throws Exception
	{
		
		OptionsManager.destroyInstance();
		
		CryptoExtensions.registerJCEProviders();
		
		try
		{
			final PKCS11Credential cred = new BootstrappedPKCS11Credential("1Kingpuff");
			final MutableKeyStoreProtectionManager mgr = new StaticPKCS11TokenKeyStoreProtectionManager(cred, "", "");
			final CacheableKeyStoreManagerCertificateStore store = new CacheableKeyStoreManagerCertificateStore(mgr);
			store.add(TestUtils.getInternalCert("user1"));
			
			X509Certificate cert = TestUtils.getExternalCert("user1");
			
			SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
			
			cryptographer.setEncryptionAlgorithm(encAlg);
			
			MimeEntity entity = new MimeEntity();
			entity.setText("Hello world.");
			entity.setHeader(MimeStandard.ContentTypeHeader, "text/plain");
			entity.setHeader(MimeStandard.ContentTransferEncodingHeader, "7bit");
			
			MimeEntity encEntity = cryptographer.encrypt(entity, cert);
			
			assertNotNull(encEntity);
			
			// open up the pkcs11 store and find the private key
			KeyStore ks = KeyStore.getInstance("PKCS11");
			ks.load(null, "1Kingpuff".toCharArray()); 
			

			X509CertificateEx decryptCert = null;
			
			final Enumeration<String> aliases = ks.aliases();
			while (aliases.hasMoreElements())
			{
				String alias = aliases.nextElement();
				
				Certificate pkcs11Cert = ks.getCertificate(alias);
				if (pkcs11Cert != null &&pkcs11Cert instanceof X509Certificate)
				{
					
					// check if there is private key
					Key key = ks.getKey(alias, null);
					if (key != null && key instanceof PrivateKey && CryptoExtensions.certSubjectContainsName((X509Certificate)pkcs11Cert, "user1@cerner.com"))
					{
						decryptCert = X509CertificateEx.fromX509Certificate((X509Certificate)pkcs11Cert, (PrivateKey)key);
						break;
					}
				}
			}		
			
			MimeEntity decryEntity = cryptographer.decrypt(encEntity, decryptCert);
			
			assertNotNull(decryEntity);
			
			byte[] decryEntityBytes = EntitySerializer.Default.serializeToBytes(decryEntity);
			byte[] entityBytes = EntitySerializer.Default.serializeToBytes(entity);
			
			assertTrue(Arrays.equals(decryEntityBytes, entityBytes));
		}		
		finally
		{
			System.setProperty("org.nhindirect.stagent.cryptography.JCESensitiveProviderName", "");
			System.setProperty("org.nhindirect.stagent.cryptography.JCESensitiveProviderClassNames", "");
			
			OptionsManager.destroyInstance();
		}
	}
	
	@Test
	public void testEncryptAndDecryptMultipartEntityAES128() throws Exception
	{
		testEncryptAndDecryptMultipartEntity(EncryptionAlgorithm.AES128, true);
	}
	
	@Test
	public void testEncryptAndDecryptMultipartEntityAES192() throws Exception
	{
		testEncryptAndDecryptMultipartEntity(EncryptionAlgorithm.AES192, true);
	}
	
	@Test
	public void testEncryptAndDecryptMultipartEntityAES256() throws Exception
	{
		testEncryptAndDecryptMultipartEntity(EncryptionAlgorithm.AES256, true);
	}	
	
	@Test
	public void testEncryptAndDecryptMultipartEntityRSA_3DES() throws Exception
	{
		testEncryptAndDecryptMultipartEntity(EncryptionAlgorithm.RSA_3DES, false);
	}

	
	private void testEncryptAndDecryptMultipartEntity(EncryptionAlgorithm encAlgo, boolean enforceStrongEncryption) throws Exception
	{		
		X509Certificate cert = TestUtils.getExternalCert("user1");
		
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		cryptographer.setEncryptionAlgorithm(encAlgo);
		cryptographer.setStrongEncryptionEnforced(enforceStrongEncryption);
		
		MimeEntity entityText = new MimeEntity();
		entityText.setText("Hello world.");
		entityText.setHeader(MimeStandard.ContentTypeHeader, "text/plain");
		entityText.setHeader(MimeStandard.ContentTransferEncodingHeader, "7bit");
		
		MimeEntity entityXML = new MimeEntity();
		entityXML.setText("<Test></Test>");
		entityXML.setHeader(MimeStandard.ContentTypeHeader, "text/xml");		
		
		MimeMultipart mpEntity = new MimeMultipart();
		
		mpEntity.addBodyPart(entityText);
		mpEntity.addBodyPart(entityXML);
		
		MimeEntity encEntity = cryptographer.encrypt(mpEntity, cert);
		
		assertNotNull(encEntity);
		
		X509CertificateEx certex = TestUtils.getInternalCert("user1");
		
		MimeEntity decryEntity = cryptographer.decrypt(encEntity, certex);
		
		assertNotNull(decryEntity);
		
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		mpEntity.writeTo(oStream);
		InternetHeaders hdrs = new InternetHeaders();
		hdrs.addHeader(MimeStandard.ContentTypeHeader, mpEntity.getContentType());
		MimeEntity orgEntity = new MimeEntity(hdrs, oStream.toByteArray());
		
		byte[] decryEntityBytes = EntitySerializer.Default.serializeToBytes(decryEntity);
		byte[] entityBytes = EntitySerializer.Default.serializeToBytes(orgEntity);

		System.out.println("Original:\r\n" + new String(entityBytes));
		System.out.println("\r\n\r\n\r\nNew:\r\n" + new String(decryEntityBytes));		
		
		
		assertTrue(Arrays.equals(decryEntityBytes, entityBytes));		
		
	
	}	
	
	@Test
	public void testSignMimeEntitySHA256() throws Exception
	{
		testSignMimeEntity(DigestAlgorithm.SHA256WITHRSA);
	}	
	
	@Test
	public void testSignMimeEntitySHA384() throws Exception
	{
		testSignMimeEntity(DigestAlgorithm.SHA384WITHRSA);
	}	
	
	@Test
	public void testSignMimeEntitySHA512() throws Exception
	{
		testSignMimeEntity(DigestAlgorithm.SHA512WITHRSA);
	}		
	
	private void testSignMimeEntity(DigestAlgorithm digAlg) throws Exception
	{	
		X509CertificateEx certex = TestUtils.getInternalCert("user1");
		
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		cryptographer.setDigestAlgorithm(digAlg);
		
		MimeEntity entity = new MimeEntity();
		entity.setText("Hello world.");
		entity.setHeader(MimeStandard.ContentTypeHeader, "text/plain");
		entity.setHeader(MimeStandard.ContentTransferEncodingHeader, "7bit");
		
		SignedEntity signedEnt = cryptographer.sign(entity, certex);
		
		assertNotNull(signedEnt);
		
		byte[] signedEntityBytes = EntitySerializer.Default.serializeToBytes(signedEnt.getContent());
		byte[] entityBytes = EntitySerializer.Default.serializeToBytes(entity);		
		
		assertTrue(Arrays.equals(signedEntityBytes, entityBytes));
		assertNotNull(signedEnt.getSignature());
		
		X509Certificate cert = TestUtils.getExternalCert("user1");
			
		
		cryptographer.checkSignature(signedEnt, cert, new ArrayList<X509Certificate>());
	}

	@Test
	public void testSignMimeEntity_SHA1Digest_assertNotAllowedAlgorithm() throws Exception
	{	
		
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		Assertions.assertThrows(IllegalArgumentException.class, () ->{
			cryptographer.setDigestAlgorithm(DigestAlgorithm.SHA1WITHRSA);
		});
		
	}
	
	@Test
	public void testSignMimeEntity_SHA256Digest_forceStrongDigest_assertValidation() throws Exception
	{	
		X509CertificateEx certex = TestUtils.getInternalCert("user1");
		
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		cryptographer.setDigestAlgorithm(DigestAlgorithm.SHA256WITHRSA);
		
		MimeEntity entity = new MimeEntity();
		entity.setText("Hello world.");
		entity.setHeader(MimeStandard.ContentTypeHeader, "text/plain");
		entity.setHeader(MimeStandard.ContentTransferEncodingHeader, "7bit");
		
		SignedEntity signedEnt = cryptographer.sign(entity, certex);
		
		assertNotNull(signedEnt);
		
		byte[] signedEntityBytes = EntitySerializer.Default.serializeToBytes(signedEnt.getContent());
		byte[] entityBytes = EntitySerializer.Default.serializeToBytes(entity);		
		
		assertTrue(Arrays.equals(signedEntityBytes, entityBytes));
		assertNotNull(signedEnt.getSignature());
		
		X509Certificate cert = TestUtils.getExternalCert("user1");
			
		cryptographer.checkSignature(signedEnt, cert, new ArrayList<X509Certificate>());

		
	}
	
	@Test
	public void testSignMimeEntity_SHA1Digest_assertNotAllowedDigestAlgorithm() throws Exception
	{	
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		
		Assertions.assertThrows(IllegalArgumentException.class, () ->{
			cryptographer.setDigestAlgorithm(DigestAlgorithm.SHA1WITHRSA);
		});

		
		
	}
	
	@Test
	public void testEncryptAndSignMimeEntity() throws Exception
	{	
		X509Certificate cert = TestUtils.getInternalCACert("user1");
		
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		
		MimeEntity entity = new MimeEntity();
		entity.setText("Hello world.");
		entity.setHeader(MimeStandard.ContentTypeHeader, "text/plain");
		entity.setHeader(MimeStandard.ContentTransferEncodingHeader, "7bit");

		MimeEntity encEntity = cryptographer.encrypt(entity, cert);
		
		assertNotNull(encEntity);
		
		X509CertificateEx certex = TestUtils.getInternalCert("user1");

		SignedEntity signedEnt = cryptographer.sign(entity, certex);
		
		assertNotNull(signedEnt);

		cryptographer.checkSignature(signedEnt, cert, new ArrayList<X509Certificate>());

	}

	@Test
	public void testEncryptWithSingleCert_wrongDecryptCert_assertFailDecrypt() throws Exception
	{
		X509Certificate cert = TestUtils.getExternalCert("user1");
		
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		
		MimeEntity entity = new MimeEntity();
		entity.setText("Hello world.");
		entity.setHeader(MimeStandard.ContentTypeHeader, "text/plain");
		entity.setHeader(MimeStandard.ContentTransferEncodingHeader, "7bit");
		
		
		MimeEntity encEntity = cryptographer.encrypt(entity, cert);
		
		assertNotNull(encEntity);
		
		X509CertificateEx certex = TestUtils.getInternalCert("altnameonly");
		
		boolean exceptionOccured = false;
		try
		{
			cryptographer.decrypt(encEntity, certex);
		}
		catch (NHINDException e)
		{
			if (e.getError().equals(MimeError.Unexpected));
				exceptionOccured = true;
		}
		assertTrue(exceptionOccured);
	}
	
	@Test
	public void testEncryptWithSingleCert_decryptWithMutlipeCerts_onlyOneCertCorrect_assertDecrypted() throws Exception
	{
		X509Certificate cert = TestUtils.getExternalCert("user1");
		
		SMIMECryptographerImpl cryptographer = new SMIMECryptographerImpl();
		
		MimeEntity entity = new MimeEntity();
		entity.setText("Hello world.");
		entity.setHeader(MimeStandard.ContentTypeHeader, "text/plain");
		entity.setHeader(MimeStandard.ContentTransferEncodingHeader, "7bit");
		
		
		MimeEntity encEntity = cryptographer.encrypt(entity, cert);
		
		assertNotNull(encEntity);
		
		X509CertificateEx certex1 = TestUtils.getInternalCert("altnameonly");
		X509CertificateEx certex2 = TestUtils.getInternalCert("user1");

		MimeEntity decryEntity = cryptographer.decrypt(encEntity, Arrays.asList(certex1, certex2));

		assertNotNull(decryEntity);
		
		byte[] decryEntityBytes = EntitySerializer.Default.serializeToBytes(decryEntity);
		byte[] entityBytes = EntitySerializer.Default.serializeToBytes(entity);
		
		assertTrue(Arrays.equals(decryEntityBytes, entityBytes));
	}	
	
	@SuppressWarnings("deprecation")
	public void testvalidateSignature() throws Exception
	{
		final String str = FileUtils.readFileToString(new File("./src/test/resources/org/nhindirect/stagent/msgSig.txt"));
		
		byte[] byteData = Base64.decode(str);
	
		
       	CMSSignedData signed = new CMSSignedData(byteData);
		
       	Store<X509CertificateHolder> certs = signed.getCertificates();
       	
       	
        for (X509CertificateHolder cert : certs.getMatches(null))
        {
        	FileUtils.writeByteArrayToFile(new File("./testCert.der"), cert.getEncoded());
        }
	}	
}
