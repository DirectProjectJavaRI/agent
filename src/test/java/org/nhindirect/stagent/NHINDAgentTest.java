package org.nhindirect.stagent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.mail.Header;
import javax.mail.internet.MimeMessage;

import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.common.options.OptionsManagerUtils;
import org.nhindirect.common.options.OptionsParameter;
import org.nhindirect.stagent.cert.CertificateResolver;
import org.nhindirect.stagent.mail.MailStandard;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.mail.MimeStandard;
import org.nhindirect.stagent.trust.DefaultTrustAnchorResolver;
import org.nhindirect.stagent.trust.TrustError;
import org.nhindirect.stagent.utils.TestUtils;

public class NHINDAgentTest
{	
	static
	{
		// Override the logging system to turn on trace level logging
		// Need to make sure debug logging is tested to check for possible null reference errors
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "trace");
	}
	
	@BeforeEach
	public void setUp()
	{
		OptionsManagerUtils.clearOptionsManagerInstance();
	}
	
	@AfterEach
	public void tearDown()
	{
		OptionsManagerUtils.clearOptionsManagerOptions();
	}
	
	@Test
	public void testEndToEndMessageWithCertKeyStore() throws Exception
	{				
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"cerner.com"}));
		
		String testMessage = TestUtils.readResource("MultipartMimeMessage.txt");
		MimeMessage originalMsg = new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII")));

		
		OutgoingMessage SMIMEenvMessage = agent.processOutgoing(testMessage);
		
		assertNotNull(SMIMEenvMessage);
		assertTrue(SMIMEenvMessage.getMessage().toString().length() > 0);

		
		// verify the message
		// need a new agent because this is a different domain
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"starugh-stateline.com"})); 
		IncomingMessage strippedAndVerifiesMessage = agent.processIncoming(SMIMEenvMessage.getMessage().toString());
		
		
		assertNotNull(strippedAndVerifiesMessage);
		assertTrue(strippedAndVerifiesMessage.getMessage().toString().length() > 0);
		
		MimeMessage processedMsg = new MimeMessage(null, 
				new ByteArrayInputStream(strippedAndVerifiesMessage.getMessage().toString().getBytes("ASCII")));
		
		assertNotNull(processedMsg);
		
		// can't do a direct compare on headers because the processing may strip some of the recipients
		assertTrue(processedMsg.getContentType().compareTo(originalMsg.getContentType()) == 0);
		assertTrue(originalMsg.getSubject().compareTo(processedMsg.getSubject()) == 0);
				
		// get the message data and compare
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int count = 0;
		InputStream inStream = originalMsg.getInputStream();
		while ((count = inStream.read(buffer)) > -1)
			oStream.write(buffer, 0, count);
		
		oStream = new ByteArrayOutputStream();
		count = 0;
		inStream = processedMsg.getInputStream();
		while ((count = inStream.read(buffer)) > -1)
			oStream.write(buffer, 0, count);		
		
		// now do a large message with some attachments
		agent =  TestUtils.getStockAgent(Arrays.asList(new String[]{"securehealthemail.com"})); 
		
		testMessage = TestUtils.readResource("LargeMsgWithAttachments.txt");
		originalMsg = new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII")));

		
		SMIMEenvMessage = agent.processOutgoing(testMessage);
	
		
		assertNotNull(SMIMEenvMessage);
		assertTrue(SMIMEenvMessage.getMessage().toString().length() > 0);

		
		// verify the message
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"securehealthemail.com"})); 
		strippedAndVerifiesMessage = agent.processIncoming(SMIMEenvMessage);
		
		
		assertNotNull(strippedAndVerifiesMessage);
		assertTrue(strippedAndVerifiesMessage.getMessage().toString().length() > 0);
		
		processedMsg = new MimeMessage(null, new ByteArrayInputStream(strippedAndVerifiesMessage.
				getMessage().toString().getBytes("ASCII")));
		
		assertNotNull(processedMsg);
		
		// can't do a direct compare on headers because the processing may strip some of the recipients
		assertTrue(processedMsg.getContentType().compareTo(originalMsg.getContentType()) == 0);
		assertTrue(originalMsg.getSubject().compareTo(processedMsg.getSubject()) == 0);
				
		// get the message data and compare
		oStream = new ByteArrayOutputStream();
		buffer = new byte[1024];
		 count = 0;
		inStream = originalMsg.getInputStream();
		while ((count = inStream.read(buffer)) > -1)
			oStream.write(buffer, 0, count);
		
		oStream = new ByteArrayOutputStream();
		count = 0;
		inStream = processedMsg.getInputStream();
		while ((count = inStream.read(buffer)) > -1)
			oStream.write(buffer, 0, count);
		
		// do an MDN response message
		
		
		agent =  TestUtils.getStockAgent(Arrays.asList(new String[]{"cerner.com"})); 
		
		testMessage = TestUtils.readResource("MDNResponse.txt");
		originalMsg = new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII")));
		
		SMIMEenvMessage = agent.processOutgoing(testMessage);

		
		assertNotNull(SMIMEenvMessage);
		assertTrue(SMIMEenvMessage.getMessage().toString().length() > 0);

		
		// verify the message
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"starugh-stateline.com"})); 
		strippedAndVerifiesMessage = agent.processIncoming(SMIMEenvMessage);
		
		
		assertNotNull(strippedAndVerifiesMessage);
		assertTrue(strippedAndVerifiesMessage.getMessage().toString().length() > 0);
		
		processedMsg = new MimeMessage(null, new ByteArrayInputStream(strippedAndVerifiesMessage.
				getMessage().toString().getBytes("ASCII")));
		
		assertNotNull(processedMsg);
		
		// can't do a direct compare on headers because the processing may strip some of the recipients
		assertTrue(processedMsg.getContentType().compareTo(originalMsg.getContentType()) == 0);
				
		// get the message data and compare
		oStream = new ByteArrayOutputStream();
		buffer = new byte[1024];
		 count = 0;
		inStream = originalMsg.getInputStream();
		while ((count = inStream.read(buffer)) > -1)
			oStream.write(buffer, 0, count);

		
		oStream = new ByteArrayOutputStream();
		count = 0;
		inStream = processedMsg.getInputStream();
		while ((count = inStream.read(buffer)) > -1)
			oStream.write(buffer, 0, count);
				
	}
	
	@Test
	public void testDecryptProvidedMessage() throws Exception
	{	
		/*
		 * EncryptedMessage2
		 */		
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"securehealthemail.com"})); 
		String testMessage = TestUtils.readResource("EncryptedMessage2.txt");
		MimeMessage originalMsg = new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII")));
		
		
		// verify the message
		// need a new agent because this is a different domain);
		IncomingMessage strippedAndVerifiesMessage = agent.processIncoming(originalMsg);
		
		
		assertNotNull(strippedAndVerifiesMessage);
		assertTrue(strippedAndVerifiesMessage.getMessage().toString().length() > 0);

	}
	
	@Test
	public void testRejectMessageOnRoutingTamper_policyTrue_assertMessageRejected() throws Exception
	{
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.REJECT_ON_ROUTING_TAMPER, "true"));
		/*
		 * EncryptedMessage2
		 */		
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"securehealthemail.com"})); 
		String testMessage = TestUtils.readResource("EncryptedMessage2.txt");
		Message originalMsg = new Message(new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII"))));
		
		// add an extra recipient that should not receive this message
		final NHINDAddressCollection tamperedRecips = new NHINDAddressCollection();
		tamperedRecips.add(new NHINDAddress("ryan@securehealthemail.com"));
		tamperedRecips.add(new NHINDAddress("john@securehealthemail.com"));
		
		
		final IncomingMessage inMessage = new IncomingMessage(originalMsg, tamperedRecips, new NHINDAddress(originalMsg.getFrom()[0].toString()));

		boolean exceptionOccured = false;

		try
		{
			agent.processIncoming(inMessage);
		}
		catch (AgentException e)
		{
			assertEquals(AgentError.MessageTamperDectection, e.getError());
			exceptionOccured = true;
		}
		assertTrue(exceptionOccured);
		
		OptionsManager.destroyInstance();
	}
	
	@Test
	public void testRejectMessageOnRoutingTamper_policyFalse_assertDecrtyped() throws Exception
	{
		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.REJECT_ON_ROUTING_TAMPER, "false"));
		
		/*
		 * EncryptedMessage2
		 */		
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"securehealthemail.com"})); 
		String testMessage = TestUtils.readResource("EncryptedMessage2.txt");
		Message originalMsg = new Message(new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII"))));
		
		// add an extra recipient that should not receive this message
		final NHINDAddressCollection tamperedRecips = new NHINDAddressCollection();
		tamperedRecips.add(new NHINDAddress("ryan@securehealthemail.com"));
		tamperedRecips.add(new NHINDAddress("john@securehealthemail.com"));
		
		
		final IncomingMessage inMessage = new IncomingMessage(originalMsg, tamperedRecips, new NHINDAddress(originalMsg.getFrom()[0].toString()));
		IncomingMessage strippedAndVerifiesMessage = agent.processIncoming(inMessage);
		
		assertNotNull(strippedAndVerifiesMessage);
		assertTrue(strippedAndVerifiesMessage.getMessage().toString().length() > 0);
	}
	
	@Test
	public void testDecryptAttachmentMessage() throws Exception
	{
		
	
		// get the keystore file		
	
		
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"securehealthemail.com"})); 
		
		String testMessage = TestUtils.readResource("EncAttachment.txt");
		MimeMessage originalMsg = new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII")));
			
		
		
		// verify the message
		// need a new agent because this is a different domain);
		IncomingMessage strippedAndVerifiesMessage = agent.processIncoming(originalMsg);
		
		
		assertNotNull(strippedAndVerifiesMessage);
		assertTrue(strippedAndVerifiesMessage.getMessage().toString().length() > 0);	
	}	
	
	@Test
	public void testEndToEndMessageBase64AttachmentOnly() throws Exception
	{

		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"messaging.cernerdemos.com"})); 
		String testMessage = TestUtils.readResource("raw2.txt");
		MimeMessage originalMsg = new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII")));		
		
		OutgoingMessage SMIMEenvMessage = agent.processOutgoing(testMessage);
		
		assertNotNull(SMIMEenvMessage);
		assertTrue(SMIMEenvMessage.getMessage().toString().length() > 0);

		
		// verify the message
		// need a new agent because this is a different domain
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"securehealthemail.com"}));
		IncomingMessage strippedAndVerifiesMessage = agent.processIncoming(SMIMEenvMessage);
		
		
		assertNotNull(strippedAndVerifiesMessage);
		assertTrue(strippedAndVerifiesMessage.getMessage().toString().length() > 0);
		
		MimeMessage processedMsg = new MimeMessage(null, 
				new ByteArrayInputStream(strippedAndVerifiesMessage.getMessage().toString().getBytes("ASCII")));
		
		assertNotNull(processedMsg);
		
		// can't do a direct compare on headers because the processing may strip some of the recipients
		assertTrue(processedMsg.getContentType().compareTo(originalMsg.getContentType()) == 0);
		assertTrue(originalMsg.getSubject().compareTo(processedMsg.getSubject()) == 0);
				
		// get the message data and compare
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int count = 0;
		InputStream inStream = originalMsg.getInputStream();
		while ((count = inStream.read(buffer)) > -1)
			oStream.write(buffer, 0, count);
		
		oStream = new ByteArrayOutputStream();
		count = 0;
		inStream = processedMsg.getInputStream();
		while ((count = inStream.read(buffer)) > -1)
			oStream.write(buffer, 0, count);
		
	}
	
	@Test
	public void testOutgoingMessageIsWrappedCorrectly() throws Exception
	{

		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"messaging.cernerdemos.com"})); 
		String testMessage = TestUtils.readResource("raw2.txt");
		
		Message wrappedMessage = agent.wrapMessage(testMessage);
		
		assertNotNull(wrappedMessage);
		assertTrue(wrappedMessage.toString().length() > 0);
		assertEquals(MailStandard.MediaType.WrappedMessage, wrappedMessage.getContentType());
		List<String> headers = new ArrayList<String>();
		Enumeration<Header> allHeaders = wrappedMessage.getAllHeaders();
		while(allHeaders.hasMoreElements()) {
			Header header = (Header) allHeaders.nextElement();
			headers.add(header.getName());
		}
		
		// assert that the headers (according to the NHIND spec) are copied correctly from the 
		// original message into the wrapped message
		assertTrue(headers.contains(MimeStandard.VersionHeader));
		assertTrue(headers.contains("From"));
		assertTrue(headers.contains("To"));
		assertTrue(headers.contains("Message-ID"));
		
		// The wrapped message should not contain Subject header
		assertFalse(headers.contains("Subject"));
	}
	
	@Test
	public void testMessageWithUntrustedRecipient_OutboundMessageHasRejectedRecips() throws Exception
	{

		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"messaging.cernerdemos.com"})); 
		String testMessage = TestUtils.readResource("MessageWithAUntrustedRecipient.txt");
		
		OutgoingMessage SMIMEenvMessage = agent.processOutgoing(testMessage);
		
		assertNotNull(SMIMEenvMessage);
		assertTrue(SMIMEenvMessage.getMessage().toString().length() > 0);
		assertTrue(SMIMEenvMessage.hasRejectedRecipients());
 
		assertNull(SMIMEenvMessage.getMessage().getCCHeader());
		
		// verify the message
		// need a new agent because this is a different domain
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"securehealthemail.com"}));
		IncomingMessage strippedAndVerifiesMessage = agent.processIncoming(SMIMEenvMessage);
		
		
		assertNotNull(strippedAndVerifiesMessage);
		assertTrue(strippedAndVerifiesMessage.getMessage().toString().length() > 0);
		
		MimeMessage processedMsg = new MimeMessage(null, 
				new ByteArrayInputStream(strippedAndVerifiesMessage.getMessage().toString().getBytes("ASCII")));
		
		assertNotNull(processedMsg);
	}
	
	@Test
	public void testMessageWithAllUntrustedRecipients_AgentRejectsTheMessageCompletely() throws Exception
	{

		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"messaging.cernerdemos.com"})); 
		String testMessage = TestUtils.readResource("MessageWithAllUntrustedRecipients.txt");
		
		OutgoingMessage SMIMEenvMessage=null;
		try {
			SMIMEenvMessage = agent.processOutgoing(testMessage);
		}
		catch(NHINDException e) {
			AgentError agentError = (AgentError) e.m_error;
			assertEquals(AgentError.Unexpected, agentError);
		}
		
		assertNull(SMIMEenvMessage);
	}
	
	@Test
	public void testIncomingMDN_incomingNotTrusted_outgoingTrusted_assertMDNMessageTrusted() throws Exception
	{

		OptionsManager.getInstance().setOptionsParameter(
				new OptionsParameter(OptionsParameter.USE_OUTGOING_POLICY_FOR_INCOMING_NOTIFICATIONS, "true"));
		
		// first create the encyrpted message
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"starugh-stateline.com"})); 
		String testMessage = TestUtils.readResource("MDNMessage.txt");
		
		final OutgoingMessage SMIMEenvMessage = agent.processOutgoing(testMessage);;
		
		assertNotNull(SMIMEenvMessage);
		
		// now send received the MDN
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"Cerner.com"})); 
		
		DefaultTrustAnchorResolver resolver = (DefaultTrustAnchorResolver)agent.getTrustAnchors();
		
		CertificateResolver mockResolver = mock(CertificateResolver.class);
		DefaultTrustAnchorResolver newResolver = new DefaultTrustAnchorResolver(resolver.getOutgoingAnchors(), 
				mockResolver);
		
		
		agent.setTrustAnchorResolver(newResolver);
		
		IncomingMessage incomingMessage = agent.processIncoming(SMIMEenvMessage.getMessage());
		
		assertNotNull(incomingMessage);
		
	}
	
	@Test
	public void testIncomingDSN_incomingNotTrusted_outgoingTrusted_assertMDNMessageTrusted() throws Exception
	{
		OptionsManager.getInstance().setOptionsParameter(
				new OptionsParameter(OptionsParameter.USE_OUTGOING_POLICY_FOR_INCOMING_NOTIFICATIONS, "true"));
		
		// first create the encyrpted message
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"starugh-stateline.com"})); 
		String testMessage = TestUtils.readResource("DSNMessage.txt");
		
		final OutgoingMessage SMIMEenvMessage = agent.processOutgoing(testMessage);;
		
		assertNotNull(SMIMEenvMessage);
		
		// now send received the MDN
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"Cerner.com"})); 
		
		DefaultTrustAnchorResolver resolver = (DefaultTrustAnchorResolver)agent.getTrustAnchors();
		
		CertificateResolver mockResolver = mock(CertificateResolver.class);
		DefaultTrustAnchorResolver newResolver = new DefaultTrustAnchorResolver(resolver.getOutgoingAnchors(), 
				mockResolver);
		
		
		agent.setTrustAnchorResolver(newResolver);
		
		IncomingMessage incomingMessage = agent.processIncoming(SMIMEenvMessage.getMessage());
		
		assertNotNull(incomingMessage);
		
	}
	
	@Test
	public void testIncomingNormalMessage_incomingNotTrusted_outgoingTrusted_assertMessageNotTrusted() throws Exception
	{

		// first create the encyrpted message
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"cerner.com"})); 
		String testMessage = TestUtils.readResource("MultipartMimeMessage.txt");
		
		final OutgoingMessage SMIMEenvMessage = agent.processOutgoing(testMessage);;
		
		assertNotNull(SMIMEenvMessage);
		
		// now send received the MDN
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"starugh-stateline.com"})); 
		
		DefaultTrustAnchorResolver resolver = (DefaultTrustAnchorResolver)agent.getTrustAnchors();
		
		CertificateResolver mockResolver = mock(CertificateResolver.class);
		DefaultTrustAnchorResolver newResolver = new DefaultTrustAnchorResolver(resolver.getOutgoingAnchors(), 
				mockResolver);
				
		agent.setTrustAnchorResolver(newResolver);
		
		IncomingMessage incomingMessage = null;
		try 
		{
		   incomingMessage = agent.processIncoming(SMIMEenvMessage.getMessage());
		}
		catch(NHINDException e) 
		{
			assertEquals(TrustError.NoTrustedRecipients, e.getError());
		}
		assertNull(incomingMessage);
		
	}	
	
	@Test
	public void testIncomingMDN_incomingNotTrusted_outgoingTrusted_useIncomingSettingFalse_assertMDNMessageNotTrusted() throws Exception
	{

		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.USE_OUTGOING_POLICY_FOR_INCOMING_NOTIFICATIONS, "false"));
		
		// first create the encyrpted message
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"starugh-stateline.com"})); 
		String testMessage = TestUtils.readResource("MDNMessage.txt");
		
		final OutgoingMessage SMIMEenvMessage = agent.processOutgoing(testMessage);;
		
		assertNotNull(SMIMEenvMessage);
		
		// now send received the MDN
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"Cerner.com"})); 
		
		DefaultTrustAnchorResolver resolver = (DefaultTrustAnchorResolver)agent.getTrustAnchors();
		
		CertificateResolver mockResolver = mock(CertificateResolver.class);
		DefaultTrustAnchorResolver newResolver = new DefaultTrustAnchorResolver(resolver.getOutgoingAnchors(), 
				mockResolver);
		
		
		agent.setTrustAnchorResolver(newResolver);
		
		IncomingMessage incomingMessage = null;
		try 
		{
			incomingMessage = agent.processIncoming(SMIMEenvMessage.getMessage());
		}
		catch(NHINDException e) 
		{
			assertEquals(TrustError.NoTrustedRecipients, e.getError());
		}
		assertNull(incomingMessage);
		
	}	
	
	@Test
	public void testIncomingDSN_incomingNotTrusted_outgoingTrusted_useIncomingSettingFalse_assertMDNMessageNotTrusted() throws Exception
	{

		OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.USE_OUTGOING_POLICY_FOR_INCOMING_NOTIFICATIONS, "false"));
		
		// first create the encyrpted message
		DefaultNHINDAgent agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"starugh-stateline.com"})); 
		String testMessage = TestUtils.readResource("DSNMessage.txt");
		
		final OutgoingMessage SMIMEenvMessage = agent.processOutgoing(testMessage);;
		
		assertNotNull(SMIMEenvMessage);
		
		// now send received the MDN
		agent = TestUtils.getStockAgent(Arrays.asList(new String[]{"Cerner.com"})); 
		
		DefaultTrustAnchorResolver resolver = (DefaultTrustAnchorResolver)agent.getTrustAnchors();
		
		CertificateResolver mockResolver = mock(CertificateResolver.class);
		DefaultTrustAnchorResolver newResolver = new DefaultTrustAnchorResolver(resolver.getOutgoingAnchors(), 
				mockResolver);
		
		
		agent.setTrustAnchorResolver(newResolver);
		
		IncomingMessage incomingMessage = null;
		try 
		{
			incomingMessage = agent.processIncoming(SMIMEenvMessage.getMessage());
		}
		catch(NHINDException e) 
		{
			assertEquals(TrustError.NoTrustedRecipients, e.getError());
		}
		assertNull(incomingMessage);
		
	}	
}

