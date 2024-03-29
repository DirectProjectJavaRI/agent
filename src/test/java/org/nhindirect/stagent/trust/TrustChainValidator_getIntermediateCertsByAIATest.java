package org.nhindirect.stagent.trust;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;


import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.utils.TestUtils;


public class TrustChainValidator_getIntermediateCertsByAIATest
{
	static class TrustChainValidatorWrapper extends TrustChainValidator
	{
		public String retrievedURL;
	}
	
	@Test
	public void testGetIntermediateCertsByAIA_AIAExists_validateResolved() throws Exception
	{
	
		final TrustChainValidatorWrapper validator = new TrustChainValidatorWrapper()
		{
			protected Collection<X509Certificate> downloadCertsFromAIA(String url) throws NHINDException
			{
				try
				{
					retrievedURL = url;
					return Arrays.asList(TestUtils.loadCertificate("bob.der"));
				}
				catch (Exception e){throw new NHINDException(e);}
			}
		};
				
		final TrustChainValidatorWrapper spyValidator = spy(validator);
		
		Collection<X509Certificate> downloadedCerts = spyValidator.getIntermediateCertsByAIA(TestUtils.loadCertificate("demo.sandboxcernerdirect.com.der"));
		
		assertEquals("http://sandboxcernerdirect.com/professional/public/subordinate.der", spyValidator.retrievedURL);
		assertEquals(1, downloadedCerts.size());
		assertEquals(TestUtils.loadCertificate("bob.der"), downloadedCerts.iterator().next());
		
		verify(spyValidator, times(1)).downloadCertsFromAIA((String)any());
	}
	
	@Test
	public void testGetIntermediateCertsByAIA_emptyAIA_validateNotResolved() throws Exception
	{
	
		final TrustChainValidatorWrapper validator = new TrustChainValidatorWrapper()
		{
			protected Collection<X509Certificate> downloadCertsFromAIA(String url) throws NHINDException
			{
				try
				{
					retrievedURL = url;
					return Arrays.asList(TestUtils.loadCertificate("bob.der"));
				}
				catch (Exception e){throw new NHINDException(e);}
			}
		};
				
		final TrustChainValidatorWrapper spyValidator = spy(validator);
		
		Collection<X509Certificate> downloadedCerts = spyValidator.getIntermediateCertsByAIA(TestUtils.loadCertificate("altNameOnly.der"));
		
		assertNull(spyValidator.retrievedURL);
		assertEquals(0, downloadedCerts.size());
		
		verify(spyValidator, never()).downloadCertsFromAIA((String)any());
	}
	
	@Test
	public void testGetIntermediateCertsByAIA_errorInDownload_validateEmpty() throws Exception
	{
	
		final TrustChainValidatorWrapper validator = new TrustChainValidatorWrapper()
		{
			protected Collection<X509Certificate> downloadCertsFromAIA(String url) throws NHINDException
			{
				throw new NHINDException();
			}
		};
				
		final TrustChainValidatorWrapper spyValidator = spy(validator);
		
		Collection<X509Certificate> downloadedCerts = spyValidator.getIntermediateCertsByAIA(TestUtils.loadCertificate("demo.sandboxcernerdirect.com.der"));
		
		assertNull(spyValidator.retrievedURL);
		assertEquals(0, downloadedCerts.size());
		
		verify(spyValidator, times(1)).downloadCertsFromAIA((String)any());
	}
}
