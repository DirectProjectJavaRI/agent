package org.nhindirect.stagent.cryptography;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.cert.X509Certificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nhindirect.stagent.utils.TestUtils;

public class SMIMECryptographerImpl_isAllowedCertKeyTest 
{
	protected SMIMECryptographerImpl impl;
	
	@BeforeEach
	public void setUp()
	{
		impl = new SMIMECryptographerImpl();
	}
	
	@Test
	public void testIsAllowedCertKey_size2048bits_assertAllowed() throws Exception
	{
		final X509Certificate cert = TestUtils.loadCertificate("certCheckA.der");
		
		assertTrue(impl.isAllowedCertKey(cert));
	}
	
	@Test
	public void testIsAllowedCertKey_size1024bits_assertNotAllowed() throws Exception
	{
		final X509Certificate cert = TestUtils.loadCertificate("msanchor.der");
		
		assertFalse(impl.isAllowedCertKey(cert));
	}
}
