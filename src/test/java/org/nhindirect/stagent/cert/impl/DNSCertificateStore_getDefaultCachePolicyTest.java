package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.nhindirect.common.options.OptionsManagerUtils;
import org.nhindirect.stagent.cert.CertCacheFactory;


public class DNSCertificateStore_getDefaultCachePolicyTest
{
	@BeforeEach
	public void setUp()
	{
		OptionsManagerUtils.clearOptionsManagerInstance();
		CertCacheFactory.getInstance().flushAll();
	}
	
	@AfterEach
	public void tearDown()
	{
		OptionsManagerUtils.clearOptionsManagerOptions();
		CertCacheFactory.getInstance().flushAll();
	}
	
	@Test
	public void testGetDefaultCachePolicyTest_useDefaultSettings_assertSettings() throws Exception
	{
		DNSCertificateStore store = new DNSCertificateStore();

		assertNotNull(store.cachePolicy);
		assertEquals(DNSCertificateStore.DEFAULT_DNS_MAX_CAHCE_ITEMS, store.cachePolicy.getMaxItems());
		assertEquals(DNSCertificateStore.DEFAULT_DNS_TTL, store.cachePolicy.getSubjectTTL());
	}
	
	@Test
	public void testGetDefaultCachePolicyTest_useSettingsFromJVMParams_assertSettings() throws Exception
	{
		System.setProperty("org.nhindirect.stagent.cert.dnsresolver.MaxCacheSize", "500");
		System.setProperty("org.nhindirect.stagent.cert.dnsresolver.CacheTTL", "1800");
		
		try
		{
			DNSCertificateStore store = new DNSCertificateStore();
			assertNotNull(store.cachePolicy);
			assertEquals(500, store.cachePolicy.getMaxItems());
			assertEquals(1800, store.cachePolicy.getSubjectTTL());
		}
		finally
		{
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.MaxCacheSize", "");
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.CacheTTL", "");
		}

	}	
	
	@Test
	public void testGetDefaultCachePolicyTest_useSettingsFromPropertiesFile_assertSettings() throws Exception
	{	
		File propFile = new File("./target/props/agentSettings.properties");
		if (propFile.exists())
			propFile.delete();
	
		System.setProperty("org.nhindirect.stagent.PropertiesFile", "./target/props/agentSettings.properties");
		

		try (OutputStream outStream = FileUtils.openOutputStream(propFile);)
		{
			
			outStream.write("org.nhindirect.stagent.cert.dnsresolver.MaxCacheSize=1200\r\n".getBytes());
			outStream.write("org.nhindirect.stagent.cert.dnsresolver.CacheTTL=900".getBytes());
			outStream.flush();
			
		}
		finally
		{

		}
		
		try
		{
			DNSCertificateStore store = new DNSCertificateStore();
			assertNotNull(store.cachePolicy);
			assertEquals(1200, store.cachePolicy.getMaxItems());
			assertEquals(900, store.cachePolicy.getSubjectTTL());
		}
		finally
		{
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.MaxCacheSize", "");
			System.setProperty("org.nhindirect.stagent.cert.dnsresolver.CacheTTL", "");
			System.setProperty("org.nhindirect.stagent.PropertiesFile", "");
			propFile.delete();
		}

	}	
}
