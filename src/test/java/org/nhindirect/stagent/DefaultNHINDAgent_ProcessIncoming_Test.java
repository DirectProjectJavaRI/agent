package org.nhindirect.stagent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javax.mail.internet.MimeMessage;

import org.nhindirect.stagent.cert.impl.KeyStoreCertificateStore;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.trust.DefaultTrustAnchorResolver;
import org.nhindirect.stagent.utils.BaseTestPlan;
import org.nhindirect.stagent.utils.SecondaryMimeMessage;

/**
 * Generated test case.
 * 
 * @author junit_generate
 */
public class DefaultNHINDAgent_ProcessIncoming_Test 
{
	abstract class TestPlan extends BaseTestPlan {
		@Override
		protected void performInner() throws Exception {
			DefaultNHINDAgent impl = createNHINDAgent();
			IncomingMessage processIncoming = impl
					.processIncoming(createMessage());
			doAssertions(processIncoming);
		}

		protected DefaultNHINDAgent createNHINDAgent() throws Exception {
			return new DefaultNHINDAgent("", new KeyStoreCertificateStore(),
					new KeyStoreCertificateStore(), new DefaultTrustAnchorResolver()) {

						@Override 
						protected void processMessage(IncomingMessage message){
							  processMessageCalls++;
							  processMessage_Internal(message);
							}
				
			};
		}
		
		protected int processMessageCalls=0;
		protected void processMessage_Internal(IncomingMessage message){
		}

		protected IncomingMessage theCreateMessage;

		protected IncomingMessage createMessage() throws Exception {
			theCreateMessage = new IncomingMessage("");
			return theCreateMessage;
		}

		protected void doAssertions(IncomingMessage processIncoming)
				throws Exception {
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNullMessageParam_ThrowsException() throws Exception {
		new TestPlan() {
			protected IncomingMessage createMessage() throws Exception {
				theCreateMessage = null;
				return theCreateMessage;
			}

			protected void doAssertions(IncomingMessage processIncoming)
					throws Exception {
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
	
	class MessageParamIsNotNull extends TestPlan {
		protected IncomingMessage createMessage() throws Exception {
			MimeMessage mimeMsg = new SecondaryMimeMessage();
			mimeMsg.setText("");
			Message msg = new Message(mimeMsg);
			NHINDAddressCollection recipients = new NHINDAddressCollection();
			recipients.add(new NHINDAddress(""));
			NHINDAddress sender = new NHINDAddress("");
			theCreateMessage = new IncomingMessage(msg, recipients, sender) {
				
				@Override 
				protected void validate(){
					  validateCalls++;
					  validate_Internal();
					}
				
			};
			return theCreateMessage;
		}
		
		protected int validateCalls=0;
		protected void validate_Internal(){
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAgentIsSetInMessage() throws Exception {
		new MessageParamIsNotNull() {
			
			protected void doAssertions(IncomingMessage processIncoming)
					throws Exception {
				assertNotNull(processIncoming.getAgent());
			}

		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageIsValidated() throws Exception {
		new MessageParamIsNotNull() {
			
			protected void doAssertions(IncomingMessage processIncoming)
					throws Exception {
				assertEquals(1, validateCalls);
			}

		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectMessageParamIsPassedToProcessMessageMethod() throws Exception {
		new MessageParamIsNotNull() {
			
			@Override
			protected void processMessage_Internal(IncomingMessage message){
				assertEquals(theCreateMessage, message);
			}
			
			protected void doAssertions(IncomingMessage processIncoming)
					throws Exception {
				assertEquals(1, processMessageCalls);
			}

		}.perform();
	}
	
	class NHINDAgentEventListenerIsNotNull extends MessageParamIsNotNull {
		@Override
		protected DefaultNHINDAgent createNHINDAgent() throws Exception {
			DefaultNHINDAgent nhindAgent = new DefaultNHINDAgent("", new KeyStoreCertificateStore(),
					new KeyStoreCertificateStore(), new DefaultTrustAnchorResolver()) {

						@Override 
						protected void processMessage(IncomingMessage message){
							  processMessageCalls++;
							  processMessage_Internal(message);
							}
				
			};
			nhindAgent.setEventListener(new NHINDAgentEventListenerAdapter() {

				@Override 
				public void errorIncoming(IncomingMessage msg,Exception e){
					  errorIncomingCalls++;
					  errorIncoming_Internal(msg,e);
					}

				@Override 
				public void postProcessIncoming(IncomingMessage msg) throws NHINDException {
					  postProcessIncomingCalls++;
					  postProcessIncoming_Internal(msg);
					}

				@Override 
				public void preProcessIncoming(IncomingMessage msg) throws NHINDException {
					  preProcessIncomingCalls++;
					  preProcessIncoming_Internal(msg);
					}
				
			});
			return nhindAgent;
		}
		
		protected int errorIncomingCalls=0;
		protected void errorIncoming_Internal(IncomingMessage msg,Exception e){
		}
		
		protected int postProcessIncomingCalls=0;
		protected void postProcessIncoming_Internal(IncomingMessage msg) throws NHINDException {
		}
		
		protected int preProcessIncomingCalls=0;
		protected void preProcessIncoming_Internal(IncomingMessage msg) throws NHINDException {
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectMessageParamIsPassedToPreProcessIncomingMethod() throws Exception {
		new NHINDAgentEventListenerIsNotNull() {
			
			@Override
			protected void preProcessIncoming_Internal(IncomingMessage msg) throws NHINDException {
				assertEquals(theCreateMessage, msg);
			}
			
			protected void doAssertions(IncomingMessage processIncoming)
					throws Exception {
				assertEquals(1, preProcessIncomingCalls);
			}

		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCorrectMessageParamIsPassedToPostProcessIncomingMethod() throws Exception {
		new NHINDAgentEventListenerIsNotNull() {
			
			@Override
			protected void postProcessIncoming_Internal(IncomingMessage msg) throws NHINDException {
				assertEquals(theCreateMessage, msg);
			}
			
			protected void doAssertions(IncomingMessage processIncoming)
					throws Exception {
				assertEquals(1, postProcessIncomingCalls);
			}

		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPostProcessIncomingMethodThrowsException_ErrorIncomingIsCalled() throws Exception {
		new NHINDAgentEventListenerIsNotNull() {
			
			protected NHINDException nhindException;
			
			@Override
			protected void postProcessIncoming_Internal(IncomingMessage msg) throws NHINDException {
				nhindException = new NHINDException();
				throw nhindException;
			}
			
			@Override
			protected void errorIncoming_Internal(IncomingMessage msg,Exception e){
				assertEquals(theCreateMessage, msg);
				assertEquals(nhindException, e);
			}
			
			protected void doAssertions(IncomingMessage processIncoming)
					throws Exception {
				fail("");
			}

			@Override
			protected void assertException(Exception exception)
					throws Exception {
				assertEquals(1, errorIncomingCalls);
			}

		}.perform();
	}
}