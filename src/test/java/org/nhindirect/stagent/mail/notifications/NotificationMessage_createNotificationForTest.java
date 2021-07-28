package org.nhindirect.stagent.mail.notifications;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import javax.mail.internet.MimeMessage;

import org.nhindirect.stagent.mail.MailStandard;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.utils.TestUtils;

public class NotificationMessage_createNotificationForTest
{
	@Test
	public void testCreateNotificationFor_processedMDN_assertSubjectHasCorrectPrefix() throws Exception
	{
		final String testMessage = TestUtils.readResource("MultipartMimeMessage.txt");
		
		final MimeMessage msg = new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII")));
		
		final Notification noti = new Notification(NotificationType.Processed);
		final NotificationMessage notiMsg = NotificationMessage.createNotificationFor(new Message(msg), noti);
	
		
		assertTrue(notiMsg.getHeader(MailStandard.Headers.Subject, ",").startsWith("Processed"));
	}
	
	@Test
	public void testCreateNotificationFor_dispatchedMDN_assertSubjectHasCorrectPrefix() throws Exception
	{
		final String testMessage = TestUtils.readResource("MultipartMimeMessage.txt");
		
		final MimeMessage msg = new MimeMessage(null, new ByteArrayInputStream(testMessage.getBytes("ASCII")));
		
		final Notification noti = new Notification(NotificationType.Dispatched);
		final NotificationMessage notiMsg = NotificationMessage.createNotificationFor(new Message(msg), noti);
		
		assertTrue(notiMsg.getHeader(MailStandard.Headers.Subject, ",").startsWith("Dispatched"));
	}
	
	
	
}
