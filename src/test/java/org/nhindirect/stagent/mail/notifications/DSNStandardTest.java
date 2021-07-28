package org.nhindirect.stagent.mail.notifications;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.InputStream;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class DSNStandardTest 
{
	@Test
	public void testIsReport_validDSN_assertTrue() throws Exception
	{
		try (InputStream in = FileUtils.openInputStream(new File("./src/test/resources/org/nhindirect/stagent/DSNMessage.txt"));)
		{
			final MimeMessage msg = new MimeMessage((Session)null, in);
			
			assertTrue(DSNStandard.isReport(msg));
		}
	}
	
	@Test
	public void testIsReport_MDNMessage_assertFalse() throws Exception
	{
		try (InputStream in = FileUtils.openInputStream(new File("./src/test/resources/org/nhindirect/stagent/MDNResponse.txt"));)
		{
			final MimeMessage msg = new MimeMessage((Session)null, in);
			
			assertFalse(DSNStandard.isReport(msg));
		}
	}
}
