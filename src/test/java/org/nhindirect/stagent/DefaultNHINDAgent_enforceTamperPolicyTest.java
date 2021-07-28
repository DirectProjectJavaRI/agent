package org.nhindirect.stagent;

import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.common.options.OptionsManagerUtils;
import org.nhindirect.common.options.OptionsParameter;
import org.nhindirect.stagent.cert.CertificateResolver;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.trust.TrustAnchorResolver;
import org.nhindirect.stagent.utils.TestUtils;

public class DefaultNHINDAgent_enforceTamperPolicyTest 
{
	protected Message originalMessage;
	
	protected DefaultNHINDAgent agent;
	
	@BeforeEach
	public void setUp() throws Exception
	{
		OptionsManagerUtils.clearOptionsManagerInstance();
		
		String testMessage = TestUtils.readResource("MessageWithAttachment.txt");
		
		originalMessage = new Message(new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII"))));
		
		agent = new DefaultNHINDAgent("widget", mock(CertificateResolver.class), mock(CertificateResolver.class), mock(TrustAnchorResolver.class));
	}
	
	@AfterEach
	public void tearDown()
	{
		OptionsManagerUtils.clearOptionsManagerInstance();
	}
	
	@Test
	public void testEnforceTamperPolicy_tamperedSingleRecip_policyOn_assertException() throws Exception
	{
		Assertions.assertThrows(AgentException.class, () ->
		{
			final NHINDAddressCollection recipients = NHINDAddressCollection.create(Collections.singleton(new NHINDAddress("not@exists.com")));
			
			final NHINDAddress sender = new NHINDAddress(originalMessage.getFrom()[0].toString());
			
			// The default policy is to enforce the policy, so no change in options params needed
			final IncomingMessage msg = new IncomingMessage(originalMessage, recipients, sender);
			
			agent.enforceTamperPolicy(msg);
		});
	}
	
	@Test
	public void testEnforceTamperPolicy_tamperedMultipleRecips_policyOn_assertException() throws Exception
	{
		Assertions.assertThrows(AgentException.class, () ->
		{
			final Collection<NHINDAddress> recips = new ArrayList<>();
			
			Arrays.stream(originalMessage.getAllRecipients()).forEach(addr -> recips.add(new NHINDAddress((InternetAddress)addr)));
			
			recips.add(new NHINDAddress("not@exists.com"));
			
			final NHINDAddressCollection recipients = NHINDAddressCollection.create(recips);
			
			final NHINDAddress sender = new NHINDAddress(originalMessage.getFrom()[0].toString());
			
			// The default policy is to enforce the policy, so no change in options params needed
			final IncomingMessage msg = new IncomingMessage(originalMessage, recipients, sender);
			
			agent.enforceTamperPolicy(msg);
		});
	}	
	
	@Test
	public void testEnforceTamperPolicy_tamperedFrom_policyOn_assertException() throws Exception
	{
		
		Assertions.assertThrows(AgentException.class, () ->
		{
			final Collection<NHINDAddress> recips = new ArrayList<>();
			
			Arrays.stream(originalMessage.getAllRecipients()).forEach(addr -> recips.add(new NHINDAddress((InternetAddress)addr)));
			
			final NHINDAddressCollection recipients = NHINDAddressCollection.create(recips);
			
			final NHINDAddress sender = new NHINDAddress("not@exists.com");
			
			// The default policy is to enforce the policy, so no change in options params needed
			final IncomingMessage msg = new IncomingMessage(originalMessage, recipients, sender);
			
			agent.enforceTamperPolicy(msg);
		});
	}	
	
	@Test
	public void testEnforceTamperPolicy_tamperedSingleRecip_policyOff_noException() throws Exception
	{
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.REJECT_ON_ROUTING_TAMPER, "false"));

		final NHINDAddressCollection recipients = NHINDAddressCollection.create(Collections.singleton(new NHINDAddress("not@exists.com")));
		
		final NHINDAddress sender = new NHINDAddress(originalMessage.getFrom()[0].toString());
		
		// The default policy is to enforce the policy, so no change in options params needed
		final IncomingMessage msg = new IncomingMessage(originalMessage, recipients, sender);
		
		agent.enforceTamperPolicy(msg);
	}
	
	@Test
	public void testEnforceTamperPolicy_tamperedFrom_policyOff_noException() throws Exception
	{
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.REJECT_ON_ROUTING_TAMPER, "false"));

		final Collection<NHINDAddress> recips = new ArrayList<>();
		
		Arrays.stream(originalMessage.getAllRecipients()).forEach(addr -> recips.add(new NHINDAddress((InternetAddress)addr)));
		
		final NHINDAddressCollection recipients = NHINDAddressCollection.create(recips);
		
		final NHINDAddress sender = new NHINDAddress("not@exists.com");
		
		// The default policy is to enforce the policy, so no change in options params needed
		final IncomingMessage msg = new IncomingMessage(originalMessage, recipients, sender);
		
		agent.enforceTamperPolicy(msg);
	}	
	
	@Test
	public void testEnforceTamperPolicy_untampered_noException() throws Exception
	{
		final Collection<NHINDAddress> recips = new ArrayList<>();
		
		Arrays.stream(originalMessage.getAllRecipients()).forEach(addr -> recips.add(new NHINDAddress((InternetAddress)addr)));
		
		final NHINDAddressCollection recipients = NHINDAddressCollection.create(recips);
		
		final NHINDAddress sender = new NHINDAddress(originalMessage.getFrom()[0].toString());
		
		// The default policy is to enforce the policy, so no change in options params needed
		final IncomingMessage msg = new IncomingMessage(originalMessage, recipients, sender);
		
		agent.enforceTamperPolicy(msg);
	}		
}
