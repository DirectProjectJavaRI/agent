package org.nhindirect.stagent.mail.notifications;

import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeMultipart;

/**
 * Temporary version of the Apache James MimeMultipartReport class.
 * Currently, the latest version of James still derives from jakarta.mail classes
 * instead of Jarkata.  Latest Snapshots of James are using Jakarta, so this class will
 * be removed when releases are available.
 */
public class MimeMultipartReport extends MimeMultipart {

    /**
     * Default constructor
     */
    public MimeMultipartReport() {
        this("report");
    }

    /**
     * Constructs a MimeMultipartReport of the given subtype.
     * @param subtype
     */
    public MimeMultipartReport(String subtype) {
        super(subtype);
    }

    /**
     * Constructs a MimeMultipartReport from the passed DataSource.
     * @param aDataSource
     * @throws jakarta.mail.MessagingException
     */
    public MimeMultipartReport(DataSource aDataSource) throws MessagingException {
        super(aDataSource);
    }
    
    /**
     * Sets the type of report.
     * @param reportType
     * @throws MessagingException
     */
    public void setReportType(String reportType) throws MessagingException {
        ContentType contentType = new ContentType(getContentType());
        contentType.setParameter("report-type", reportType);
        setContentType(contentType);
    }
    
    /**
     * Sets the content type
     * @param aContentType
     */
    protected void setContentType(ContentType aContentType) {
        contentType = aContentType.toString();
    }

}

