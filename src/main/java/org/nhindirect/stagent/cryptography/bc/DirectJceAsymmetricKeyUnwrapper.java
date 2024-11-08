package org.nhindirect.stagent.cryptography.bc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DefaultAlgorithmNameFinder;
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
                        AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(this.getAlgorithmIdentifier().getAlgorithm().toString());
                        algorithmParameters.init(this.getAlgorithmIdentifier().getParameters().toASN1Primitive().getEncoded());
                        keyCipher.init(Cipher.DECRYPT_MODE, privateKey, algorithmParameters);
                    } else {
                        keyCipher.init(Cipher.DECRYPT_MODE, privateKey);
                    }
                    byte[] var1 = keyCipher.doFinal(encryptedKey);   // emm
                    sKey = new SecretKeySpec(keyCipher.doFinal(encryptedKey), encryptedKeyAlgorithm.getAlgorithm().getId());
                }

                return new JceGenericKey(encryptedKeyAlgorithm, sKey);
            }
            catch (Exception e)
            {
                throw new OperatorException("Decrypt failed: " + e.getMessage(), e);
            }

        } 
    

}
