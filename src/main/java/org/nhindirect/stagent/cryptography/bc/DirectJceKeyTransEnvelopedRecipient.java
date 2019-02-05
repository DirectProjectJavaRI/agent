package org.nhindirect.stagent.cryptography.bc;

import java.io.InputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Provider;
import java.util.Iterator;

import javax.crypto.Cipher;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientOperator;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.operator.InputDecryptor;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import org.nhindirect.common.crypto.CryptoExtensions;

public class DirectJceKeyTransEnvelopedRecipient extends JceKeyTransEnvelopedRecipient
{
    protected DirectEnvelopedDataHelper helper;
    protected DirectEnvelopedDataHelper contentHelper = helper;
    protected PrivateKey recipientKey;
    
	public DirectJceKeyTransEnvelopedRecipient(PrivateKey recipientKey)
	{
		super(recipientKey);
		
		this.recipientKey = recipientKey;
		setProvider(CryptoExtensions.getJCEProviderName());
	}
	
	@Override
	public JceKeyTransRecipient setProvider(Provider provider)
	{
		this.helper = new DirectEnvelopedDataHelper(new DirectProviderJcaJceExtHelper(provider));
		this.contentHelper = helper;
		
		return this;
	}
	
	@Override
	public JceKeyTransRecipient setProvider(String providerName)
	{
		this.helper = new DirectEnvelopedDataHelper(new DirectNamedJcaJceExtHelper(providerName));
		this.contentHelper = helper;
		
		return this;
	}
	
	@Override
    public JceKeyTransRecipient setContentProvider(Provider provider)
    {
        this.contentHelper = createContentHelper(provider);

        return this;
    }
	
	
    public JceKeyTransRecipient setContentProvider(String providerName)
    {
        this.contentHelper = createContentHelper(providerName);

        return this;
    }
	
	@Override
    public RecipientOperator getRecipientOperator(AlgorithmIdentifier keyEncryptionAlgorithm, final AlgorithmIdentifier contentEncryptionAlgorithm, byte[] encryptedContentEncryptionKey)
            throws CMSException
        {
            Key secretKey = extractSecretKey(keyEncryptionAlgorithm, contentEncryptionAlgorithm, encryptedContentEncryptionKey);

            final Cipher dataCipher = contentHelper.createContentCipher(secretKey, contentEncryptionAlgorithm);

            return new RecipientOperator(new InputDecryptor()
            {
                public AlgorithmIdentifier getAlgorithmIdentifier()
                {
                    return contentEncryptionAlgorithm;
                }

                public InputStream getInputStream(InputStream dataIn)
                {
                    return new CipherInputStream(dataIn, dataCipher);
                }
            });
        }
	
    @SuppressWarnings("rawtypes")
	protected Key extractSecretKey(AlgorithmIdentifier keyEncryptionAlgorithm, AlgorithmIdentifier encryptedKeyAlgorithm, byte[] encryptedEncryptionKey)
            throws CMSException
    {

        JceAsymmetricKeyUnwrapper unwrapper = helper.createAsymmetricUnwrapper(keyEncryptionAlgorithm, recipientKey).setMustProduceEncodableUnwrappedKey(unwrappedKeyMustBeEncodable);

        if (!extraMappings.isEmpty())
        {
            for (Iterator it = extraMappings.keySet().iterator(); it.hasNext(); )
            {
                ASN1ObjectIdentifier algorithm = (ASN1ObjectIdentifier)it.next();

                unwrapper.setAlgorithmMapping(algorithm, (String)extraMappings.get(algorithm));
            }
        }

        try
        {
            Key key = helper.getJceKey(encryptedKeyAlgorithm.getAlgorithm(), unwrapper.generateUnwrappedKey(encryptedKeyAlgorithm, encryptedEncryptionKey));

            if (validateKeySize)
            {
                helper.keySizeCheck(encryptedKeyAlgorithm, key);
            }

            return key;
        }
        catch (OperatorException e)
        {
            throw new CMSException("exception unwrapping key: " + e.getMessage(), e);
        }

    }	
	
    static DirectEnvelopedDataHelper createContentHelper(Provider provider)
    {
        if (provider != null)
        {
            return new DirectEnvelopedDataHelper(new DirectProviderJcaJceExtHelper(provider));
        }
        else
        {
            return new DirectEnvelopedDataHelper(new DirectNamedJcaJceExtHelper(CryptoExtensions.getJCEProviderName()));
        }
    }	
    
    static DirectEnvelopedDataHelper createContentHelper(String providerName)
    {
        if (providerName != null)
        {
            return new DirectEnvelopedDataHelper(new DirectNamedJcaJceExtHelper(providerName));
        }
        else
        {
            return new DirectEnvelopedDataHelper(new DirectNamedJcaJceExtHelper(CryptoExtensions.getJCEProviderName()));
        }
    }
}
