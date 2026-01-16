package org.nhindirect.stagent;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.nhindirect.stagent.cert.impl.KeyStoreCertificateStore;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.trust.DefaultTrustAnchorResolver;
import org.nhindirect.stagent.utils.BaseTestPlan;
import org.nhindirect.stagent.utils.SecondaryMimeMessage;

/**
 * Generated test case.
 * @author junit_generate
 */
public class DefaultNHINDAgent_ProcessOutgoing_RawStringAndAddresses_Test 
{
	abstract class TestPlan extends BaseTestPlan {
		@Override
		protected void performInner() throws Exception {
			DefaultNHINDAgent impl = createDefaultNHINDAgent();
			OutgoingMessage processOutgoing = impl.processOutgoing(
					createMessageText(), createRecipients(), createSender());
			doAssertions(processOutgoing);
		}

		protected DefaultNHINDAgent createDefaultNHINDAgent() throws Exception {
			return new DefaultNHINDAgent("", new KeyStoreCertificateStore(),
					new KeyStoreCertificateStore(), new DefaultTrustAnchorResolver()) {
				@Override
				protected void checkEnvelopeAddresses(
						NHINDAddressCollection recipients, NHINDAddress sender) {
					checkEnvelopeAddressesCalls++;
					checkEnvelopeAddresses_Internal(recipients, sender);
				}

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

		protected int checkEnvelopeAddressesCalls = 0;

		protected void checkEnvelopeAddresses_Internal(
				NHINDAddressCollection recipients, NHINDAddress sender) {
		}

		protected Message theWrapMessage;
		protected int wrapMessageCalls = 0;

		protected Message wrapMessage_Internal(String messageText) {
			try {
				MimeMessage mimeMsg = new SecondaryMimeMessage();
				mimeMsg.setText("");
				mimeMsg.setRecipients(RecipientType.TO, "some");
				mimeMsg.setSender(new InternetAddress());
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

		protected NHINDAddressCollection theCreateRecipients;

		protected NHINDAddressCollection createRecipients() throws Exception {
			theCreateRecipients = new NHINDAddressCollection();
			theCreateRecipients.add(new NHINDAddress(""));
			return theCreateRecipients;
		}

		protected NHINDAddress theCreateSender;

		protected NHINDAddress createSender() throws Exception {
			theCreateSender = new NHINDAddress("");
			return theCreateSender;
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
	public void testCorrectRecipientsParamIsPassedToCheckEnvelopeAddresses() throws Exception {
		new TestPlan() {
			protected void checkEnvelopeAddresses_Internal(
					NHINDAddressCollection recipients, NHINDAddress sender) {
				assertEquals(theCreateRecipients, recipients);
			}
			
			protected void doAssertions(OutgoingMessage processOutgoing)
				throws Exception {
				assertEquals(1, checkEnvelopeAddressesCalls);
			}
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectSenderParamIsPassedToCheckEnvelopeAddresses() throws Exception {
		new TestPlan() {
			protected void checkEnvelopeAddresses_Internal(
					NHINDAddressCollection recipients, NHINDAddress sender) {
				assertEquals(theCreateSender, sender);
			}
			
			protected void doAssertions(OutgoingMessage processOutgoing)
				throws Exception {
				assertEquals(1, checkEnvelopeAddressesCalls);
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
	public void testProcessOutgoingMethodIsCalled() throws Exception {
		new TestPlan() {
			
			protected void doAssertions(OutgoingMessage processOutgoing)
				throws Exception {
				assertEquals(1, processOutgoingCalls);
				assertEquals(theProcessOutgoing, processOutgoing);
			}
		}.perform();
	}
}