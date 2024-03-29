/* 
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Authors:
   Umesh Madan     umeshma@microsoft.com
   Greg Meyer      gm2552@cerner.com
 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
in the documentation and/or other materials provided with the distribution.  Neither the name of the The NHIN Direct Project (nhindirect.org). 
nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.nhindirect.stagent.cert.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.common.options.OptionsParameter;
import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.cert.CacheableCertStore;
import org.nhindirect.stagent.cert.CertCacheFactory;
import org.nhindirect.stagent.cert.CertStoreCachePolicy;
import org.nhindirect.stagent.cert.CertificateStore;
import org.xbill.DNS.CERTRecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import lombok.extern.slf4j.Slf4j;

/**
 * Certificate store backed by DNS CERT records (RFC 4398) for dynamic lookup and a configurable local cache of off line lookup. 
 * By default the service uses the local node's DNS server configuration for initial DNS queries and a key store implementation for 
 * off line lookups.  The default key store creates new file named NHINDKeyStore with a default file and private key password if the 
 * file does not already exist.
 * <br>
 * Depending the OS TCP implementation, lookups may be cached in native DNS resolvers resulting in optimized lookups.  
 * However this may not always in line with HIPS policies.  Refer to you OS DNS implementation for more details.
 * <br>
 * This service caches DNS entries independently of OS resolver.  Caching can be tuned using the {@link CacheableCertStore} interface.
 * By default, the time to live of a subjects DNS certs is one day and the maximum number of entries is 1000 before the cache
 * is pruned to make room for new entries.  Pruning by default uses a least recently used algorithm.
 * 
 * @author Greg Meyer
 *
 */
@Slf4j
public class DNSCertificateStore extends CertificateStore implements CacheableCertStore
{
	private static final String CACHE_NAME = "DNS_REMOTE_CERT_CACHE";
	
	protected static final int DEFAULT_DNS_TIMEOUT = 3; // 3 seconds
	protected static final int DEFAULT_DNS_RETRIES = 2;
	protected static final boolean DEFAULT_DNS_USE_TCP = true;
	
	protected static final int DEFAULT_DNS_MAX_CAHCE_ITEMS = 1000;
	protected static final int DEFAULT_DNS_TTL = 3600; // 1 hour

	protected static final int DEFAULT_URL_CONNECTION_TIMEOUT = 10000; // 10 seconds	
	protected static final int DEFAULT_URL_READ_TIMEOUT = 10000; // 10 hour seconds	
	
	protected CertificateStore localStoreDelegate;
	protected List<String> servers = new ArrayList<String>();
	protected JCS cache;
	protected CertStoreCachePolicy cachePolicy;

	protected int timeout;
	protected int retries;
	protected boolean useTCP;
	
	static 
	{
		Cache ch = Lookup.getDefaultCache(DClass.IN);
		ch.clearCache();
	}
	
	/**
	 * Constructs a service using the machines local DNS server configuration and a default key store implementation for
	 * local lookups.
	 */
	public DNSCertificateStore()
	{
		getServerQuerySettings();
		setServers(null);		
		
		// no longer create a default local
		// bootstrap store by default
		
		// create the in memory cache
		createCache();
	}
	
	
	
	/**
	 * Constructs a service using the server list for DNS lookups and a default key store implementation for
	 * local lookups.
	 * @param servers The DNS users to use for initial certificate resolution.
	 */
	public DNSCertificateStore(Collection<String> servers)
	{
		getServerQuerySettings();
		setServers(servers);
		// no longer create a default local
		// bootstrap store by default
		
		// create the in memory cache
		createCache();
	}
	
	/**
	 * Constructs a service using the server list for DNS lookups and a key store implementation for
	 * local lookups.
	 * @param servers The DNS users to use for initial certificate resolution.
	 * @param localStoreDelegate The certificate store used for local lookups.  This store is also the boot strap store.
	 * The boot strap store may be null.
	 */
	public DNSCertificateStore(Collection<String> servers, 
			CertificateStore bootstrapStore, CertStoreCachePolicy policy)
	{
		// null boot strap store is OK
		
		getServerQuerySettings();
		setServers(servers);
		
		this.cachePolicy = policy;			
		this.localStoreDelegate = bootstrapStore;	
		
		// create the in memory cache
		createCache();
		
		// no longer create a default local
		// bootstrap store by default if the boot strap is null
		if (localStoreDelegate != null)
			loadBootStrap();
	}	
		
