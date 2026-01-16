package org.nhindirect.stagent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import jakarta.mail.internet.InternetAddress;


public class NHINDAddressTest
{
	@Test
	public void testSetGetPersonllAttribute() throws Exception
	{
		NHINDAddress address = new NHINDAddress("Greg Meyer <gm2552@cerner.com>");
		
		assertEquals("gm2552@cerner.com", address.getAddress().toString());
		assertEquals("Greg Meyer", address.getPersonal());
		
		List<X509Certificate> empty = Collections.emptyList();
		
		address = new NHINDAddress("Greg Meyer <gm2552@cerner.com>", empty);
		
		assertEquals("gm2552@cerner.com", address.getAddress().toString());
		assertEquals("Greg Meyer", address.getPersonal());	
		
		
		address = new NHINDAddress(new InternetAddress("Greg Meyer <gm2552@cerner.com>"));
		
		assertEquals("gm2552@cerner.com", address.getAddress().toString());
		assertEquals("Greg Meyer", address.getPersonal());			
	}
}
