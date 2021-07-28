package org.nhindirect.stagent.cryptography;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.common.options.OptionsManagerUtils;
import org.nhindirect.common.options.OptionsParameter;

public class SMIMECryptographerImpl_constructTest
{
	@BeforeEach
	public void setUp()
	{
		OptionsManagerUtils.clearOptionsManagerInstance();
	}
	
	@AfterEach
	public void tearDown()
	{
		OptionsManagerUtils.clearOptionsManagerOptions();
	}
	
	// Now defaulting to SHA 256
	@Test
	public void testContructSMIMECryptographerImpl_defaultSettings()
	{
		SMIMECryptographerImpl impl = new SMIMECryptographerImpl();
		
		assertEquals(DigestAlgorithm.SHA256WITHRSA, impl.getDigestAlgorithm());
		assertEquals(EncryptionAlgorithm.AES128, impl.getEncryptionAlgorithm());
	}
	
	@Test
	public void testContructSMIMECryptographerImpl_setAlgorithms()
	{
		SMIMECryptographerImpl impl = new SMIMECryptographerImpl(EncryptionAlgorithm.RSA_3DES, DigestAlgorithm.SHA384);
		
		assertEquals(DigestAlgorithm.SHA384, impl.getDigestAlgorithm());
		assertEquals(EncryptionAlgorithm.RSA_3DES, impl.getEncryptionAlgorithm());
	}
	
	@Test
	public void testContructSMIMECryptographerImpl_disallowedDigestAlgorithm_assertException()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () ->{
			new SMIMECryptographerImpl(EncryptionAlgorithm.AES128, DigestAlgorithm.SHA1);
		});
	}
	
	@Test
	public void testContructSMIMECryptographerImpl_JVMSettings()
	{
		System.setProperty("org.nhindirect.stagent.cryptographer.smime.EncryptionAlgorithm", "AES256");
		System.setProperty("org.nhindirect.stagent.cryptographer.smime.DigestAlgorithm", "SHA256");
		
		try
		{
		
			SMIMECryptographerImpl impl = new SMIMECryptographerImpl();
		
			assertEquals(DigestAlgorithm.SHA256, impl.getDigestAlgorithm());
			assertEquals(EncryptionAlgorithm.AES256, impl.getEncryptionAlgorithm());
		}
		finally
		{
			System.setProperty("org.nhindirect.stagent.cryptographer.smime.EncryptionAlgorithm", "");
			System.setProperty("org.nhindirect.stagent.cryptographer.smime.DigestAlgorithm", "");
		}
	}
	
	@Test
	public void testContructSMIMECryptographerImpl_invalidDigestOption_assertException()
	{
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.CRYPTOGRAHPER_SMIME_DIGEST_ALGORITHM, DigestAlgorithm.SHA1.getAlgName()));
		
		Assertions.assertThrows(IllegalArgumentException.class, () ->
		{
			new SMIMECryptographerImpl();
		});	
	}
	
	@Test
	public void testContructSMIMECryptographerImpl_InvalidJVMSettings()
	{
		System.setProperty("org.nhindirect.stagent.cryptographer.smime.EncryptionAlgorithm", "AES256323");
		System.setProperty("org.nhindirect.stagent.cryptographer.smime.DigestAlgorithm", "SHA2564323");
		
		try
		{
		
			SMIMECryptographerImpl impl = new SMIMECryptographerImpl();
		
			assertEquals(DigestAlgorithm.SHA256WITHRSA, impl.getDigestAlgorithm());
			assertEquals(EncryptionAlgorithm.AES128, impl.getEncryptionAlgorithm());
		}
		finally
		{
			System.setProperty("org.nhindirect.stagent.cryptographer.smime.EncryptionAlgorithm", "");
			System.setProperty("org.nhindirect.stagent.cryptographer.smime.DigestAlgorithm", "");
		}
	}
	
	
	@Test
	public void testContructSMIMECryptographerImpl_propFileSettings() throws Exception
	{
		File propFile = new File("./target/props/agentSettings.properties");
		if (propFile.exists())
			propFile.delete();
	
		System.setProperty("org.nhindirect.stagent.PropertiesFile", "./target/props/agentSettings.properties");
		
		try(OutputStream outStream = FileUtils.openOutputStream(propFile))
		{
			outStream.write("org.nhindirect.stagent.cryptographer.smime.EncryptionAlgorithm=AES192\r\n".getBytes());
			outStream.write("org.nhindirect.stagent.cryptographer.smime.DigestAlgorithm=SHA512".getBytes());
			outStream.flush();
			
		}
		finally
		{
		}
		
		try
		{
		
			SMIMECryptographerImpl impl = new SMIMECryptographerImpl();
		
			assertEquals(DigestAlgorithm.SHA512, impl.getDigestAlgorithm());
			assertEquals(EncryptionAlgorithm.AES192, impl.getEncryptionAlgorithm());
		}
		finally
		{
			System.setProperty("org.nhindirect.stagent.cryptographer.smime.EncryptionAlgorithm", "");
			System.setProperty("org.nhindirect.stagent.cryptographer.smime.DigestAlgorithm", "");
			System.setProperty("org.nhindirect.stagent.PropertiesFile", "");
			propFile.delete();
		}
	}
}
