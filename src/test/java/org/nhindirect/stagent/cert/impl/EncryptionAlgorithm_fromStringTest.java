package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.nhindirect.stagent.cryptography.EncryptionAlgorithm;

public class EncryptionAlgorithm_fromStringTest
{
	@Test
	public void testFromString_RSA_3DES()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("RSA_3DES", EncryptionAlgorithm.AES256);
		assertEquals(EncryptionAlgorithm.RSA_3DES, alg);
	}
	
	@Test
	public void testFromString_AES128()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("AES128", EncryptionAlgorithm.AES256);
		assertEquals(EncryptionAlgorithm.AES128, alg);
	}
	
	@Test
	public void testFromString_AES192()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("AES192", EncryptionAlgorithm.AES128);
		assertEquals(EncryptionAlgorithm.AES192, alg);
	}
	
	@Test
	public void testFromString_AES256()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("AES256", EncryptionAlgorithm.AES128);
		assertEquals(EncryptionAlgorithm.AES256, alg);
	}
	
	@Test
	public void testFromString_RSA()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("RSA", EncryptionAlgorithm.RSA);
		assertEquals(EncryptionAlgorithm.RSA, alg);
	}
	
	@Test
	public void testFromString_DSA()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("DSA", EncryptionAlgorithm.DSA);
		assertEquals(EncryptionAlgorithm.DSA, alg);
	}
	
	@Test
	public void testFromString_ENCRYPTION_RSA_PSS()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("RSAandMGF1", EncryptionAlgorithm.RSAandMGF1);
		assertEquals(EncryptionAlgorithm.RSAandMGF1, alg);
	}
	
	@Test
	public void testFromString_ECDSA()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("ECDSA", EncryptionAlgorithm.ECDSA);
		assertEquals(EncryptionAlgorithm.ECDSA, alg);
	}
	
	@Test
	public void testFromString_DES_EDE3_CBC()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("DESEDE/CBC/PKCS5Padding", EncryptionAlgorithm.DES_EDE3_CBC);
		assertEquals(EncryptionAlgorithm.DES_EDE3_CBC, alg);
	}
	
	@Test
	public void testFromString_AES128_CBC()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("AES/CBC/PKCS5Padding", EncryptionAlgorithm.AES128_CBC);
		assertEquals(EncryptionAlgorithm.AES128_CBC, alg);
	}

	@Test
	public void testFromString_AES192_CBC()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("AES/CBC/PKCS5Padding", EncryptionAlgorithm.AES192_CBC);
		assertEquals(EncryptionAlgorithm.AES192_CBC, alg);
	}
	
	@Test
	public void testFromString_AES256_CBC()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("AES/CBC/PKCS5Padding", EncryptionAlgorithm.AES256_CBC);
		assertEquals(EncryptionAlgorithm.AES256_CBC, alg);
	}
	
	@Test
	public void testFromString_nullName_assertDefault()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString(null, EncryptionAlgorithm.AES192);
		assertEquals(EncryptionAlgorithm.AES192, alg);
	}
	
	@Test
	public void testFromString_emptyName_assertDefault()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("", EncryptionAlgorithm.AES192);
		assertEquals(EncryptionAlgorithm.AES192, alg);
	}
	
	@Test
	public void testFromString_unknownName_assertDefault()
	{
		EncryptionAlgorithm alg = EncryptionAlgorithm.fromString("asdfwqerasd", EncryptionAlgorithm.AES192);
		assertEquals(EncryptionAlgorithm.AES192, alg);
	}
}
