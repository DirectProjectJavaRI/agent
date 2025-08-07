package org.nhindirect.stagent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jakarta.mail.MessagingException;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.common.options.OptionsManagerUtils;
import org.nhindirect.common.options.OptionsParameter;
import org.nhindirect.stagent.cert.impl.KeyStoreCertificateStore;
import org.nhindirect.stagent.cryptography.SMIMECryptographerImpl;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.trust.DefaultTrustAnchorResolver;
import org.nhindirect.stagent.trust.TrustEnforcementStatus;
import org.nhindirect.stagent.trust.TrustError;
import org.nhindirect.stagent.trust.TrustException;
import org.nhindirect.stagent.trust.TrustModel;
import org.nhindirect.stagent.utils.BaseTestPlan;
import org.nhindirect.stagent.utils.SecondaryMimeMessage;


/**
 * Generated test case.
 * @author junit_generate
 */
public class DefaultNHINDAgent_ProcessIncomingMessage_Test 
{
	abstract class TestPlan extends BaseTestPlan {
		@Override
		protected void performInner() throws Exception {
			DefaultNHINDAgent impl = createNHINDAgent();
			impl.processMessage(createMessage());
			doAssertions();
		}

		protected void setupMocks() {
			OptionsManagerUtils.clearOptionsManagerInstance();
			
			OptionsManager.getInstance().setOptionsParameter(new OptionsParameter(OptionsParameter.REJECT_ON_ROUTING_TAMPER, "false"));
		}

		protected void tearDownMocks() {
			
			OptionsManagerUtils.clearOptionsManagerInstance();
		}
		
		protected DefaultNHINDAgent createNHINDAgent() throws Exception {
			/*return new NHINDAgent((Collection<String>) null,
					(ICertificateResolver) null, (ICertificateResolver) null,
					(ITrustAnchorResolver) null, createTrustModel(),
					(SMIMECryptographer) null) {
			};
			*/
			return new DefaultNHINDAgent("", new KeyStoreCertificateStore(),
					new KeyStoreCertificateStore(), new DefaultTrustAnchorResolver(), createTrustModel(), new SMIMECryptographerImpl()) {
				@Override
				protected void bindAddresses(IncomingMessage message) {
					bindAddressesCalls++;
					bindAddresses_Internal(message);
				}
				
				@Override  
				protected void decryptSignedContent(IncomingMessage message){
					  decryptSignedContentCalls++;
					  decryptSignedContent_Internal(message);
					}
				@Override
				protected Message unwrapMessage(Message message) {
					unwrapMessageCalls++;
					return unwrapMessage_Internal(message);
				}

			};
		}
		
		protected int bindAddressesCalls = 0;

		protected void bindAddresses_Internal(IncomingMessage message) {
		}
		
		protected int decryptSignedContentCalls=0;
		protected void decryptSignedContent_Internal(IncomingMessage message){
		}
		
		protected Message theUnwrapMessage;
		protected int unwrapMessageCalls = 0;

		protected Message unwrapMessage_Internal(Message message) {
			MimeMessage mimeMsg = new SecondaryMimeMessage();
			try {
				mimeMsg.setText("");
			theUnwrapMessage = new Message(mimeMsg);
			}
			catch (MessagingException e) {
				e.printStackTrace();
				fail("");
			}
			return theUnwrapMessage;
		}

		protected IncomingMessage theCreateMessage;

		protected IncomingMessage createMessage() throws Exception {
			MimeMessage mimeMsg = new SecondaryMimeMessage();
			mimeMsg.setText("");
			mimeMsg.setFrom(new InternetAddress("some"));
			mimeMsg.addRecipient(RecipientType.TO, new InternetAddress("some"));
			Message msg = new Message(mimeMsg);
			NHINDAddressCollection recipients = new NHINDAddressCollection();
			recipients.add(new NHINDAddress("some"));
			NHINDAddress sender = new NHINDAddress("some");
			theCreateMessage = new IncomingMessage(msg, recipients, sender) {
				
				@Override 
				public boolean hasDomainRecipients(){
					  hasDomainRecipientsCalls++;
					  return hasDomainRecipients_Internal();
					}

				@Override 
				protected void categorizeRecipients(TrustEnforcementStatus minTrustStatus){
					  categorizeRecipientsCalls++;
					  categorizeRecipients_Internal(minTrustStatus);
					}
				
			};
			return theCreateMessage;
		}
		
