package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.ref.SoftReference;
import java.security.cert.X509CRL;
import java.util.Calendar;

import org.nhindirect.common.crypto.CryptoExtensions;

public class CRLRevocationManager_getCRLCollectionTest 
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
	public void testGetCRLCollection_emptyCRL_assertEmpty()
	{
		assertEquals(0, CRLRevocationManager.getInstance().getCRLCollection().size());
	}
	
	@Test
	public void testGetCRLCollection_singleCRL_assertCRLRetrieved()
	{
		String uri = "http://localhost:8080/master.crl";
		Calendar nextUpdateDate = Calendar.getInstance();
		nextUpdateDate.set(Calendar.YEAR, nextUpdateDate.get(Calendar.YEAR) + 10);
		
		X509CRL crl = mock(X509CRL.class);
		when(crl.getNextUpdate()).thenReturn(nextUpdateDate.getTime());
		
		CRLRevocationManager.cache.put(uri, new SoftReference<X509CRL>(crl));
		
		X509CRL retCrl = CRLRevocationManager.getInstance().getCrlFromUri(uri);
		assertNotNull(retCrl);
		assertEquals(crl, retCrl);
		
		assertEquals(1, CRLRevocationManager.getInstance().getCRLCollection().size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetCRLCollection_singleCRL_softRefExpired_assertEmpty()
	{
		String uri = "http://localhost:8080/master.crl";
		
		
		SoftReference<X509CRL> softRef = mock(SoftReference.class);
		when(softRef.get()).thenReturn(null);
		
		CRLRevocationManager.cache.put(uri, softRef);
		
		assertEquals(0, CRLRevocationManager.getInstance().getCRLCollection().size());
	}
}
