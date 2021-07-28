package org.nhindirect.stagent.mail.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nhindirect.stagent.mail.Message;

@ExtendWith(MockitoExtension.class)
public class NotificationHelperTest 
{
	
	@Mock
	private Message mockMessage;
	
	private static final String DISPOSITION_NOTIFICATION_TO = "a@test.com";
	private static final String FROM = "b@test.com";

	@Test
	public void testNoDestination() throws Exception {
		String destination = NotificationHelper.getNotificationDestination(mockMessage);

		assertEquals("", destination);
	}

	@Test
	public void testDispositionNotification() throws Exception {
		when(mockMessage.getHeader(MDNStandard.Headers.DispositionNotificationTo))
				.thenReturn(new String[] { DISPOSITION_NOTIFICATION_TO });

		String destination = NotificationHelper.getNotificationDestination(mockMessage);

		assertEquals(DISPOSITION_NOTIFICATION_TO, destination);
	}

	@Test
	public void testDispositionNotificationTo() throws Exception {
		when(mockMessage.getHeader(MDNStandard.Headers.DispositionNotificationTo))
				.thenReturn(new String[] { DISPOSITION_NOTIFICATION_TO });

		String destination = NotificationHelper.getNotificationDestination(mockMessage);

		assertEquals(DISPOSITION_NOTIFICATION_TO, destination);
	}

	@Test
	public void testMultipleDispositionNotificationTo() throws Exception {
		String anotherAddress = "anotheraddress@test.com";
		when(mockMessage.getHeader(MDNStandard.Headers.DispositionNotificationTo))
				.thenReturn(new String[] { DISPOSITION_NOTIFICATION_TO, anotherAddress });

		String destination = NotificationHelper.getNotificationDestination(mockMessage);

		assertEquals(DISPOSITION_NOTIFICATION_TO + "," + anotherAddress, destination);
	}

	@Test
	public void testNullDispositionNotificationTo_FromAsFallback() throws Exception {
		when(mockMessage.getHeader(MDNStandard.Headers.DispositionNotificationTo)).thenReturn(null);
		when(mockMessage.getHeader(MDNStandard.Headers.From)).thenReturn(new String[] { FROM });

		String destination = NotificationHelper.getNotificationDestination(mockMessage);

		assertEquals(FROM, destination);
	}

	@Test
	public void testEmptyDispositionNotificationTo_FromAsFallback() throws Exception {
		when(mockMessage.getHeader(MDNStandard.Headers.DispositionNotificationTo)).thenReturn(new String[] {});
		when(mockMessage.getHeader(MDNStandard.Headers.From)).thenReturn(new String[] { FROM });

		String destination = NotificationHelper.getNotificationDestination(mockMessage);

		assertEquals(FROM, destination);
	}

	@Test
	public void testDispositionNotificationToDuplicatesAreRemoved() throws Exception {
		when(mockMessage.getHeader(MDNStandard.Headers.DispositionNotificationTo))
				.thenReturn(new String[] { DISPOSITION_NOTIFICATION_TO, DISPOSITION_NOTIFICATION_TO.toUpperCase() });

		String destination = NotificationHelper.getNotificationDestination(mockMessage);

		assertEquals(DISPOSITION_NOTIFICATION_TO, destination);
	}

	@Test
	public void testFromDuplicatesAreRemoved() throws Exception {
		when(mockMessage.getHeader(MDNStandard.Headers.DispositionNotificationTo)).thenReturn(null);
		when(mockMessage.getHeader(MDNStandard.Headers.From)).thenReturn(new String[] { FROM, FROM.toUpperCase() });

		String destination = NotificationHelper.getNotificationDestination(mockMessage);

		assertEquals(FROM, destination);
	}
}