		protected int categorizeRecipientsCalls=0;
		protected void categorizeRecipients_Internal(TrustEnforcementStatus minTrustStatus){
		}
		
		protected boolean theHasDomainRecipients;
		protected int hasDomainRecipientsCalls=0;
		protected boolean hasDomainRecipients_Internal(){
		  theHasDomainRecipients=true;
		  return theHasDomainRecipients;
		}

		protected TrustModel theCreateTrustModel;
		protected int enforceCalls = 0;

		protected void enforce_Internal(IncomingMessage message) {
		}

		protected TrustModel createTrustModel() {
			theCreateTrustModel = new TrustModel() {
				@Override
				public void enforce(IncomingMessage message) {
					enforceCalls++;
					enforce_Internal(message);
				}
			};
			return theCreateTrustModel;
		}

		protected void doAssertions() throws Exception {
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageDoesNotHaveDomainRecips_ThrowsAgentException() throws Exception {
		new TestPlan() {
			
			protected boolean hasDomainRecipients_Internal(){
				  theHasDomainRecipients=false;
				  return theHasDomainRecipients;
				}
			
			protected void doAssertions() throws Exception {
				fail("");
			}

			@Override
			protected void assertException(Exception exception)
					throws Exception {
				assertTrue(exception instanceof AgentException);
				AgentException agentException = (AgentException) exception;
				assertTrue(agentException.getError().equals(AgentError.NoTrustedRecipients));
			}
			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectMessageParamIsPassedToBindAddressMethod() throws Exception {
		new TestPlan() {
			
			protected void bindAddresses_Internal(IncomingMessage message) {
				assertEquals(theCreateMessage, message);
			}
			
			protected void doAssertions() throws Exception {
				assertEquals(1, bindAddressesCalls);
			}
			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectMessageParamIsPassedToDecryptSignedContentMethod() throws Exception {
		new TestPlan() {
			
			protected void decryptSignedContent_Internal(IncomingMessage message){
				assertEquals(theCreateMessage, message);
			}
			
			protected void doAssertions() throws Exception {
				assertEquals(1, decryptSignedContentCalls);
			}
			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectMessageParamIsPassedToUnwrapMessageMethod() throws Exception {
		new TestPlan() {
			
			protected Message unwrapMessage_Internal(Message message) {
				assertEquals(theCreateMessage.getMessage(), message);
				return super.unwrapMessage_Internal(message);
			}
			
			protected void doAssertions() throws Exception {
				assertEquals(1, unwrapMessageCalls);
			}
			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageReturnedFromUnwrapMessageMethodIsSetInIncomingMessage() throws Exception {
		new TestPlan() {
			
			protected void doAssertions() throws Exception {
				assertEquals(1, unwrapMessageCalls);
				assertEquals(theUnwrapMessage, theCreateMessage.getMessage());
			}
			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectMessageParamIsPassedToEnforce() throws Exception {
		new TestPlan() {
			
			protected void enforce_Internal(IncomingMessage message) {
				assertEquals(theCreateMessage, message);
			}
			
			protected void doAssertions() throws Exception {
				assertEquals(1, enforceCalls);
			}			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageHasDomainRecipients_CategorizeRecipientsIsCalled() throws Exception {
		new TestPlan() {
			
			protected boolean hasDomainRecipients_Internal(){
				  theHasDomainRecipients=true;
				  return theHasDomainRecipients;
				}
			
			protected void doAssertions() throws Exception {
				assertTrue(hasDomainRecipientsCalls>0);
				assertEquals(1, categorizeRecipientsCalls);
			}			
			
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageDoesNotHaveDomainRecipientsAfterCategorizeRecipientsIsCalled_ThrowsException() throws Exception {
		new TestPlan() {
			
			protected boolean hasDomainRecipients_Internal(){
				if(hasDomainRecipientsCalls==3) {
					theHasDomainRecipients=false;
				}
				else {
				  theHasDomainRecipients=true;
				}
				  return theHasDomainRecipients;
				}
			
			protected void doAssertions() throws Exception {
				fail("");
			}
			
			@Override
			protected void assertException(Exception exception)
					throws Exception {
				assertEquals(3, hasDomainRecipientsCalls);
				assertTrue(exception instanceof TrustException);
				TrustException trustException = (TrustException) exception;
				assertTrue(trustException.getError().equals(TrustError.NoTrustedRecipients));
			}
			
		}.perform();
	}

}