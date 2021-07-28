package org.nhindirect.stagent.trust;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Collection;


import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.utils.TestUtils;

public class TrustChainValidator_downloadCertsFromAIATest
{
	protected String filePrefix;
	
	@BeforeEach
	public void setUp()
	{	
		// check for Windows... it doens't like file://<drive>... turns it into FTP
		File file = new File("./src/test/resources/certs/bob.der");
		if (file.getAbsolutePath().contains(":/"))
			filePrefix = "file:///";
		else
			filePrefix = "file:///";
	}
	
	@Test
	public void testDownloadCertsFromAIA_validURL_singleCert_assertDownloaded() throws Exception
	{
		final TrustChainValidator validator = new TrustChainValidator();
		
		final File fl = new File("src/test/resources/certs/bob.der");
		
		final X509Certificate downloadedCert = validator.downloadCertsFromAIA(filePrefix + fl.getAbsolutePath()).iterator().next();
		
		assertNotNull(downloadedCert);
		
		assertEquals(TestUtils.loadCertificate("bob.der"), downloadedCert);
	}
	
	@Test
	public void testDownloadCertsFromAIA_validURL_collectionCert_assertDownloaded() throws Exception
	{
		final TrustChainValidator validator = new TrustChainValidator();
		
		final File fl = new File("src/test/resources/certs/cmsRandomizer.p7b");
		
		final Collection<X509Certificate> downloadedCerts = validator.downloadCertsFromAIA(filePrefix + fl.getAbsolutePath());
		
		assertNotNull(downloadedCerts);
		
		
		assertEquals(6, downloadedCerts.size());
	}
	
	@Test
	public void testDownloadCertsFromAIA_certNotAtURL_assertException() throws Exception
	{
		final TrustChainValidator validator = new TrustChainValidator();
		
		final File fl = new File("src/test/resources/certs/bob.derdd");
		
		boolean exceptionOccurred = false;
		
		try
		{
			validator.downloadCertsFromAIA(filePrefix + fl.getAbsolutePath());
		}
		catch (NHINDException e)
		{
			exceptionOccurred = true;
		}
		
		assertTrue(exceptionOccurred);
	}
}
