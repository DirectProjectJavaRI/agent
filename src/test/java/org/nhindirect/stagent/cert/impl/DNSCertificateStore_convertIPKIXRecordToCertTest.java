package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.cert.Certificate;

import org.nhindirect.stagent.cert.CertCacheFactory;
import org.xbill.DNS.CERTRecord;


public class DNSCertificateStore_convertIPKIXRecordToCertTest
{
	protected String filePrefix;
	
	@BeforeEach
	public void setUp()
	{
		// flush the caches
		CertCacheFactory.getInstance().flushAll();
		
		// check for Windows... it doens't like file://<drive>... turns it into FTP
		File file = new File("./src/test/resources/certs/certCheckA.der");
		if (file.getAbsolutePath().contains(":/"))
			filePrefix = "file:///";
		else
			filePrefix = "file:///";
		
	}
	
	@Test
	public void testConvertIPKIXRecordToCert_validCERTData_assertCertificate() throws Exception
	{
		File file = new File("./src/test/resources/certs/certCheckA.der");
		
		final String url = filePrefix + file.getAbsolutePath();
		
		final CERTRecord rec = mock(CERTRecord.class);
		when(rec.getCert()).thenReturn(url.getBytes());
		
		final DNSCertificateStore store = new DNSCertificateStore();
		
		Certificate cert = store.convertIPKIXRecordToCert(rec);
		assertNotNull(cert);
	}
	
	@Test
	public void testConvertIPKIXRecordToCert_invalidCERTData_assertNoCertificate() throws Exception
	{
		File file = new File("./src/test/resources/log4j.properties");
		

		
		final String url = filePrefix + file.getAbsolutePath();
		
		final CERTRecord rec = mock(CERTRecord.class);
		when(rec.getCert()).thenReturn(url.getBytes());
		
		final DNSCertificateStore store = new DNSCertificateStore();
		
		Certificate cert = store.convertIPKIXRecordToCert(rec);
		assertNull(cert);
	}
	
	@Test
	public void testConvertIPKIXRecordToCert_invalidURL_assertNoCertificate() throws Exception
	{
		
		final CERTRecord rec = mock(CERTRecord.class);
		when(rec.getCert()).thenReturn("http://localhost:9481/bogus".getBytes());
		
		final DNSCertificateStore store = new DNSCertificateStore();
		
		Certificate cert = store.convertIPKIXRecordToCert(rec);
		assertNull(cert);
	}
}
