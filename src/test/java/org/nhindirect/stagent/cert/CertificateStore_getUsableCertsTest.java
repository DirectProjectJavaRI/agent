package org.nhindirect.stagent.cert;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

import javax.mail.internet.InternetAddress;

import org.nhindirect.stagent.AgentError;
import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.utils.TestUtils;

public class CertificateStore_getUsableCertsTest
{
	
	@Test
	public void testGetUsableCerts_inValidUserCert_noCertsRetrieved() throws Exception
	{
		final X509CertificateEx userCert = TestUtils.getInternalCert("user1");
		final X509CertificateEx domainCert = TestUtils.getInternalCert("gm2552");
		
		CertificateStore store = new CertificateStoreAdapter()
		{
		    protected Collection<X509Certificate> filterUsable(Collection<X509Certificate> certs)
		    {
		    	if (certs.iterator().next().getSubjectDN().getName().contains("user1"))
		    		return null;
		    	else
		    		return certs;
		    }
		    
		    public Collection<X509Certificate> getCertificates(String subjectName)
		    {
		    	if (subjectName.contains("user1@domain.com"))
		    		return Arrays.asList((X509Certificate)userCert);
		    	else
		    		return Arrays.asList((X509Certificate)domainCert);
		    }
		};
		
		boolean exceptionOccured = false;
		try
		{
			store.getCertificates(new InternetAddress("user1@domain.com"));
		}
		catch (NHINDException e)
		{
			assertEquals(e.getError(), AgentError.AllCertsInResolverInvalid);
			exceptionOccured = true;
		}
		
		assertTrue(exceptionOccured);
	}

	@Test
	public void testGetUsableCerts_allCertsInvalid_assertNoCerts() throws Exception
	{
		final X509CertificateEx userCert = TestUtils.getInternalCert("user1");
		final X509CertificateEx domainCert = TestUtils.getInternalCert("gm2552");
		
		CertificateStore store = new CertificateStoreAdapter()
		{
		    protected Collection<X509Certificate> filterUsable(Collection<X509Certificate> certs)
		    {
		    	return null;
		    }
		    
		    public Collection<X509Certificate> getCertificates(String subjectName)
		    {
		    	if (subjectName.contains("user1@domain.com"))
		    		return Arrays.asList((X509Certificate)userCert);
		    	else
		    		return Arrays.asList((X509Certificate)domainCert);
		    }
		};
		
			
		boolean exceptionOccured = false;
		try
		{
			store.getCertificates(new InternetAddress("user1@domain.com"));
		}
		catch (NHINDException e)
		{
			assertEquals(e.getError(), AgentError.AllCertsInResolverInvalid);
			exceptionOccured = true;
		}
		
		assertTrue(exceptionOccured);			
	}
	
	@Test
	public void testGetUsableCerts_getUserCert() throws Exception
	{
		final X509CertificateEx userCert = TestUtils.getInternalCert("user1");
		final X509CertificateEx domainCert = TestUtils.getInternalCert("gm2552");
		
		CertificateStore store = new CertificateStoreAdapter()
		{
		    protected Collection<X509Certificate> filterUsable(Collection<X509Certificate> certs)
		    {
		    	if (certs.iterator().next().getSubjectDN().getName().contains("user1"))
		    		return certs;
		    	else
		    		return certs;
		    }
		    
		    public Collection<X509Certificate> getCertificates(String subjectName)
		    {
		    	if (subjectName.contains("user1@domain.com"))
		    		return Arrays.asList((X509Certificate)userCert);
		    	else
		    		return Arrays.asList((X509Certificate)domainCert);
		    }
		};
		
		Collection<X509Certificate> foundCert = store.getCertificates(new InternetAddress("user1@domain.com"));
		assertEquals(userCert, foundCert.iterator().next());
	}
	
	static class CertificateStoreAdapter extends CertificateStore
	{

		@Override
		public boolean contains(X509Certificate cert) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void add(X509Certificate cert) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void remove(X509Certificate cert) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Collection<X509Certificate> getAllCertificates() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