	private void getServerQuerySettings()
	{
		OptionsParameter param = OptionsManager.getInstance().getParameter(OptionsParameter.DNS_CERT_RESOLVER_TIMEOUT);
		timeout = OptionsParameter.getParamValueAsInteger(param, DEFAULT_DNS_TIMEOUT);
		
		param = OptionsManager.getInstance().getParameter(OptionsParameter.DNS_CERT_RESOLVER_RETRIES);
		retries = OptionsParameter.getParamValueAsInteger(param, DEFAULT_DNS_RETRIES);
		
		param = OptionsManager.getInstance().getParameter(OptionsParameter.DNS_CERT_RESOLVER_USE_TCP);
		useTCP = OptionsParameter.getParamValueAsBoolean(param, DEFAULT_DNS_USE_TCP);
	}
	
	private synchronized JCS getCache()
	{
		if (cache == null)
			createCache();
		
		return cache;
	}
	
	private void createCache()
	{
		try
		{
			// create instance
			cache = CertCacheFactory.getInstance().getCertCache(CACHE_NAME, cachePolicy == null ? getDefaultPolicy() : cachePolicy);	
			if (cachePolicy == null)
				cachePolicy = getDefaultPolicy();
		}
		///CLOVER:OFF
		catch (CacheException e)
		{
			log.warn("DNSCertificateStore - Could not create certificate cache {}", CACHE_NAME, e);
		}
		///CLOVER:ON
	}
	
	private CertStoreCachePolicy getDefaultPolicy()
	{
		return new DefaultDNSCachePolicy();
	}
	
