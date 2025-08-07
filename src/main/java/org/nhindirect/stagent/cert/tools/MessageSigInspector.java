package org.nhindirect.stagent.cert.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.mail.smime.CMSProcessableBodyPart;
import org.bouncycastle.util.Store;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.policy.PolicyProcessException;
import org.nhindirect.stagent.cert.impl.CRLRevocationManager;

public class MessageSigInspector 
{
	static
	{
		CryptoExtensions.registerJCEProviders();
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String args[])
	{
		if (args.length == 0)
		{
            //printUsage();
            System.exit(-1);			
		}	
		
		String messgefile = null;
		
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
        
            // Options
            if (!arg.startsWith("-"))
            {
                System.err.println("Error: Unexpected argument [" + arg + "]\n");
                //printUsage();
                System.exit(-1);
            }
            
            else if (arg.equalsIgnoreCase("-msgFile"))
            {
                if (i == args.length - 1 || args[i + 1].startsWith("-"))
                {
                    System.err.println("Error: Missing message file");
                    System.exit(-1);
                }
         
                messgefile = args[++i];
                
            }
            else if (arg.equals("-help"))
            {
                //printUsage();
                System.exit(-1);
            }            
            else
            {
                System.err.println("Error: Unknown argument " + arg + "\n");
                //printUsage();
                System.exit(-1);
            }
            
        }
        
        if (messgefile == null)
        {
        	System.err.println("Error: missing message file\n");
        }
        
        InputStream inStream = null;
        try
        {
        	inStream = FileUtils.openInputStream(new File(messgefile));
        	
        	MimeMessage message = new MimeMessage(null, inStream);
        	
        	MimeMultipart mm = (MimeMultipart)message.getContent();
        	
    		//byte[] messageBytes = EntitySerializer.Default.serializeToBytes(mm.getBodyPart(0).getContent());
            //MimeBodyPart signedContent = null;
            
           	//signedContent = new MimeBodyPart(new ByteArrayInputStream(messageBytes));
        	
        	final CMSSignedData signed = new CMSSignedData(new CMSProcessableBodyPart(mm.getBodyPart(0)), mm.getBodyPart(1).getInputStream());
        	
           	
        	Store<X509CertificateHolder> certs = signed.getCertificates();
	        SignerInformationStore  signers = signed.getSignerInfos();
			Collection<SignerInformation> c = signers.getSigners();
	        
	        System.out.println("Found " + c.size() + " signers");
	        
	        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
	        int cnt = 1;
	        for (SignerInformation signer : c)
	        {
	        			
	            Collection<X509CertificateHolder> certCollection = certs.getMatches(signer.getSID());
	            if (certCollection != null && certCollection.size() > 0)
	            {
	            	
	            	X509Certificate cert = (X509Certificate)certFactory.generateCertificate(new ByteArrayInputStream(certCollection.iterator().next().getEncoded()));
	            	System.out.println("\r\nInfo for certificate " + cnt++);
	            	System.out.println("\tSubject: " + cert.getSubjectDN());
	            	System.out.println("\tSerial Number: " + cert.getSerialNumber().toString(16));
	            	
	            	
	            	FileUtils.writeByteArrayToFile(new File("SigCert.der"), cert.getEncoded());
	            	
	            	byte[]  bytes = cert.getExtensionValue("2.5.29.15");
	            	

	            	if (bytes != null)
	            	{
	            		
		            	final ASN1Object obj = getObject(bytes);
		            	final KeyUsage keyUsage = KeyUsage.getInstance(obj);
		            	
		        		final byte[] data = keyUsage.getBytes();
		        		
		        		final int intValue = (data.length == 1) ? data[0] & 0xff : (data[1] & 0xff) << 8 | (data[0] & 0xff);
		        		
		        		System.out.println("\tKey Usage: " + intValue);
	            	}
	            	else 
	            		System.out.println("\tKey Usage: NONE");
	            	
	            	if (CRLRevocationManager.getInstance().isRevoked(cert))
	            		System.out.println("\tHas been marked as revoked");
	            	
	            	//verify and get the digests
		        	final Attribute digAttr = signer.getSignedAttributes().get(CMSAttributes.messageDigest);
		        	final ASN1Encodable hashObj = digAttr.getAttrValues().getObjectAt(0);
		        	final byte[] signedDigest = ((ASN1OctetString)hashObj).getOctets();
		        	final String signedDigestHex = org.apache.commons.codec.binary.Hex.encodeHexString(signedDigest);
		        	System.out.println("\r\nSigned Message Digest: " + signedDigestHex);
	            	
		        	try
		        	{
		        		signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(CryptoExtensions.getJCEProviderName()).build(cert));
		        		System.out.println("Signature verified.");
		        	}
		        	catch (CMSException e)
		        	{
		        		System.out.println("Signature failed to verify.");
		        	}
		        	// should have the computed digest now
		        	final byte[] digest = signer.getContentDigest();
		        	final String digestHex = org.apache.commons.codec.binary.Hex.encodeHexString(digest);
		        	System.out.println("\r\nComputed Message Digest: " + digestHex);
		        	
	            }
	        }

        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	IOUtils.closeQuietly(inStream);
        }
	}
	
    @SuppressWarnings("deprecation")
	protected static ASN1Object getObject(byte[] ext)
            throws PolicyProcessException
    {
    	ASN1InputStream aIn = null;
        try
        {
            aIn = new ASN1InputStream(ext);
            ASN1OctetString octs = (ASN1OctetString)aIn.readObject();
        	IOUtils.closeQuietly(aIn);
            
            aIn = new ASN1InputStream(octs.getOctets());
            return aIn.readObject();
        }
        catch (Exception e)
        {
            throw new PolicyProcessException("Exception processing data ", e);
        }
        finally
        {
        	IOUtils.closeQuietly(aIn);
        }
    }	
}
