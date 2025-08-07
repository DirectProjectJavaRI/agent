package org.nhindirect.stagent.cert.tools;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import jakarta.mail.internet.MimeBodyPart;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.nhindirect.common.crypto.CryptoExtensions;

public class MessagaeDecryptor
{
	static
	{
    	CryptoExtensions.registerJCEProviders();
	}
	
	public static void main(String[] args)
	{
		try
		{
			
			
			final KeyStore store = KeyStore.getInstance("pkcs12");
			store.load(FileUtils.openInputStream(new File("/Users/gm2552/Desktop/AnchorApproval/ATABValidationKeysAndAnchors/good.p12")), "".toCharArray());
			final String alias = store.aliases().nextElement();
			final PrivateKey entry = (PrivateKey)store.getKey(alias, "".toCharArray());
			final X509Certificate cert = (X509Certificate)store.getCertificate(alias);
			
			/*
			for (String arg :args)
			{
				if (arg )
			}
			*/
			
			//String encryptedStuff = FileUtils.readFileToString(new File("users/gm2552/Desktop/cry.eml"));
			
			InputStream inStream = FileUtils.openInputStream(new File("/users/gm2552/Desktop/dec.txt"));
			
			MimeBodyPart part = new MimeBodyPart(inStream);
			
			final SMIMEEnveloped m = new SMIMEEnveloped(part); 
			
			RecipientId recId =  new org.bouncycastle.cms.jcajce.JceKeyTransRecipientId(cert);
			
			
			final RecipientInformationStore recipients = m.getRecipientInfos();
			final RecipientInformation recipient = recipients.get(recId);	
			final JceKeyTransEnvelopedRecipient recip = new JceKeyTransEnvelopedRecipient(entry);
			
			
			recipient.getContent(recip);
			System.out.println("Alg OID: " + m.getEncryptionAlgOID());
			
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