	/**
	 * Sets the DNS servers used for initial certificate lookups.  This replaces all currently set DNS server configuration.  This method is thread safe and
	 * may block if a current lookup is currently in progress.
	 * @param servers The DNS servers used for initial certificate lookups.
	 */
	public void setServers(Collection<String> servers)
	{
		if (servers == null || servers.size() == 0)
		{
			String[] configedServers = null;
			
			// first check if servers were configured via the options manager
			final OptionsParameter serverOptions = OptionsManager.getInstance().getParameter(OptionsParameter.DNS_CERT_RESOLVER_SERVERS);
			if (serverOptions != null && !StringUtils.isEmpty(serverOptions.getParamValue()))
			{
				configedServers = serverOptions.getParamValue().split(",");
			}
			else // no servers in the options manager, fall back to the local machine's settings
			{
				List<String> holdServer = ResolverConfig.getCurrentConfig().servers().stream().map(addr -> {
					return addr.getHostString();
				}).collect(Collectors.toList());
				
				configedServers = holdServer.toArray(new String[holdServer.size()]);
				
			}

			if (configedServers != null)
			{
				this.servers.addAll(Arrays.asList(configedServers));
			}		
		}		
		else
		{
			this.servers.clear();
			this.servers.addAll(servers);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
    public boolean contains(X509Certificate cert)
    {
    	return localStoreDelegate == null ? false : localStoreDelegate.contains(cert);
    }	
    
	/**
	 * {@inheritDoc}
	 */
    public void add(X509Certificate cert)
    {
    	if (localStoreDelegate != null)
    		localStoreDelegate.add(cert);
    }    
	
	/**
	 * {@inheritDoc}
	 */
    public void remove(X509Certificate cert)
    {
    	if (localStoreDelegate != null)
    		localStoreDelegate.remove(cert);
    }    
    
    
	/**
	 * {@inheritDoc}
	 */  
    @SuppressWarnings("unchecked")
    @Override
    public Collection<X509Certificate> getCertificates(String subjectName)
    {
      	String realSubjectName;
    	int index;
		if ((index = subjectName.indexOf("EMAILADDRESS=")) > -1)
			realSubjectName = subjectName.substring(index + "EMAILADDRESS=".length());
		else
			realSubjectName = subjectName;    	
    	
    	Collection<X509Certificate> retVal;
    	
    	JCS cache = getCache();
    	
    	if (cache != null)
    	{
    		retVal = (Collection<X509Certificate>)cache.get(realSubjectName);
    		if (retVal == null || retVal.size() == 0)
    		{
    			retVal = this.lookupDNS(realSubjectName);
    			if (retVal == null || retVal.size() == 0)
    			{
    				log.info("getCertificates(String subjectName) - Could not find a DNS certificate for subject {}", subjectName);
    			}
    		}
    	}
    	else // cache miss
    	{
    		retVal = this.lookupDNS(realSubjectName);
    		if (retVal.size() == 0)
    		{
    			if (localStoreDelegate != null)
    			{
	    			retVal = localStoreDelegate.getCertificates(realSubjectName); // last ditch effort is to go to the bootstrap cache
	    			if (retVal == null || retVal.size() == 0)
	    			{
	    				log.info("getCertificates(String subjectName) - Could not find a DNS certificate for subject {}", subjectName);
	    			}
    			}
    			else 
    				log.info("getCertificates(String subjectName) - Could not find a DNS certificate for subject {}", subjectName);
    		}
    	}
    	
    	return retVal;
    }     
    
	/**
	 * {@inheritDoc}
	 */
    @Override
    public Collection<X509Certificate> getAllCertificates()
    {
    	return (localStoreDelegate == null) ? null : localStoreDelegate.getAllCertificates(); 
    }    
    
	protected Collection<X509Certificate> lookupDNS(String name)
	{
		String domain;
		String lookupName = name.replace('@', '.');
		Collection<X509Certificate> retVal = new ArrayList<X509Certificate>();
				
		// get the domain of the address
		int index;
		if ((index = name.indexOf("@")) > -1)
			domain = name.substring(index + 1);
		else
			domain = name;
		
		try
		{
			// try the configured servers first
			Lookup lu = new Lookup(new Name(lookupName), Type.CERT);
			lu.setResolver(createExResolver(servers.toArray(new String[servers.size()]), retries, timeout)); // default retries is 3, limite to 2
			lu.setSearchPath((String[])null);
			
			Record[] retRecords = null;
			
			try
			{
				retRecords = lu.run();
			}
			catch (Exception e)
			{
				log.warn("Error using recusive DNS CERT lookup for name {}" +
						"\r\nFalling back to looking up NS record for a targeted search", lookupName  , e);
			}
			
			if (retRecords == null || retRecords.length == 0)
			{
				Name tempDomain;
				
				// try to find the resource's name server records
				// the address may be an alias so check if there is a CNAME record
				lu = new Lookup(new Name(lookupName), Type.CNAME);
				lu.setResolver(createExResolver(servers.toArray(new String[servers.size()]), retries, timeout));
				lu.setSearchPath((String[])null);
				
				retRecords = lu.run();	
				if (retRecords != null && retRecords.length > 0)
				{
					CNAMERecord cnameRect = (CNAMERecord)retRecords[0];
					tempDomain = cnameRect.getTarget();
				}
				else
					tempDomain = new Name(domain);  // not a CNAME						
					
				// look for a name server records
				while (tempDomain.labels() > 1)
				{
					lu = new Lookup(tempDomain, Type.NS);
					lu.setResolver(createExResolver(servers.toArray(new String[servers.size()]), retries, timeout));
					lu.setSearchPath((String[])null);
					retRecords = lu.run();
					
					if (retRecords != null && retRecords.length > 0)
						break;
					
					tempDomain = new Name(tempDomain.toString().substring((tempDomain.toString().indexOf(".") + 1)));
				}
				
				if (retRecords == null || retRecords.length == 0)
					return retVal; // can't find a name server... bail
				
				String[] remoteServers = new String[retRecords.length];
				for (int i = 0; i < remoteServers.length - 0; ++i)
				{
					remoteServers[i] = ((NSRecord)retRecords[i]).getTarget().toString();
				}
				
				// search the name servers for the cert
				lu = new Lookup(new Name(lookupName), Type.CERT);
				ExtendedResolver remoteResolver = createExResolver(remoteServers, 2, 3);
				if (remoteResolver.getResolvers().length > 0) {
					lu.setResolver(remoteResolver);
					lu.setSearchPath((String[])null);
					
					// CLEAR THE CACHE!!!  We are seeing instances where an NXRRSET is cached because
					// a DNS provider is trying to handle a request that it should be delegating
					// The purpose of bypassing the DNS provider and going directly to the NS server
					// is to avoid issues like this
					
					/*
					 * Change of heart on clearing the DNS cache.  Covering up the NXRRSET hides potential issues
					 * with incorrect DNS configuration.  It is important that NXRRSET issues are discovered and corrected
					 * so all participants in the community participate in a consistent manner.
					 */
					//lu.setCache(new Cache(DClass.IN));
					
					retRecords = lu.run();
				} else {
					// null out NS records
					retRecords = null;
				}
			}
						
			if (retRecords != null)
			{
				retVal = new ArrayList<X509Certificate>();
				for (Record rec : retRecords)
				{
					if (rec instanceof CERTRecord) 
					{
						CERTRecord certRec = (CERTRecord)rec;
						switch(certRec.getCertType())
						{
							case CERTRecord.PKIX:
							{
								Certificate certToAdd = convertPKIXRecordToCert(certRec);
								if (certToAdd != null && certToAdd instanceof X509Certificate) // may not be an X509Cert
									retVal.add((X509Certificate)certToAdd);
								break;
							}
							case CERTRecord.URI:
							{
								Certificate certToAdd = convertIPKIXRecordToCert(certRec);
								if (certToAdd != null && certToAdd instanceof X509Certificate) // may not be an X509Cert
									retVal.add((X509Certificate)certToAdd);
								break;
							}
							default:
							{
								log.warn("Unknown CERT type {} encountered for lookup name {}",  certRec.getCertType(),  lookupName);
							}
						}
					}
				}			
			}
			else if (domain.length() < name.length())  // if this is an email address, do the search again and the host level
				retVal = lookupDNS(domain);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new NHINDException("", e);
		}
		
		// add or update the local cert store
		if (retVal != null && retVal.size() > 0 && localStoreDelegate != null)
		{
			for (X509Certificate cert : retVal)
			{

				if (localStoreDelegate != null)
				{
					if (localStoreDelegate.contains(cert)) 
						localStoreDelegate.update(cert);
					else
						localStoreDelegate.add(cert);
				}
			}			
			try
			{
				if (cache != null)
					cache.put(name, retVal);
			}
			catch (CacheException e)
			{
				/*
				 * TODO: handle exception
				 */
			}
		}
		return retVal;
	}

	
	public void flush(boolean purgeBootStrap) 
	{
		
		if (cache != null)
		{
			try
			{
				cache.clear();
			}
			catch (CacheException e)
			{
				/**
				 * TODO: handle exception
				 */
			}
		
			if (purgeBootStrap && this.localStoreDelegate != null)
			{
				localStoreDelegate.remove(localStoreDelegate.getAllCertificates());
			}
		}
	}

	@SuppressWarnings("unused")
	public void loadBootStrap() 
	{
		if (localStoreDelegate == null)
			throw new IllegalStateException("The boot strap store has not been set.");
		

		JCS cache = null;
		if ((cache = getCache()) != null)
		{
			Map<String, Collection<X509Certificate>> cacheBuilderMap = new HashMap<String, Collection<X509Certificate>>();
			for (X509Certificate cert : localStoreDelegate.getAllCertificates())
			{
				/*
				 * TODO: need to decide how the entries/subjects will be indexed and named
				 */
			}
			
			for (Entry<String, Collection<X509Certificate>> entry : cacheBuilderMap.entrySet())
			{
				try
				{
					cache.put(entry.getKey(), entry.getValue());
				}
				catch (CacheException e)
				{
					/*
					 * TODO: handle exception
					 */
				}
			}
		}
	}

	public void loadBootStrap(CertificateStore bootstrapStore) 
	{
		if (localStoreDelegate == null)
		{
			throw new IllegalArgumentException();
		}
		this.localStoreDelegate = bootstrapStore;
		loadBootStrap();
	}

	public void setBootStrap(CertificateStore bootstrapStore) 
	{
		if (localStoreDelegate == null)
		{
			throw new IllegalArgumentException();
		}
		this.localStoreDelegate = bootstrapStore;		
	}

	public void setCachePolicy(CertStoreCachePolicy policy) 
	{		
		this.cachePolicy = policy;
		applyCachePolicy(policy);
	}
	
	private void applyCachePolicy(CertStoreCachePolicy policy)
	{
		if (getCache() != null)
		{
			try
			{
				ICompositeCacheAttributes attributes = cache.getCacheAttributes();
				attributes.setMaxObjects(policy.getMaxItems());
				attributes.setUseLateral(false);
				attributes.setUseRemote(false);
				cache.setCacheAttributes(attributes);
				
				IElementAttributes eattributes = cache.getDefaultElementAttributes();
				eattributes.setMaxLifeSeconds(policy.getSubjectTTL());
				eattributes.setIsEternal(false);
				eattributes.setIsLateral(false);
				eattributes.setIsRemote(false);		
				
				cache.setDefaultElementAttributes(eattributes);
			}
			catch (CacheException e)
			{
				// TODO: Handle exception
			}
		}
	}
	
	public static class DefaultDNSCachePolicy implements CertStoreCachePolicy
	{
		protected final int maxItems;
		protected final int subjectTTL;
		
		public DefaultDNSCachePolicy()
		{
			OptionsParameter param = OptionsManager.getInstance().getParameter(OptionsParameter.DNS_CERT_RESOLVER_MAX_CACHE_SIZE);
			maxItems =  OptionsParameter.getParamValueAsInteger(param, DEFAULT_DNS_MAX_CAHCE_ITEMS); 
			
			param = OptionsManager.getInstance().getParameter(OptionsParameter.DNS_CERT_RESOLVER_CACHE_TTL);
			subjectTTL =  OptionsParameter.getParamValueAsInteger(param, DEFAULT_DNS_TTL); 
		}
		
		public int getMaxItems() 
		{
			return maxItems;
		}

		public int getSubjectTTL() 
		{
			return subjectTTL;
		}
		
	}
	
	protected ExtendedResolver createExResolver(String[] servers, int retries, int timeout)
	{
		// create a default ExtendedResolver
		ExtendedResolver extendedResolver = new ExtendedResolver();


		// remove all resolvers from default ExtendedResolver
		Resolver[] resolvers = extendedResolver.getResolvers();
		if (!ArrayUtils.isEmpty(resolvers)) {
			for (Resolver resolver : resolvers) {
				extendedResolver.deleteResolver(resolver);
			}
		}

		// add the specified servers
		if (!ArrayUtils.isEmpty(servers)) {
			for (String server : servers) {
				// support for IP addresses instead of names
				server = server.replaceFirst("\\.$", "");

				try {
					// create and add a SimpleResolver for each server
					SimpleResolver simpleResolver = new SimpleResolver(server);
					extendedResolver.addResolver(simpleResolver);
				} catch (UnknownHostException e) {
					log.debug("unable to add resolver for {}", server, e);
					continue;
				}
			}
			extendedResolver.setRetries(retries);
			extendedResolver.setTimeout(Duration.ofSeconds(timeout));
			extendedResolver.setTCP(useTCP);
		}

		return extendedResolver;
	}
	
	@SuppressWarnings("deprecation")
	protected Certificate convertPKIXRecordToCert(CERTRecord certRec)
	{
		Certificate retVal = null;
		ByteArrayInputStream inputStream = null;
		final byte[] certData = certRec.getCert();


		try
		{
			final CertificateFactory cf = CertificateFactory.getInstance("X.509");
			inputStream = new ByteArrayInputStream(certData);
			retVal = (X509Certificate)cf.generateCertificate(inputStream);
		}
		catch (Exception e)
		{
			log.warn("Failed to convert certificate from DNS byte data.", e);
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
		
		return retVal;
	}
	
	@SuppressWarnings("deprecation")
	protected Certificate convertIPKIXRecordToCert(CERTRecord certRec)
	{
		Certificate retVal = null;
		InputStream inputStream = null;

		try
		{
			// in this case the cert is a binary representation
			// of the CERT URL... transform to a string
			final URL certURL = getCertURL(certRec);
			
			final URLConnection connection = certURL.openConnection();
			
			// the connection is not actually made until the input stream
			// is open, so set the timeouts before getting the stream
			connection.setConnectTimeout(DEFAULT_URL_CONNECTION_TIMEOUT);
			connection.setReadTimeout(DEFAULT_URL_READ_TIMEOUT);
			
			// open the URL as in input stream
			inputStream = connection.getInputStream();
			
			final CertificateFactory cf = CertificateFactory.getInstance("X.509");
			retVal = (X509Certificate)cf.generateCertificate(inputStream);
		}
		catch (Exception e)
		{
			log.warn("Failed to get cert recrod from IPKIX location.", e);
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
		
		return retVal;
	}
	
	protected URL getCertURL(CERTRecord certRec) throws MalformedURLException, UnsupportedEncodingException  
	{
		final URL certURL = new URL(new String(certRec.getCert(), "ASCII"));
		
		return certURL;
	}
}
