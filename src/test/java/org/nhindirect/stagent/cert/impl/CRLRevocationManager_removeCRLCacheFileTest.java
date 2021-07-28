package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.cert.CRL;
import java.security.cert.X509CRL;

import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.stagent.utils.TestUtils;

public class CRLRevocationManager_removeCRLCacheFileTest
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
	public void testRemoveCRLCacheFile_removeExistingFile() throws Exception
	{
		CRLRevocationManager.initCRLCacheLocation();

		CRL crlToWrite = TestUtils.loadCRL("certs.crl");
		String distURI = "http://localhost:8080/config";
		
		CRLRevocationManager.getInstance().writeCRLCacheFile(distURI, (X509CRL)crlToWrite);
		
		// make sure the file exists
		File crlFile = new File(CRLRevocationManager.getCacheFileName(distURI));
		assertTrue(crlFile.exists());
		
		// now delete the file
		CRLRevocationManager.getInstance().removeCrlCacheFile(distURI);

		
		// make sure the file does not exist
		crlFile = new File(CRLRevocationManager.getCacheFileName(distURI));
		assertFalse(crlFile.exists());
	}
	
	@Test
	public void testRemoveCRLCacheFile_removeNonExistingFile() throws Exception
	{
		CRLRevocationManager.initCRLCacheLocation();

		String distURI = "http://localhost:8080/config";
		
		
		// make sure the file does not exists
		File crlFile = new File(CRLRevocationManager.getCacheFileName(distURI));
		assertFalse(crlFile.exists());
		
		// now delete the file
		CRLRevocationManager.getInstance().removeCrlCacheFile(distURI);

		
		// make sure the file does not exist
		crlFile = new File(CRLRevocationManager.getCacheFileName(distURI));
		assertFalse(crlFile.exists());
	}
	
	@Test
	public void testRemoveCRLCacheFile_noCacheLoction() throws Exception
	{
		String distURI = "http://localhost:8080/config";
		
		assertEquals("", CRLRevocationManager.getCacheFileName(distURI));
		
		// make sure the file does not exists
		File crlFile = new File(CRLRevocationManager.getCacheFileName(distURI));
		assertFalse(crlFile.exists());
		
		// now delete the file
		CRLRevocationManager.getInstance().removeCrlCacheFile(distURI);

		
		// make sure the file does not exist
		crlFile = new File(CRLRevocationManager.getCacheFileName(distURI));
		assertFalse(crlFile.exists());
	}
}
