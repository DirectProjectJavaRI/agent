package org.nhindirect.stagent.trust;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.cert.CertificateResolver;
import org.nhindirect.stagent.trust.TrustChainValidator_getIntermediateCertsByAIATest.TrustChainValidatorWrapper;
import org.nhindirect.stagent.utils.TestUtils;

public class TrustChainValidator_resolveIssuersTest
{
	@Test
	public void testResolveIssuers_AIAExists_validateResolved() throws Exception
	{
	
		final TrustChainValidatorWrapper validator = new TrustChainValidatorWrapper()
		{			
			@Override
			protected Collection<X509Certificate> downloadCertsFromAIA(String url) throws NHINDException
			{
				try
				{
					retrievedURL = url;
					return Arrays.asList(TestUtils.loadCertificate("CernerDirect Cert Professional Community CA.der"));
				}
				catch (Exception e){throw new NHINDException(e);}
			}
		};
				
    	final Collection<X509Certificate> resolvedIssuers = new ArrayList<X509Certificate>();
    	final Collection<X509Certificate> anchors = new ArrayList<X509Certificate>();
    	final TrustChainValidatorWrapper spyValidator = spy(validator);
		
		
		spyValidator.resolveIssuers(TestUtils.loadCertificate("demo.sandboxcernerdirect.com.der"), resolvedIssuers, 0, anchors);
		
		assertEquals(1, resolvedIssuers.size());
		assertEquals(TestUtils.loadCertificate("CernerDirect Cert Professional Community CA.der"), resolvedIssuers.iterator().next());
		
		verify(spyValidator, times(2)).downloadCertsFromAIA((String)any());
	}
	
	@Test
	public void testResolveIssuers_AIAExists_resolveToRoot_validateResolved() throws Exception
	{
	
		final TrustChainValidatorWrapper validator = new TrustChainValidatorWrapper()
		{
			protected Collection<X509Certificate> downloadCertsFromAIA(String url) throws NHINDException
			{
				try
				{
					if (url.contains("sandbox"))
						return Arrays.asList(TestUtils.loadCertificate("CernerDirect Cert Professional Community CA.der"));
					else
						return Arrays.asList(TestUtils.loadCertificate("CernerRoot.der"));
				}
				catch (Exception e){throw new NHINDException(e);}
			}
		};
				
    	final Collection<X509Certificate> resolvedIssuers = new ArrayList<X509Certificate>();
    	final Collection<X509Certificate> anchors = new ArrayList<X509Certificate>();
    	final TrustChainValidatorWrapper spyValidator = spy(validator);
		
		
		spyValidator.resolveIssuers(TestUtils.loadCertificate("demo.sandboxcernerdirect.com.der"), resolvedIssuers, 0, anchors);
		
		assertEquals(2, resolvedIssuers.size());
		Iterator<X509Certificate> iter = resolvedIssuers.iterator();
		assertEquals(TestUtils.loadCertificate("CernerDirect Cert Professional Community CA.der"), iter.next());
		assertEquals(TestUtils.loadCertificate("CernerRoot.der"), iter.next());
		
		verify(spyValidator, times(2)).downloadCertsFromAIA((String)any());
	}
	
	@Test
	public void testResolveIssuers_noAIAExists_notAvailViaResolver_validateNotResolved() throws Exception
	{
	
		final TrustChainValidatorWrapper validator = new TrustChainValidatorWrapper()
		{
			protected Collection<X509Certificate> downloadCertsFromAIA(String url) throws NHINDException
			{
				throw new NHINDException();
			}
		};
		
		validator.setCertificateResolver(new ArrayList<CertificateResolver>());
				
    	final Collection<X509Certificate> resolvedIssuers = new ArrayList<X509Certificate>();
    	final Collection<X509Certificate> anchors = new ArrayList<X509Certificate>();
    	final TrustChainValidatorWrapper spyValidator = spy(validator);
		
		
		spyValidator.resolveIssuers(TestUtils.loadCertificate("altNameOnly.der"), resolvedIssuers, 0, anchors);
		
		assertEquals(0, resolvedIssuers.size());
		
		verify(spyValidator, times(0)).downloadCertsFromAIA((String)any());
	}
}
