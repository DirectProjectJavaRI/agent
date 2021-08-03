package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.nhindirect.common.crypto.CryptoExtensions;


public class CRLRevocationManager_getCacheFileNameTest
{
	@BeforeEach
	public void setUp()
	{
    	CryptoExtensions.registerJCEProviders();
		
		CRLRevocationManager.initCRLCacheLocation();
		CRLRevocationManager.getInstance().flush();
		CRLRevocationManager.crlCacheLocation = null;
	}
	
	@AfterEach
	public void tearDown()
	{
		CRLRevocationManager.getInstance().flush();
		CRLRevocationManager.initCRLCacheLocation();
	}
	
	@Test
	public void testGetCacheName_uniqueNames() throws Exception
	{
		CRLRevocationManager.initCRLCacheLocation();
		String uriName1 = CRLRevocationManager.getCacheFileName("http://localhost:8080/master.crl");
		assertNotNull(uriName1);
		assertTrue(uriName1.contains("CrlCache"));
		
		String uriName2 = CRLRevocationManager.getCacheFileName("http://localhost/master.crl");
		assertNotNull(uriName2);
		assertTrue(uriName2.contains("CrlCache"));
		
		assertFalse(uriName1.equals(uriName2));
	}
	
	@Test
	public void testGetCacheName_sameNames() throws Exception
	{
		CRLRevocationManager.initCRLCacheLocation();
		String uriName1 = CRLRevocationManager.getCacheFileName("http://localhost:8080/master.crl");
		assertNotNull(uriName1);
		assertTrue(uriName1.contains("CrlCache"));
		
		String uriName2 = CRLRevocationManager.getCacheFileName("http://localhost:8080/master.crl");
		assertNotNull(uriName2);
		assertTrue(uriName2.contains("CrlCache"));
		
		assertEquals(uriName1, uriName2);
	}
	
	@Test
	public void testGetCacheName_nullCacheLocation_assertEmptyName() throws Exception
	{
		String uriName1 = CRLRevocationManager.getCacheFileName("http://localhost:8080/master.crl");
		assertNotNull(uriName1);
		assertEquals("", uriName1);

	}
	
}
