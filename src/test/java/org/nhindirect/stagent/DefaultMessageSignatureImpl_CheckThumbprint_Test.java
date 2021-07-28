package org.nhindirect.stagent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.mail.smime.CMSProcessableBodyPartInbound;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.cryptography.CryptoAlgorithmsHelper;
import org.nhindirect.stagent.cryptography.DigestAlgorithm;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.mail.MimeEntity;
import org.nhindirect.stagent.parser.EntitySerializer;
import org.nhindirect.stagent.utils.BaseTestPlan;
import org.nhindirect.stagent.utils.TestUtils;

/**
 * Generated test case.
 * 
 * @author junit_generate
 */
public class DefaultMessageSignatureImpl_CheckThumbprint_Test
{
	abstract class TestPlan extends BaseTestPlan {
		
		@Override
		protected void setupMocks()
		{
			CryptoExtensions.registerJCEProviders();
		}
		
		@Override
		protected void performInner() throws Exception {
			DefaultMessageSignatureImpl impl = createMessageSignature();
			boolean checkThumbprint = impl
					.checkThumbprint(createMessageSender());
			doAssertions(checkThumbprint);
		}

		protected DefaultMessageSignatureImpl createMessageSignature() throws Exception {
			return new DefaultMessageSignatureImpl(createSignerInformation(),
					(boolean) false, theGetCertificates.iterator().next()) {
			};
		}

		protected NHINDAddress theCreateMessageSender;

		@SuppressWarnings("serial")
		protected NHINDAddress createMessageSender() throws Exception {
			theCreateMessageSender = new NHINDAddress("") {

				@Override
				public boolean hasCertificates() {
					hasCertificatesCalls++;
					return hasCertificates_Internal();
				}

				@Override 
				public Collection<X509Certificate> getCertificates(){
					  getCertificatesCalls++;
					  return getCertificates_Internal();
					}
				
			};
			return theCreateMessageSender;
		}
		
		protected Collection<X509Certificate> theGetCertificates;
		protected int getCertificatesCalls=0;
		protected Collection<X509Certificate> getCertificates_Internal(){
		  return theGetCertificates;
		}

		protected boolean theHasCertificates;
		protected int hasCertificatesCalls = 0;

		protected boolean hasCertificates_Internal() {
			theHasCertificates = false;
			return theHasCertificates;
		}

		protected SignerInformation createSignerInformation() throws Exception {
			X509CertificateEx internalCert = TestUtils.getInternalCert("user1");
			String testMessage = TestUtils
					.readResource("MultipartMimeMessage.txt");

			MimeMessage entity = EntitySerializer.Default
					.deserialize(testMessage);
			Message message = new Message(entity);

			MimeEntity entityToSig = message.extractEntityForSignature(true);

			byte[] messageBytes = EntitySerializer.Default
					.serializeToBytes(entityToSig); // Serialize message out as
													// ASCII encoded...

			MimeBodyPart partToSign = null;

			try {
				partToSign = new MimeBodyPart(new ByteArrayInputStream(
						messageBytes));
			} catch (Exception e) {
			}

			CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

			ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
			SMIMECapabilityVector caps = new SMIMECapabilityVector();

			caps.addCapability(SMIMECapability.dES_EDE3_CBC);
			caps.addCapability(SMIMECapability.rC2_CBC, 128);
			caps.addCapability(SMIMECapability.dES_CBC);
			caps.addCapability(new ASN1ObjectIdentifier("1.2.840.113549.1.7.1"));
			caps.addCapability(PKCSObjectIdentifiers.x509Certificate);
			signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

			List<X509Certificate> certList = new ArrayList<X509Certificate>();

			ContentSigner sha1Signer = new JcaContentSignerBuilder(DigestAlgorithm.SHA256WITHRSA.getAlgName())
					.setProvider(CryptoExtensions.getJCESensitiveProviderName()).build(((X509CertificateEx) internalCert).getPrivateKey());
			
			
			gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder()
			  .setProvider(CryptoExtensions.getJCEProviderName()).build())
              .build(sha1Signer, internalCert));
			
