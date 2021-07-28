package org.nhindirect.stagent.cryptography;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SMIMECryptographerImpl_isAllowedDigestAlgorithmTest 
{
	protected SMIMECryptographerImpl impl;
	
	@BeforeEach
	public void setUp()
	{
		impl = new SMIMECryptographerImpl();
	}
	
	@Test
	public void testIsAllowedDigestAlgorithm_SHA256_assertAllowed() throws Exception
	{
		assertTrue(impl.isAllowedDigestAlgorithm(DigestAlgorithm.SHA256.getOID()));
	}
	
	@Test
	public void testIsAllowedDigestAlgorithm_SHA384_assertAllowed() throws Exception
	{
		assertTrue(impl.isAllowedDigestAlgorithm(DigestAlgorithm.SHA384.getOID()));
	}
	
	@Test
	public void testIsAllowedDigestAlgorithm_MD5_assertNotAllowed() throws Exception
	{
		assertFalse(impl.isAllowedDigestAlgorithm(DigestAlgorithm.MD5.getOID()));
	}
	
	@Test
	public void testIsAllowedDigestAlgorithm_SHA1_assertNotAllowed() throws Exception
	{
		assertFalse(impl.isAllowedDigestAlgorithm(DigestAlgorithm.SHA1.getOID()));
	}
	
	@Test
	public void testIsAllowedDigestAlgorithm_SHA1WITHRSA_assertNotAllowed() throws Exception
	{
		assertFalse(impl.isAllowedDigestAlgorithm(DigestAlgorithm.SHA1WITHRSA.getOID()));
	}
}
