package org.nhindirect.stagent.cryptography.bc;

import java.security.PrivateKey;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAESOAEPparams;
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
    	 * Use explicit names to indicate parameters for OAEP so we don't have to pass them later
    	 * use a parameter structure (some providers may not support setting parameters via a parameters
    	 * structure).
    	 */

    	if (PKCSObjectIdentifiers.id_RSAES_OAEP.equals(keyEncryptionAlgorithm.getAlgorithm()))
    	{
    		ASN1Encodable asn1Encodable = keyEncryptionAlgorithm.getParameters();
    		
            RSAESOAEPparams rsaesoaePparams = null;

            if( asn1Encodable instanceof DERSequence) {
                DERSequence derSequence = (DERSequence) asn1Encodable;
                rsaesoaePparams = RSAESOAEPparams.getInstance(derSequence);
            }
            if( asn1Encodable instanceof DLSequence) {
                DLSequence dlSequence = (DLSequence) asn1Encodable;
                rsaesoaePparams = RSAESOAEPparams.getInstance(dlSequence);
            }
            
        	if (!StringUtils.isEmpty(providerName) && !providerName.equalsIgnoreCase("BC"))
        	{
        		// special cases for different HSMs
        		if (providerName.equalsIgnoreCase("SAFENETPROTECTWRAPPER"))
					retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/ECB/OAEP");
				else
				{
					// Default use case for LUNA
	                if( rsaesoaePparams != null) {
	                	String algorithm = rsaesoaePparams.getHashAlgorithm().getAlgorithm().getId();
	                	if (algorithm.equals(NISTObjectIdentifiers.id_sha256.getId()))
	                		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/None/OAEPWithSHA-256AndMGF1Padding");
	                	else if (algorithm.equals(NISTObjectIdentifiers.id_sha384.getId()))
	                		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/None/OAEPWithSHA-384AndMGF1Padding");
	                	else if (algorithm.equals(NISTObjectIdentifiers.id_sha512.getId()))
	                		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/None/OAEPWithSHA-512AndMGF1Padding");
	                	else if (algorithm.equals(OIWObjectIdentifiers.idSHA1.getId()))
	                		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/None/OAEPWithSHA-1AndMGF1Padding");
	                }
	                else {
	                	retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/None/OAEPWithSHA1AndMGF1Padding");
	                }					
				}
        	}
        	else {

                if( rsaesoaePparams != null) {
                	String algorithm = rsaesoaePparams.getHashAlgorithm().getAlgorithm().getId();
                	if (algorithm.equals(NISTObjectIdentifiers.id_sha256.getId()))
                		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
                	else if (algorithm.equals(NISTObjectIdentifiers.id_sha384.getId()))
                		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/ECB/OAEPWithSHA-384AndMGF1Padding");
                	else if (algorithm.equals(NISTObjectIdentifiers.id_sha512.getId()))
                		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
                	else if (algorithm.equals(OIWObjectIdentifiers.idSHA1.getId()))
                		retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
                }
                else {
                	retVal.setAlgorithmMapping(PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
                }
        	}

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