			certList.add(internalCert);
			
			theGetCertificates = certList;

			MimeMultipart retVal = new MimeMultipart();

	    	final JcaCertStore  certs = new JcaCertStore(certList);
	    	gen.addCertificates(certs);

	    	final CMSProcessableByteArray content = new CMSProcessableByteArray(messageBytes);
	    	
	    	final CMSSignedData signedData = gen.generate(content);

	        final String  header = "signed; protocol=\"application/pkcs7-signature\"; micalg=" + 
	        		CryptoAlgorithmsHelper.toDigestAlgorithmMicalg(DigestAlgorithm.SHA256WITHRSA);           
	        
	        final String encodedSig = StringUtils.toEncodedString(Base64.encodeBase64(signedData.getEncoded(), true), StandardCharsets.UTF_8);
	        
	        retVal = new MimeMultipart(header.toString());
	        
	        final MimeBodyPart sig = new MimeBodyPart(new InternetHeaders(), encodedSig.getBytes("ASCII"));
            sig.addHeader("Content-Type", "application/pkcs7-signature; name=smime.p7s; smime-type=signed-data");
            sig.addHeader("Content-Disposition", "attachment; filename=\"smime.p7s\"");
            sig.addHeader("Content-Description", "S/MIME Cryptographic Signature");
            sig.addHeader("Content-Transfer-Encoding", "base64");
	                    
            retVal.addBodyPart(partToSign);
            retVal.addBodyPart(sig);
            
			ByteArrayOutputStream oStream = new ByteArrayOutputStream();
			retVal.writeTo(oStream);
			oStream.flush();
			byte[] serialzedBytes = oStream.toByteArray();

			ByteArrayDataSource dataSource = new ByteArrayDataSource(
					serialzedBytes, retVal.getContentType());

			MimeMultipart verifyMM = new MimeMultipart(dataSource);

			CMSSignedData signeddata = new CMSSignedData(
					new CMSProcessableBodyPartInbound(partToSign), verifyMM
							.getBodyPart(1).getInputStream());
			SignerInformationStore signers = signeddata.getSignerInfos();
			Collection<SignerInformation> c = signers.getSigners();
			Iterator<SignerInformation> it = c.iterator();
			while (it.hasNext()) {
				SignerInformation signer = (SignerInformation) it.next();
				return signer;
			}
			return null;
		}

		protected void doAssertions(boolean checkThumbprint) throws Exception {
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMessageSenderDoesNotHaveCertificates_ReturnsFalse()
			throws Exception {
		new TestPlan() {

			protected boolean hasCertificates_Internal() {
				theHasCertificates = false;
				return theHasCertificates;
			}

			protected void doAssertions(boolean checkThumbprint)
					throws Exception {
				assertFalse(checkThumbprint);
			}
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThumbprintMatches_ReturnsTrue()
			throws Exception {
		new TestPlan() {

			protected boolean hasCertificates_Internal() {
				theHasCertificates = true;
				return theHasCertificates;
			}

			protected void doAssertions(boolean checkThumbprint)
					throws Exception {
				assertTrue(checkThumbprint);
			}
		}.perform();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThumbprintDoesNotMatch_ReturnsFalse()
			throws Exception {
		new TestPlan() {

			protected boolean hasCertificates_Internal() {
				theHasCertificates = true;
				return theHasCertificates;
			}
			
			protected DefaultMessageSignatureImpl createMessageSignature() throws Exception {
				return new DefaultMessageSignatureImpl(createSignerInformation(),
						(boolean) false, TestUtils.getInternalCert("bob")) {
				};
			}

			protected void doAssertions(boolean checkThumbprint)
					throws Exception {
				
				assertFalse(checkThumbprint);
			}
		}.perform();
	}
}