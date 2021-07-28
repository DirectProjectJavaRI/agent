package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.nhindirect.stagent.cryptography.DigestAlgorithm;


public class DigestAlgorithm_fromStringTest
{
	
	@Test
	public void testFromString_MD5()
	{
		DigestAlgorithm alg = DigestAlgorithm.fromString("MD5", DigestAlgorithm.SHA256);
		assertEquals(DigestAlgorithm.MD5, alg);
	}
	
	@Test
	public void testFromString_SHA1()
	{
		DigestAlgorithm alg = DigestAlgorithm.fromString("SHA1", DigestAlgorithm.SHA256);
		assertEquals(DigestAlgorithm.SHA1, alg);
	}
	
	@Test
	public void testFromString_SHA256()
	{
		DigestAlgorithm alg = DigestAlgorithm.fromString("SHA256", DigestAlgorithm.SHA1);
		assertEquals(DigestAlgorithm.SHA256, alg);
	}
	
	@Test
	public void testFromString_SHA384()
	{
		DigestAlgorithm alg = DigestAlgorithm.fromString("SHA384", DigestAlgorithm.SHA1);
		assertEquals(DigestAlgorithm.SHA384, alg);
	}
	
	@Test
	public void testFromString_SHA512()
	{
		DigestAlgorithm alg = DigestAlgorithm.fromString("SHA512", DigestAlgorithm.SHA1);
		assertEquals(DigestAlgorithm.SHA512, alg);
	}
	
	@Test
	public void testFromString_nullName_assertDefault()
	{
		DigestAlgorithm alg = DigestAlgorithm.fromString(null, DigestAlgorithm.SHA256);
		assertEquals(DigestAlgorithm.SHA256, alg);
	}
	
	@Test
	public void testFromString_emptyName_assertDefault()
	{
		DigestAlgorithm alg = DigestAlgorithm.fromString("", DigestAlgorithm.SHA256);
		assertEquals(DigestAlgorithm.SHA256, alg);
	}
	
	@Test
	public void testFromString_unknownName_assertDefault()
	{
		DigestAlgorithm alg = DigestAlgorithm.fromString("asdfwqerasd", DigestAlgorithm.SHA256);
		assertEquals(DigestAlgorithm.SHA256, alg);
	}
}
