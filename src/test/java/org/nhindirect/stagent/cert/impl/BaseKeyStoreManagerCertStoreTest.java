package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.commons.lang3.StringUtils;
import org.nhindirect.common.crypto.MutableKeyStoreProtectionManager;
import org.nhindirect.common.crypto.PKCS11Credential;
import org.nhindirect.common.crypto.impl.BootstrappedPKCS11Credential;
import org.nhindirect.common.crypto.impl.StaticPKCS11TokenKeyStoreProtectionManager;
import org.nhindirect.stagent.cert.CertCacheFactory;
import org.nhindirect.stagent.utils.TestUtils;

public abstract class BaseKeyStoreManagerCertStoreTest
{
	/*
	 * Testing these with the SafeNet token.  Only run these if the token is installed
	 */
	protected CacheableKeyStoreManagerCertificateStore store = null;
	
	@BeforeEach
	public void setUp() throws Exception
	{
		CertCacheFactory.getInstance().flushAll();
		
		if (!StringUtils.isEmpty(TestUtils.setupSafeNetToken()))
		{
			// clean out the token of all private keys
			final PKCS11Credential cred = new BootstrappedPKCS11Credential("1Kingpuff");
			
			final MutableKeyStoreProtectionManager mgr = new StaticPKCS11TokenKeyStoreProtectionManager(cred, "", "");
			
			store = new CacheableKeyStoreManagerCertificateStore(mgr);
			
			store.remove(store.getAllCertificates());
			
			assertTrue(store.getAllCertificates().isEmpty());
		}
	}
	
	@AfterEach
	public void tearDown() throws Exception
	{
		if (store != null)
		{
			store.remove(store.getAllCertificates());
			assertTrue(store.getAllCertificates().isEmpty());
		}
	}
}
