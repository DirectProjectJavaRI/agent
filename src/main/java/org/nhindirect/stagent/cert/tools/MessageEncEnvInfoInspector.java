package org.nhindirect.stagent.cert.tools;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collection;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.KeyTransRecipientInformation;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.nhindirect.common.crypto.CryptoExtensions;

public class MessageEncEnvInfoInspector 
{
	
	static
	{
		CryptoExtensions.registerJCEProviders();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
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
        	
        	final SMIMEEnveloped env = new SMIMEEnveloped(new MimeMessage(null, inStream)); 
        	
        	String OID = env.getEncryptionAlgOID();
        	final RecipientInformationStore recipients = env.getRecipientInfos();
        	Collection<RecipientInformation> reps = recipients.getRecipients();
        	for (RecipientInformation repInfo : reps)
        	{
        		KeyTransRecipientInformation keyTransRepInfo = (KeyTransRecipientInformation)repInfo;
        		RecipientId recId = repInfo.getRID();
        		//BigInteger serialNum = recId.getSerialNumber();
        		
        		//System.out.println(recId.toString());
        		//System.out.println("HEX Serial: " + serialNum.toString(16));
        	}
        	
        	System.out.println("Encryption OID: " + OID);
        	
           	
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
}
