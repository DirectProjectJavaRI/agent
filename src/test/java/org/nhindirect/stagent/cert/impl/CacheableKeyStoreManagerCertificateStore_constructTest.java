package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.nhindirect.common.crypto.impl.BootstrappedKeyStoreProtectionManager;
import org.nhindirect.stagent.cert.CertCacheFactory;
import org.nhindirect.stagent.cert.DefaultCertStoreCachePolicy;

public class CacheableKeyStoreManagerCertificateStore_constructTest
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
	public void testContrust_defaultConstructor_assertNullStore() throws Exception
	{
		final CacheableKeyStoreManagerCertificateStore store = new CacheableKeyStoreManagerCertificateStore();
		assertNull(store.storeMgr);
		assertNotNull(store.cachePolicy);
		assertEquals(CacheableKeyStoreManagerCertificateStore.DEFAULT_MAX_CAHCE_ITEMS, store.cachePolicy.getMaxItems());
		assertEquals(CacheableKeyStoreManagerCertificateStore.DEFAULT_CACHE_TTL, store.cachePolicy.getSubjectTTL());
	}
	
	@Test
	public void testContrust_providedStore_assertNonEmptyStore() throws Exception
	{
		final BootstrappedKeyStoreProtectionManager mgr = new BootstrappedKeyStoreProtectionManager();
		final CacheableKeyStoreManagerCertificateStore store = new CacheableKeyStoreManagerCertificateStore(mgr);
		assertNotNull(store.storeMgr);
		assertNotNull(store.cachePolicy);
		assertEquals(CacheableKeyStoreManagerCertificateStore.DEFAULT_MAX_CAHCE_ITEMS, store.cachePolicy.getMaxItems());
		assertEquals(CacheableKeyStoreManagerCertificateStore.DEFAULT_CACHE_TTL, store.cachePolicy.getSubjectTTL());
	}
	
	@Test
	public void testContrust_providedStoreAndCachePolicy_assertNonEmptyStoreAndCustomPolicy() throws Exception
	{
		DefaultCertStoreCachePolicy policy = new DefaultCertStoreCachePolicy();
		policy.setMaxItems(456);
		policy.setSubjectTTL(999);
		final BootstrappedKeyStoreProtectionManager mgr = new BootstrappedKeyStoreProtectionManager();
		final CacheableKeyStoreManagerCertificateStore store = new CacheableKeyStoreManagerCertificateStore(mgr, policy);
		assertNotNull(store.storeMgr);
		assertNotNull(store.cachePolicy);
		assertEquals(456, store.cachePolicy.getMaxItems());
		assertEquals(999, store.cachePolicy.getSubjectTTL());
	}
}
