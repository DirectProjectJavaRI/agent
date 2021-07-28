package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.common.options.OptionsParameter;


public class CRLRevocationManager_initCRLCacheLocationTest
{
	static final char[] invalidFileName;
	
	static
	{
		invalidFileName = new char[Character.MAX_VALUE];
		
		for (char i = 1; i < Character.MAX_VALUE; ++i)
		{
			invalidFileName[i - 1] = i;
		}
	}
	
	@BeforeEach
	public void setUp()
	{
    	CryptoExtensions.registerJCEProviders();
		
		CRLRevocationManager.initCRLCacheLocation();
		CRLRevocationManager.getInstance().flush();		
		CRLRevocationManager.crlCacheLocation = null;
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.CRL_CACHE_LOCATION, ""));
	}
	
	@AfterEach
	public void tearDown()
	{
		CRLRevocationManager.getInstance().flush();
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.CRL_CACHE_LOCATION, ""));
		CRLRevocationManager.initCRLCacheLocation();
	}
	
	@Test
	public void testInitCRLCacheLocation_noOptionParameter()
	{	
		CRLRevocationManager.initCRLCacheLocation();
		assertTrue(CRLRevocationManager.crlCacheLocation.getAbsolutePath().endsWith("CrlCache"));
	}
	
	@Test
	public void testInitCRLCacheLocation_customOptionParameter()
	{	
		String crlLocation = UUID.randomUUID().toString();
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.CRL_CACHE_LOCATION, "target/" + crlLocation));
		CRLRevocationManager.initCRLCacheLocation();
		assertTrue(CRLRevocationManager.crlCacheLocation.getAbsolutePath().endsWith(crlLocation));
	}
	
	@Test
	public void testInitCRLCacheLocation_locExistsAndNotADirectory() throws Exception
	{	
		String crlLocation = UUID.randomUUID().toString();
		File createFile = new File("target/" + crlLocation);
		createFile.createNewFile();
		
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.CRL_CACHE_LOCATION, "target/" + crlLocation));
		CRLRevocationManager.initCRLCacheLocation();
		assertNull(CRLRevocationManager.crlCacheLocation);
	}
	
	@Test
	public void testInitCRLCacheLocation_invalidLocationName() throws Exception
	{	
		
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.CRL_CACHE_LOCATION, "target/" + new String(invalidFileName)));
		CRLRevocationManager.initCRLCacheLocation();
		assertNull(CRLRevocationManager.crlCacheLocation);
	}
}
