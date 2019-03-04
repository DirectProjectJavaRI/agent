package org.nhindirect.stagent.cryptography.bc;

import java.security.PrivateKey;
import java.security.Provider;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import org.bouncycastle.operator.SymmetricKeyUnwrapper;
import org.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import org.bouncycastle.operator.jcajce.JceKTSKeyUnwrapper;
import org.bouncycastle.operator.jcajce.JceSymmetricKeyUnwrapper;

public class DirectProviderJcaJceExtHelper extends ProviderJcaJceHelper implements DirectJcaJceExtHelper
{

    public DirectProviderJcaJceExtHelper(Provider provider)
    {
        super(provider);
    }
    
    public JceAsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey)
    {
    	final JceAsymmetricKeyUnwrapper retVal = new DirectJceAsymmetricKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey);
    	retVal.setProvider(provider);
    	
    	/*
    	 * For a non-BC provider, we need to map the OAEP algorithm OID to a name.  Many HSMs do not recognized the algorithm OID and explicitly
    	 * need the name.  May need to get sophisticated in later versions to map names for specific HSMs. 
    	 */
    	if (provider != null && !StringUtils.isEmpty(provider.getName()) && !provider.getName().equalsIgnoreCase("BC"))
    	{
    		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/None/OAEPWithSHA1AndMGF1Padding");
    	}    	
    	
        return retVal;
    }
    
    public JceKTSKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey, byte[] partyUInfo, byte[] partyVInfo)
    {
        return new JceKTSKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey, partyUInfo, partyVInfo).setProvider(provider);
    }

    public SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, SecretKey keyEncryptionKey)
    {
        return new JceSymmetricKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey).setProvider(provider);
    }
}