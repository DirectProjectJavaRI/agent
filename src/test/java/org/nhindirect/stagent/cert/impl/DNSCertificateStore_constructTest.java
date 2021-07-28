package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.nhindirect.stagent.cert.CertCacheFactory;
import org.nhindirect.stagent.cert.CertStoreCachePolicy;
import org.nhindirect.stagent.cert.CertificateStore;
import org.xbill.DNS.ResolverConfig;

public class DNSCertificateStore_constructTest
{
	@BeforeEach
	public void setUp()
	{
		CertCacheFactory.getInstance().flushAll();
	}
	
	@AfterEach
	public void tearDown()
	{
		CertCacheFactory.getInstance().flushAll();
	}
	
	@Test
	public void testContructDNSCertificateStore_defaultConstructor()
	{
		DNSCertificateStore store = new DNSCertificateStore();
		
		assertEquals(ResolverConfig.getCurrentConfig().servers().size(), store.servers.size());
		assertNull(store.localStoreDelegate);
		assertNotNull(store.cachePolicy);
	}
	
	@Test
	public void testContructDNSCertificateStore_nullServers()
	{
		DNSCertificateStore store = new DNSCertificateStore(null);
		
		assertEquals(ResolverConfig.getCurrentConfig().servers().size(), store.servers.size());
		assertNull(store.localStoreDelegate);
		assertNotNull(store.cachePolicy);
	}
	
	@Test
	public void testContructDNSCertificateStore_emptyServers()
	{
		DNSCertificateStore store = new DNSCertificateStore(new ArrayList<String>());
		
		assertEquals(ResolverConfig.getCurrentConfig().servers().size(), store.servers.size());
		assertNull(store.localStoreDelegate);
		assertNotNull(store.cachePolicy);
	}
	
	@Test
	public void testContructDNSCertificateStore_providedServers()
	{
		Collection<String> servers = Arrays.asList("159.140.168.3");
		
		DNSCertificateStore store = new DNSCertificateStore(servers);
		
		assertEquals(1, store.servers.size());
		assertEquals("159.140.168.3", store.servers.iterator().next());
		assertNull(store.localStoreDelegate);
		assertNotNull(store.cachePolicy);
	}
	
	@Test
	public void testContructDNSCertificateStore_fullConstructor_nullBootStrap()
	{

		DNSCertificateStore store =	new DNSCertificateStore(null, null, null);

		assertNotNull(store.cachePolicy);
		assertNull(store.localStoreDelegate);
	}
	
	@Test
	public void testContructDNSCertificateStore_fullConstructor_providedServers()
	{
		CertificateStore bootStrap = mock(CertificateStore.class);
		Collection<String> servers = Arrays.asList("159.140.168.3");
		
		DNSCertificateStore store = new DNSCertificateStore(servers, bootStrap, null);
		
		assertEquals(1, store.servers.size());
		assertEquals("159.140.168.3", store.servers.iterator().next());
		assertEquals(bootStrap, store.localStoreDelegate);
		assertNotNull(store.cachePolicy);
	}
	
	@Test
	public void testContructDNSCertificateStore_fullConstructor_nullServers()
	{
		CertificateStore bootStrap = mock(CertificateStore.class);
		
		DNSCertificateStore store = new DNSCertificateStore(null, bootStrap, null);
		
		assertEquals(ResolverConfig.getCurrentConfig().servers().size(), store.servers.size());
		assertNotNull(store.localStoreDelegate);
		assertNotNull(store.cachePolicy);
	}
	
	@Test
	public void testContructDNSCertificateStore_fullConstructor_emptyServers()
	{
		CertificateStore bootStrap = mock(CertificateStore.class);
		
		DNSCertificateStore store = new DNSCertificateStore(new ArrayList<String>(), bootStrap, null);
		
		assertEquals(ResolverConfig.getCurrentConfig().servers().size(), store.servers.size());
		assertNotNull(store.localStoreDelegate);
		assertNotNull(store.cachePolicy);
	}
	
	
	@Test
	public void testContructDNSCertificateStore_fullConstructor_emptyServersAndProvidedCachePolicy()
	{
		CertificateStore bootStrap = mock(CertificateStore.class);
		CertStoreCachePolicy cachePolicy = mock(CertStoreCachePolicy.class);
		
		DNSCertificateStore store = new DNSCertificateStore(new ArrayList<String>(), bootStrap, cachePolicy);
		
		assertEquals(ResolverConfig.getCurrentConfig().servers().size(), store.servers.size());
		assertNotNull(store.localStoreDelegate);
		assertEquals(cachePolicy, store.cachePolicy);
	}
}
