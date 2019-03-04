package org.nhindirect.stagent.cryptography.bc;

import java.security.PrivateKey;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.jcajce.util.NamedJcaJceHelper;
import org.bouncycastle.operator.SymmetricKeyUnwrapper;
import org.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import org.bouncycastle.operator.jcajce.JceKTSKeyUnwrapper;
import org.bouncycastle.operator.jcajce.JceSymmetricKeyUnwrapper;

public class DirectNamedJcaJceExtHelper extends NamedJcaJceHelper implements DirectJcaJceExtHelper
{
    public DirectNamedJcaJceExtHelper(String providerName)
    {
        super(providerName);
    }

    public JceAsymmetricKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey)
    {
    	final JceAsymmetricKeyUnwrapper retVal = new DirectJceAsymmetricKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey);
    	retVal.setProvider(providerName);
    	
    	/*
    	 * For a non-BC provider, we need to map the OAEP algorithm OID to a name.  Many HSMs do not recognized the algorithm OID and explicitly
    	 * need the name.
    	 */
    	if (!StringUtils.isEmpty(providerName) && !providerName.equalsIgnoreCase("BC"))
    	{
    		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/ECB/OAEP");
    	}
    	
        return retVal;
    }
    
    public JceKTSKeyUnwrapper createAsymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, PrivateKey keyEncryptionKey, byte[] partyUInfo, byte[] partyVInfo)
    {
        return new JceKTSKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey, partyUInfo, partyVInfo).setProvider(providerName);
    }

    public SymmetricKeyUnwrapper createSymmetricUnwrapper(AlgorithmIdentifier keyEncryptionAlgorithm, SecretKey keyEncryptionKey)
    {
        return new JceSymmetricKeyUnwrapper(keyEncryptionAlgorithm, keyEncryptionKey).setProvider(providerName);
    }    
}
