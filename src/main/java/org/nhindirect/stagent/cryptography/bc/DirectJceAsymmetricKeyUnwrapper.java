package org.nhindirect.stagent.cryptography.bc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAESOAEPparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.GenericKey;
import org.bouncycastle.operator.OperatorException;
import org.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper;
import org.bouncycastle.operator.jcajce.JceGenericKey;

public class DirectJceAsymmetricKeyUnwrapper extends JceAsymmetricKeyUnwrapper
{
    protected Map<ASN1ObjectIdentifier, String> extraMappings = new HashMap<>();
    protected PrivateKey privateKey;


    public DirectJceAsymmetricKeyUnwrapper(AlgorithmIdentifier algorithmIdentifier, PrivateKey privKey)
    {
        super(algorithmIdentifier, privKey);
        this.privateKey = privKey;
    }

    @Override
    public JceAsymmetricKeyUnwrapper setAlgorithmMapping(ASN1ObjectIdentifier algorithm, String algorithmName)
    {
        super.setAlgorithmMapping(algorithm, algorithmName);

        extraMappings.put(algorithm, algorithmName);

        return this;
    }

    @Override
    public GenericKey generateUnwrappedKey(AlgorithmIdentifier encryptedKeyAlgorithm, byte[] encryptedKey)
            throws OperatorException
    {
        try
        {
            Key sKey = null;

            Class<?> parentClass = Class.forName("org.bouncycastle.operator.jcajce.JceAsymmetricKeyUnwrapper");
            Field helpField = parentClass.getDeclaredField("helper");
            helpField.setAccessible(true);

            Class<?> helperClazz = Class.forName("org.bouncycastle.operator.jcajce.OperatorHelper");

            Method cipherMeth = helperClazz.getDeclaredMethod("createAsymmetricWrapper", ASN1ObjectIdentifier.class, Map.class);
            cipherMeth.setAccessible(true);

            Cipher keyCipher = (Cipher)cipherMeth.invoke(helpField.get(this), this.getAlgorithmIdentifier().getAlgorithm(), extraMappings);


            // some providers do not support UNWRAP (this appears to be only for asymmetric algorithms)
            if (sKey == null)
            {

                if( this.getAlgorithmIdentifier().getAlgorithm().getId().equals(PKCSObjectIdentifiers.id_RSAES_OAEP.toString())) {
                    // Get the SHA Digest from the algorithm identifier
                    //AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(this.getAlgorithmIdentifier().getAlgorithm().toString());
                    //algorithmParameters.init(this.getAlgorithmIdentifier().getParameters().toASN1Primitive().getEncoded());
            		ASN1Encodable asn1Encodable = this.getAlgorithmIdentifier().getParameters();
            		
                    RSAESOAEPparams rsaesoaePparams = null;

                    if( asn1Encodable instanceof DERSequence) {
                        DERSequence derSequence = (DERSequence) asn1Encodable;
                        rsaesoaePparams = RSAESOAEPparams.getInstance(derSequence);
                    }
                    else if( asn1Encodable instanceof DLSequence) {
                        DLSequence dlSequence = (DLSequence) asn1Encodable;
                        rsaesoaePparams = RSAESOAEPparams.getInstance(dlSequence);
                    }
                    else
                    	rsaesoaePparams = RSAESOAEPparams.getInstance(asn1Encodable);
                	
                	if (rsaesoaePparams != null) {
                    	String hashAlg = getDigestName(rsaesoaePparams.getHashAlgorithm().getAlgorithm());
                        
                    	AlgorithmIdentifier mgfAlgId = rsaesoaePparams.getMaskGenAlgorithm();
                    	String maskFunction = oidToMGFName(mgfAlgId.getAlgorithm().getId());
                    	String mgfHashAlg = getDigestName(AlgorithmIdentifier.getInstance(mgfAlgId.getParameters()).getAlgorithm());

                        AlgorithmIdentifier pSourceAlgId = rsaesoaePparams.getPSourceAlgorithm();
                        ASN1Encodable pSrcValue = pSourceAlgId.getParameters();
                        byte[] pValue = ASN1OctetString.getInstance(pSrcValue).getOctets();
                        PSource.PSpecified pSource = (pValue == null) ? PSource.PSpecified.DEFAULT : new PSource.PSpecified(pValue);
                        
                    	
                        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                        		hashAlg,                        // main hash algorithm
                        		maskFunction,                   //mask function
                        		new MGF1ParameterSpec(mgfHashAlg),         
                        		pSource   
                            );                    
                    	
                        
                    	try {
                        	keyCipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
                        }
                        catch (Exception e) {
                        	// fall back to NOT supplying algorithm parameters in the event the underlying JCE provider does not support
                        	// parameters.  This should work in most cases as long as the encryption algorithm string contains a 
                        	// complete OAEP padding scheme such as RSA/None/OAEPWithSHA-256AndMGF1Padding.
                        	keyCipher.init(Cipher.DECRYPT_MODE, privateKey);
                        }
                	}
                	else
                		keyCipher.init(Cipher.DECRYPT_MODE, privateKey);
                		

                	
                } else {
                    keyCipher.init(Cipher.DECRYPT_MODE, privateKey);
                }

                sKey = new SecretKeySpec(keyCipher.doFinal(encryptedKey), encryptedKeyAlgorithm.getAlgorithm().getId());
            }

            return new JceGenericKey(encryptedKeyAlgorithm, sKey);
        }
        catch (Exception e)
        {
            throw new OperatorException("Decrypt failed: " + e.getMessage(), e);
        }

    }

    private String getDigestName(ASN1ObjectIdentifier oid) {
        if (oid.equals(OIWObjectIdentifiers.idSHA1)) return "SHA-1";
        if (oid.equals(NISTObjectIdentifiers.id_sha224)) return "SHA-224";
        if (oid.equals(NISTObjectIdentifiers.id_sha256)) return "SHA-256";
        if (oid.equals(NISTObjectIdentifiers.id_sha384)) return "SHA-384";
        if (oid.equals(NISTObjectIdentifiers.id_sha512)) return "SHA-512";
        throw new IllegalArgumentException("Unknown digest OID: " + oid);
    }
    
    private String oidToMGFName(String oid) {
        if (oid.equals(PKCSObjectIdentifiers.id_mgf1.getId())) {
            return "MGF1";    // JCE name
        }
        // if we never need to support additional mask generation functions, then extend the mapping above
        throw new IllegalArgumentException("Unsupported MGF: " + oid);
    }

}