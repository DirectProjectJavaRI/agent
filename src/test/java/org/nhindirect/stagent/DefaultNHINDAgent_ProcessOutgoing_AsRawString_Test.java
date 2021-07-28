package org.nhindirect.stagent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.nhindirect.stagent.cert.impl.KeyStoreCertificateStore;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.trust.DefaultTrustAnchorResolver;
import org.nhindirect.stagent.utils.BaseTestPlan;
import org.nhindirect.stagent.utils.SecondaryMimeMessage;

/**
 * Generated test case.
 * @author junit_generate
 */
public class DefaultNHINDAgent_ProcessOutgoing_AsRawString_Test 
{
	abstract class TestPlan extends BaseTestPlan {
		@Override
		protected void performInner() throws Exception {
			DefaultNHINDAgent impl = createDefaultNHINDAgent();
			OutgoingMessage processOutgoing = impl
					.processOutgoing(createMessageText());
			doAssertions(processOutgoing);
		}

		protected DefaultNHINDAgent createDefaultNHINDAgent() throws Exception {
			return new DefaultNHINDAgent("", new KeyStoreCertificateStore(),
					new KeyStoreCertificateStore(), new DefaultTrustAnchorResolver()) {
				@Override
				protected Message wrapMessage(String messageText) {
					wrapMessageCalls++;
					return wrapMessage_Internal(messageText);
				}

				@Override 
				public OutgoingMessage processOutgoing(OutgoingMessage message){
					  processOutgoingCalls++;
					  return processOutgoing_Internal(message);
					}
			};
		}

		protected Message theWrapMessage;
		protected int wrapMessageCalls = 0;

		protected Message wrapMessage_Internal(String messageText) {
			try {
			MimeMessage mimeMsg = new SecondaryMimeMessage();
			mimeMsg.setText("");
			mimeMsg.setRecipients(RecipientType.TO, "some");
			mimeMsg.setSender(new InternetAddress("some"));
			Message msg = new Message(mimeMsg);
			theWrapMessage = msg;
			}
			catch(Exception e) {
				e.printStackTrace();
				fail("");
			}
			return theWrapMessage;
		}
		
		protected OutgoingMessage theProcessOutgoing;
		protected int processOutgoingCalls=0;
		protected OutgoingMessage processOutgoing_Internal(OutgoingMessage message){
		  theProcessOutgoing=message;
		  return theProcessOutgoing;
		}

		protected String theCreateMessageText;

		protected String createMessageText() throws Exception {
			theCreateMessageText = "createMessageText";
			return theCreateMessageText;
		}

		protected void doAssertions(OutgoingMessage processOutgoing)
				throws Exception {
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageTxtIsNull_ThrowsException() throws Exception {
		new TestPlan() {
			@Override
			protected String createMessageText() throws Exception {
				theCreateMessageText = null;
				return theCreateMessageText;
			}

			protected void doAssertions(OutgoingMessage processOutgoing)
					throws Exception {
				fail("");
			}

			@Override
			protected void assertException(Exception exception)
					throws Exception {
				assertNull(theCreateMessageText);
				assertTrue(exception instanceof IllegalArgumentException);
			}
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageTxtIsBlank_ThrowsException() throws Exception {
		new TestPlan() {
			@Override
			protected String createMessageText() throws Exception {
				theCreateMessageText = "";
				return theCreateMessageText;
			}

			protected void doAssertions(OutgoingMessage processOutgoing)
					throws Exception {
				fail("");
			}

			@Override
			protected void assertException(Exception exception)
					throws Exception {
				assertEquals(0, theCreateMessageText.length());
				assertTrue(exception instanceof IllegalArgumentException);
			}

		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectMessageTxtParamIsPassedToWrapMessage() throws Exception {
		new TestPlan() {
			
			protected Message wrapMessage_Internal(String messageText) {
				assertEquals(theCreateMessageText, messageText);
				return super.wrapMessage_Internal(messageText);
			}
			
			protected void doAssertions(OutgoingMessage processOutgoing)
					throws Exception {
				assertEquals(1, wrapMessageCalls);
			}

		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageTxtIsValid_ProcessOutgoingMethodIsCalled()
			throws Exception {
		new TestPlan() {

			protected void doAssertions(OutgoingMessage processOutgoing)
					throws Exception {
				assertEquals(1, processOutgoingCalls);
				assertNotNull(theProcessOutgoing);
				assertEquals(theProcessOutgoing, processOutgoing);
			}

		}.perform();
	}
}