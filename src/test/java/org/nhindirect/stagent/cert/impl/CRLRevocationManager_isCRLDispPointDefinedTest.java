package org.nhindirect.stagent.cert.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;

import org.nhindirect.stagent.utils.TestUtils;

public class CRLRevocationManager_isCRLDispPointDefinedTest 
{
	@Test
	public void testIsCRLDispPointDefined_assertCRLDispDefined() throws Exception
	{
		X509Certificate cert = TestUtils.loadCertificate("uhin.cer");
		assertTrue(CRLRevocationManager.isCRLDispPointDefined(cert));
	}
	
	@Test
	public void testIsCRLDispPointDefined_assertCRLDispNotDefined() throws Exception
	{
		X509Certificate cert = TestUtils.loadCertificate("gm2552.der");
		assertFalse(CRLRevocationManager.isCRLDispPointDefined(cert));
	}
}
