package org.nhindirect.stagent.mail.notifications;

import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeMessage;

import org.nhindirect.stagent.mail.MailStandard;
import org.nhindirect.stagent.mail.MimeEntity;

/**
 * Provides constants and utility functions for working with DSN messages
 * @author Greg Meyer
 * @since 8.0.0
 *
 */
public class DSNStandard extends MailStandard 
{
	public static final String  ReportType = "report-type";
	
	public static final String  ReportTypeValueNotification = "delivery-status"; 
	
	/**
	 * MIME types for DSN 
     * @author Greg Meyer
	 *
	 */
	public static class MediaType extends MailStandard.MediaType
	{
		/**
		 * Base MIME type for an MDN
		 */
	    public static final String ReportMessage = "multipart/report";
	
	    /**
	     * MIME type with qualifier for a disposition report.
	     */
	    public static final String DispositionReport = ReportMessage + "; report-type=delivery-status";
	
	    /**
	     * MIME type for the delivery-statusn body part of the multipart/report report
	     */
	    public static final String DeliveryStatus = "message/delivery-status";
	}
	
    /**
     * Tests the entity to see if it is a DSN.
     * <p>
     * DSN status is indicated by the appropriate main body Content-Type. The multipart body
     * will contain the actual delivery status.
     * @param entity The entity to test
     * @return true if the entity is a DSN. false otherwise
     * @see #isNotification(MimeEntity)
     */
    public static boolean isReport(MimeMessage entity)
    {
        if (entity == null)
        {
            return false;
        }

        ContentType contentType = getContentType(entity);

        return (contentType.match(DSNStandard.MediaType.ReportMessage) && 
        		contentType.getParameter(DSNStandard.ReportType) != null && 
        		contentType.getParameter(MDNStandard.ReportType).equalsIgnoreCase(DSNStandard.ReportTypeValueNotification));
    }  	
}
