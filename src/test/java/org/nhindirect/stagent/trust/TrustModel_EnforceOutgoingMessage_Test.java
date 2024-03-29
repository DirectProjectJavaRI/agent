package org.nhindirect.stagent.trust;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import javax.mail.internet.MimeMessage;

import org.nhindirect.stagent.NHINDAddress;
import org.nhindirect.stagent.NHINDAddressCollection;
import org.nhindirect.stagent.OutgoingMessage;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.utils.BaseTestPlan;
import org.nhindirect.stagent.utils.SecondaryMimeMessage;

/**
 * Generated test case.
 * @author junit_generate
 */
public class TrustModel_EnforceOutgoingMessage_Test 
{
	abstract class TestPlan extends BaseTestPlan {
		@Override
		protected void performInner() throws Exception {
			TrustModel impl = createTrustModel();
			impl.enforce(createMessage());
			doAssertions();
		}

		protected TrustModel createTrustModel() throws Exception {
			return new TrustModel() {
				
				@Override
				protected Collection<X509Certificate> findTrustedCerts(
						Collection<X509Certificate> certs,
						Collection<X509Certificate> anchors) {
					findTrustedCertsCalls++;
					return findTrustedCerts_Internal(certs, anchors);
				}
			};
		}
		
		protected Collection<X509Certificate> theFindTrustedCerts;
		protected int findTrustedCertsCalls = 0;

		protected Collection<X509Certificate> findTrustedCerts_Internal(
				Collection<X509Certificate> certs,
				Collection<X509Certificate> anchors) {
			theFindTrustedCerts = new ArrayList<X509Certificate>();
			return theFindTrustedCerts;
		}

		protected OutgoingMessage theCreateMessage;

		protected OutgoingMessage createMessage() throws Exception {
			MimeMessage mimeMsg = new SecondaryMimeMessage();
			mimeMsg.setText("");
			Message msg = new Message(mimeMsg);
			NHINDAddressCollection recipients = new NHINDAddressCollection();
			recipients.add(new NHINDAddress(""));
			NHINDAddress sender = new NHINDAddress("");
			theCreateMessage = new OutgoingMessage(msg, recipients, sender) {
				
				@Override 
				public NHINDAddressCollection getRecipients(){
					  getRecipientsCalls++;
					  return getRecipients_Internal();
				}

				@Override 
				public NHINDAddress getSender(){
					  getSenderCalls++;
					  return getSender_Internal();
				}
			};
			return theCreateMessage;
		}
		
		protected NHINDAddressCollection theGetRecipients;
		protected int getRecipientsCalls=0;
		protected NHINDAddress recip;
		
		@SuppressWarnings("serial")
		protected NHINDAddressCollection getRecipients_Internal(){
			  theGetRecipients=new NHINDAddressCollection();
			  recip = new NHINDAddress("") {

				@Override 
				public boolean hasCertificates(){
					  hasCertificatesCalls++;
					  return hasCertificates_Internal();
				} 
			  };
			  theGetRecipients.add(recip);
			  return theGetRecipients;
		}
		
		protected boolean theHasCertificates;
		protected int hasCertificatesCalls=0;
		protected boolean hasCertificates_Internal(){
			  theHasCertificates=false;
			  return theHasCertificates;
		}
		
		protected NHINDAddress theGetSender;
		protected int getSenderCalls=0;
		
		@SuppressWarnings("serial")
		protected NHINDAddress getSender_Internal(){
		  theGetSender=new NHINDAddress("") {

			@Override 
			public Collection<X509Certificate> getTrustAnchors(){
				  getTrustAnchorsCalls++;
				  return getTrustAnchors_Internal();
			} 
		  };
		  return theGetSender;
		}
		
		protected Collection<X509Certificate> theGetTrustAnchors;
		protected int getTrustAnchorsCalls=0;
		protected Collection<X509Certificate> getTrustAnchors_Internal(){
		  theGetTrustAnchors=new ArrayList<X509Certificate>();
		  return theGetTrustAnchors;
		}

		protected void doAssertions() throws Exception {
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOutgoingMessageIsNull_ThrowsIllegalArgumentException() throws Exception {
		new TestPlan() {
			protected OutgoingMessage createMessage() throws Exception {
				theCreateMessage = null;
				return theCreateMessage;
			}

			protected void doAssertions() throws Exception {
				fail("");
			}

			@Override
			protected void assertException(Exception exception)
					throws Exception {
				assertNull(theCreateMessage);
				assertTrue(exception instanceof IllegalArgumentException);
			}			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void testRecipientHasCertificate_SetsTrustEnforcementStatusAsSuccess() throws Exception {
		new TestPlan() {
			
			@Override
			protected boolean hasCertificates_Internal(){
			  theHasCertificates=true;
			  return theHasCertificates;
			}

			@Override
			protected void doAssertions() throws Exception {
				assertEquals(TrustEnforcementStatus.Success, recip.getStatus());
			}			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void testRecipientDoesNotHaveCertificate_SetsTrustEnforcementStatusAsFailed() throws Exception {
		new TestPlan() {
			
			@Override
			protected boolean hasCertificates_Internal(){
			  theHasCertificates=false;
			  return theHasCertificates;
			}

			@Override
			protected void doAssertions() throws Exception {
				assertEquals(TrustEnforcementStatus.Failed, recip.getStatus());
			}			
			
		}.perform();
	}
}