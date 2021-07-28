package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.utils.TestUtils;

public class CacheableKeyStoreManagerCertificateStore_containsTest extends BaseKeyStoreManagerCertStoreTest
{
	@Test
	public void testContains_existingCert_assertFound() throws Exception
	{
		if (store != null)
		{
			// add a certificate
			final X509CertificateEx user1 = (X509CertificateEx)TestUtils.getInternalCert("user1");
			store.add(user1);
			
			assertTrue(store.contains(user1));
		}
	}
	
	@Test
	public void testContains_nonExistingCert_assertNotFound() throws Exception
	{
		if (store != null)
		{
			// add a certificate
			final X509CertificateEx user1 = (X509CertificateEx)TestUtils.getInternalCert("user1");
			
			assertFalse(store.contains(user1));
		}
	}
}
