/* 
Copyright (c) 2010, NHIN Direct Project
All rights reserved.

Authors:
   Umesh Madan     umeshma@microsoft.com
   Greg Meyer      gm2552@cerner.com
 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer 
in the documentation and/or other materials provided with the distribution.  Neither the name of the The NHIN Direct Project (nhindirect.org). 
nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.nhindirect.stagent.cryptography;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.ParseException;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.RecipientId;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.mail.smime.CMSProcessableBodyPart;
import org.bouncycastle.mail.smime.SMIMEEnveloped;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.nhindirect.common.crypto.CryptoExtensions;
import org.nhindirect.common.options.OptionsManager;
import org.nhindirect.common.options.OptionsParameter;
import org.nhindirect.stagent.NHINDException;
import org.nhindirect.stagent.SignatureValidationException;
import org.nhindirect.stagent.cert.X509CertificateEx;
import org.nhindirect.stagent.cryptography.bc.DirectJceKeyTransEnvelopedRecipient;
import org.nhindirect.stagent.mail.Message;
import org.nhindirect.stagent.mail.MimeEntity;
import org.nhindirect.stagent.mail.MimeError;
import org.nhindirect.stagent.mail.MimeException;
import org.nhindirect.stagent.mail.MimeStandard;
import org.nhindirect.stagent.parser.EntitySerializer;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;

/**
 * Executes the cryptography operations.  This include encryption, decryption, and signature generation. 
 * @author Greg Meyer
 * @author Umesh Madan
 *
 */
@Slf4j
public class SMIMECryptographerImpl implements Cryptographer
{	
	// Using the BC PKCSObjectIdentifiers type is not compatible across versions of the BC library
	public final static ASN1ObjectIdentifier x509CertificateObjectsIdent = new ASN1ObjectIdentifier("1.2.840.113549.1.9.22.1");
	
    public final static SMIMECryptographerImpl Default = new SMIMECryptographerImpl();
    
    protected EncryptionAlgorithm m_encryptionAlgorithm;
    protected DigestAlgorithm m_digestAlgorithm;
    protected boolean m_includeEpilogue = true;
    protected boolean enforceStrongEncryption;
    protected boolean enforceStrongDigests;
    
	private boolean m_logDigest = false;
	
    /**
     * Constructs a Cryptographer with a default EncryptionAlgorithm and DigestAlgorithm.
     */
    public SMIMECryptographerImpl()
    {
    	OptionsParameter param = OptionsManager.getInstance().getParameter(OptionsParameter.CRYPTOGRAHPER_SMIME_ENCRYPTION_ALGORITHM);
    	this.m_encryptionAlgorithm = (param == null) ? EncryptionAlgorithm.AES128 : EncryptionAlgorithm.fromString(param.getParamValue(), EncryptionAlgorithm.AES128);
    	
        param = OptionsManager.getInstance().getParameter(OptionsParameter.CRYPTOGRAHPER_SMIME_DIGEST_ALGORITHM);
        this.m_digestAlgorithm = (param == null) ? DigestAlgorithm.SHA256WITHRSA : DigestAlgorithm.fromString(param.getParamValue(), DigestAlgorithm.SHA256WITHRSA);
        
        if (!isAllowedDigestAlgorithm(m_digestAlgorithm.getOID()))
        	throw new IllegalArgumentException("The digest algorithm " + m_digestAlgorithm.getAlgName() + " is not allowed");
        
        param = OptionsManager.getInstance().getParameter(OptionsParameter.ENFORCE_STRONG_DIGESTS);
        this.enforceStrongDigests = OptionsParameter.getParamValueAsBoolean(param, true);
        
        param = OptionsManager.getInstance().getParameter(OptionsParameter.ENFORCE_STRONG_ENCRYPTION);
        this.enforceStrongEncryption = OptionsParameter.getParamValueAsBoolean(param, true);
		
		this.m_logDigest = OptionsParameter.getParamValueAsBoolean(param, false);
    }

    /**
     * Constructs a Cryptographer with an EncryptionAlgorithm and DigestAlgorithm.
     * @param encryptionAlgorithm The encryption algorithm used to encrypt the message.
     * @param digestAlgorithm The digest algorithm used to generate the message digest stored in the message signature.
     */    
    public SMIMECryptographerImpl(EncryptionAlgorithm encryptionAlgorithm, DigestAlgorithm digestAlgorithm)
    {
        this.m_encryptionAlgorithm = encryptionAlgorithm;
        this.m_digestAlgorithm = digestAlgorithm;
        
        OptionsParameter param = OptionsManager.getInstance().getParameter(OptionsParameter.ENFORCE_STRONG_DIGESTS);
        this.enforceStrongDigests = OptionsParameter.getParamValueAsBoolean(param, true);
        
        if (!isAllowedDigestAlgorithm(m_digestAlgorithm.getOID()))
        	throw new IllegalArgumentException("The digest algorith " + m_digestAlgorithm.getAlgName() + " is not allowed");
        
        param = OptionsManager.getInstance().getParameter(OptionsParameter.ENFORCE_STRONG_ENCRYPTION);
        this.enforceStrongEncryption = OptionsParameter.getParamValueAsBoolean(param, true);
		
		this.m_logDigest = OptionsParameter.getParamValueAsBoolean(param, false);
    }
    
    /**
     * Gets the EncryptionAlgorithm.
     * @return The EncryptionAlgorithm used to encrypt messages.
     */
    public EncryptionAlgorithm getEncryptionAlgorithm()
    {
        return this.m_encryptionAlgorithm;
    }
    
    /**
     * Sets the EncryptionAlgorithm
     * @param value The EncryptionAlgorithm used to encrypt messages.
     */
    public void setEncryptionAlgorithm(EncryptionAlgorithm value)
    {
        this.m_encryptionAlgorithm = value;
    }

    /**
     * Gets the DigestAlgorithm.
     * @return The DigestAlgorithm used generate the message digest stored in the message signature.
     */
    public DigestAlgorithm getDigestAlgorithm()
    {
    	return this.m_digestAlgorithm;
    }
    
    /**
     * Sets the DigestAlgorithm.
     * @param value The DigestAlgorithm used generate the message digest stored in the message signature.
     */   
    public void setDigestAlgorithm(DigestAlgorithm value)
    {
        if (!isAllowedDigestAlgorithm(value.getOID()))
        	throw new IllegalArgumentException("The digest algorithm " + m_digestAlgorithm.getAlgName() + " is not allowed");
    	
        this.m_digestAlgorithm = value;
    }
    
	/**
     * Indicates if message digests will be logged when verifying messages.
     * @return True if the digests will be logged.  False otherwise. 
     */
    public boolean isLogDigests()
    {
    	return this.m_logDigest;
    }
	
    /**
     * Sets the option to enforce strong message digests.
     * @param value True if strong message digests are enforced.  False otherwise.
     */   
    public void setStrongDigestEnforced(Boolean value)
    {
        this.enforceStrongDigests = value;
    }
    
    /**
     * Indicate if strong message digests are enforced
     * @return True if strong message digests are enforced.  False otherwise.
     */
    public boolean isStrongDigestEnforced()
    {
    	return this.enforceStrongDigests;
    }
    
	/**
     * Sets if message digests will be looged.
     * @param m_logDigest True if the digests will be logged.  False otherwise.
     */
    public void setLogDigests(boolean m_logDigest)
    {
    	this.m_logDigest = m_logDigest;
    }
	
    /**
     * Sets the option to enforce strong message encryption
     * @param value True if strong encryption is enforced.  False otherwise.
     */   
    public void setStrongEncryptionEnforced(Boolean value)
    {
        this.enforceStrongEncryption = value;
    }
    
    /**
     * Indicate if strong message encryption is enforced
     * @return True if strong encryption is enforced.  False otherwise.
     */
    public boolean isStrongEncryptionEnforced()
    {
    	return this.enforceStrongEncryption;
    }
    
    /**
     * Indicates if the the Epilogue part of a multipart entity should be used to generate the message signature.
     * @return True if the the Epilogue part of a multipart entity should be used to generate the message signature.  False otherwise.
     */
    public boolean isIncludeMultipartEpilogueInSignature()
    {
        return this.m_includeEpilogue;
    }            
    
    /**
     * Sets if the the Epilogue part of a multipart entity should be used to generate the message signature.
     * @param value True if the the Epilogue part of a multipart entity should be used to generate the message signature.  False otherwise.
     */   
    public void setIncludeMultipartEpilogueInSignature(boolean value)
    {
        this.m_includeEpilogue = value;
    }
    
    /*
     * Encryption
     */
    
    /**
     * 
     * Encrypts a mulit part MIME entity using the provided certificate.
     * @param entity The entity that will be encrypted.
     * @param encryptingCertificate The public certificates that will be used to encrypt the message.
     * @return A MimeEntity containing the encrypted part.
     */
    public MimeEntity encrypt(MimeMultipart entity, X509Certificate encryptingCertificate)
    {
    	Collection<X509Certificate> certs = new ArrayList<X509Certificate>();
    	certs.add(encryptingCertificate);
    	
        return this.encrypt(entity, certs);
    }    
    
    /**
     * Encrypts a mulit part MIME entity using the provided certificates.
     * @param entity The entity that will be encrypted.
     * @param encryptingCertificates The public certificates that will be used to encrypt the message.
     * @return A MimeEntity containing the encrypted part.
     */
    @SuppressWarnings("deprecation")
	public MimeEntity encrypt(MimeMultipart mmEntity, Collection<X509Certificate> encryptingCertificates)
    {
    	MimeEntity entToEncrypt = null;
    	
    	ByteArrayOutputStream oStream = new ByteArrayOutputStream();
    	try
    	{
	    	mmEntity.writeTo(oStream);
	    	oStream.flush();
	    	InternetHeaders headers = new InternetHeaders();
	    	headers.addHeader(MimeStandard.ContentTypeHeader, mmEntity.getContentType());
	    	
	    	
	    	entToEncrypt = new MimeEntity(headers, oStream.toByteArray());
	    	IOUtils.closeQuietly(oStream);	
    	}    	
    	catch (Exception e)
    	{
    		throw new MimeException(MimeError.InvalidMimeEntity, e);
    	}
        
    	return this.encrypt(entToEncrypt, encryptingCertificates);
    }       
    
    /**
     * Encrypts an entity using the provided certificate.
     * @param entity The entity that will be encrypted.
     * @param encryptingCertificate The public certificates that will be used to encrypt the message.
     * @return A MimeEntity containing the encrypted part.
     */
    public MimeEntity encrypt(MimeEntity entity, X509Certificate encryptingCertificate)
    {
    	Collection<X509Certificate> certs = new ArrayList<X509Certificate>();
    	certs.add(encryptingCertificate);
    	
        return this.encrypt(entity, certs);
    }

    /** 
     * Encrypts an entity using the provided certificates.
     * @param entity The entity that will be encrypted.
     * @param encryptingCertificate The public certificates that will be used to encrypt the message.
     * @return A MimeEntity containing the encrypted part.
     */
    public MimeEntity encrypt(MimeEntity entity,  Collection<X509Certificate> encryptingCertificates)
    {
        if (entity == null)
        {
            throw new IllegalArgumentException();
        }
        	
        MimeBodyPart partToEncrypt = entity;                
        MimeBodyPart encryptedPart =  this.encrypt(partToEncrypt, encryptingCertificates);
        MimeEntity encryptedEntity = null;
        
        try
        {
        	byte[] encBytes = EntitySerializer.Default.serializeToBytes(encryptedPart);
        	ByteArrayInputStream inStream = new ByteArrayInputStream(EntitySerializer.Default.serializeToBytes(encryptedPart));
        	encryptedEntity = new MimeEntity(inStream);
        	
            if (log.isDebugEnabled())
            {	
            	writePostEncypt(encBytes);
            }        

            encryptedEntity.setHeader(MimeStandard.ContentTypeHeader, SMIMEStandard.EncryptedContentTypeHeaderValue);
            
        }
        catch (Exception e)
        {
        	throw new MimeException(MimeError.Unexpected, e);
        }

        return encryptedEntity;
    }

    private MimeBodyPart encrypt(MimeBodyPart bodyPart, Collection<X509Certificate> encryptingCertificates)
    {
        return this.createEncryptedEnvelope(bodyPart, encryptingCertificates);
    }
            
    private MimeBodyPart createEncryptedEnvelope(MimeBodyPart bodyPart, Collection<X509Certificate> encryptingCertificates)
    {
        if (bodyPart == null || encryptingCertificates == null || encryptingCertificates.size() == 0)
        {
            throw new IllegalArgumentException();
        }
        
        if (log.isDebugEnabled())
        {	
        	writePreEncypt(EntitySerializer.Default.serializeToBytes(bodyPart));
        }          
        
        final SMIMEEnvelopedGenerator gen = new SMIMEEnvelopedGenerator();

        
        MimeBodyPart retVal = null;
        
        try
        {
            for(X509Certificate cert : encryptingCertificates)
            {
            	// ensure the certificates key is allowed
            	if (isAllowedCertKey(cert))
            		gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(cert).setProvider(CryptoExtensions.getJCEProviderName()));
            }
            
        	final ASN1ObjectIdentifier encryAlgOID = new ASN1ObjectIdentifier(this.m_encryptionAlgorithm.getOID());
        	final OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(encryAlgOID).
        			setProvider(CryptoExtensions.getJCEProviderNameForTypeAndAlgorithm("Cipher", this.m_encryptionAlgorithm.getOID())).build();
        	
        	retVal =  gen.generate(bodyPart, encryptor);
        			
        			
        			//encryAlgOID, 
        			//CryptoExtensions.getJCEProviderNameForTypeAndAlgorithm("Cipher", encryAlgOID));
        }
        catch (Exception e)
        {
        	throw new MimeException(MimeError.Unexpected, e);
        }
        
        return retVal;
    }

    //-----------------------------------------------------
    //
    // Decryption
    //
    //-----------------------------------------------------

    /**
     * Decrypts a message with the provided certificates private key.
     * @param message The message that will be decrypted.
     * @param decryptingCertificate The certificate whose private key will be used to decrypt the message.
     * @return A MimeEntity containing the decrypted part.
     */    
    public MimeEntity decrypt(Message message, X509CertificateEx decryptingCertificate)
    {
        return this.decrypt(message.extractMimeEntity(), decryptingCertificate);
    }
    
    /**
     * Decrypts an entity with the provided certificate's private key.
     * @param encryptedEntity The entity that will be decrypted.
     * @param decryptingCertificate The certificate whose private key will be used to decrypt the message.
     * @return A MimeEntity containing the decrypted part.
     */  
    public MimeEntity decrypt(MimeEntity encryptedEntity, X509CertificateEx decryptingCertificate)
    {
        if (encryptedEntity == null || decryptingCertificate == null)
        {
            throw new IllegalArgumentException();
        }
        
        if (!decryptingCertificate.hasPrivateKey())
        {
            throw new IllegalArgumentException("Certificate has no private key");
        }
        											   
        encryptedEntity.verifyContentType(SMIMEStandard.EncryptedContentTypeHeaderValue);
        encryptedEntity.verifyTransferEncoding(MimeStandard.TransferEncodingBase64);
        
    	Collection<X509CertificateEx> certs = new ArrayList<X509CertificateEx>();
    		certs.add(decryptingCertificate);
    		
        MimeEntity retVal = this.decrypt(encryptedEntity, certs);
        
        //
        // And turn the decrypted bytes back into an entity
        //
        return retVal;
    }
    
    /**
     * Decrypts an entity with the provided certificates' private key.
     * @param encryptedEntity The entity that will be decrypted.
     * @param decryptingCertificate The certificates whose private keys will be used to decrypt the message.
     * @return A MimeEntity containing the decrypted part.
     */  
    public MimeEntity decrypt(MimeEntity encryptedEntity, Collection<X509CertificateEx> decryptingCertificates)
    {
        if (decryptingCertificates == null || decryptingCertificates.size() == 0)
        {
            throw new IllegalArgumentException();
        }

        MimeEntity retEntity = null;
        try
        {        	                	
            if (log.isDebugEnabled())
            {	
            	final byte[] encryptedContent = encryptedEntity.getContentAsBytes();
            	writePreDecrypt(encryptedContent);
            }   
            
            final SMIMEEnveloped m = new SMIMEEnveloped(encryptedEntity);            
            
            if (!this.isAllowedEncryptionAlgorithm(m.getEncryptionAlgOID()))
            	throw new NHINDException(MimeError.DisallowedEncryptionAlgorithm, "The encryption algorithm " + m.getEncryptionAlgOID() 
            			+ " is not allowed");
            
            for (X509CertificateEx decryptCert : decryptingCertificates)
            {   
            	// ensure they key strength/size is allowed
            	if (!this.isAllowedCertKey(decryptCert))
            		continue;
            	
	            final RecipientId recId = generateRecipientSelector(decryptCert);
		
		        final RecipientInformationStore recipients = m.getRecipientInfos();
		        final RecipientInformation recipient = recipients.get(recId);	
		        if (recipient == null)
		        	continue;
	
		        final JceKeyTransEnvelopedRecipient recip = new DirectJceKeyTransEnvelopedRecipient(decryptCert.getPrivateKey());
		        recip.setProvider(CryptoExtensions.getJCESensitiveProviderName());
		        recip.setContentProvider(CryptoExtensions.getJCEProviderName());
		        
		        final byte[] decryptedPayload = recipient.getContent(recip);
	            if (log.isDebugEnabled())
	            {	
	            	writePostDecrypt(decryptedPayload);
	            }   
		        
	            final ByteArrayInputStream inStream = new ByteArrayInputStream(decryptedPayload);
	            
		        retEntity = new MimeEntity(inStream);
		        break;
            }
        }
        catch (MessagingException e)
        {
        	throw new MimeException(MimeError.InvalidMimeEntity, e);
        }
        catch (Exception e)
        {
        	throw new MimeException(MimeError.Unexpected, e);
        }

        if (retEntity == null)
        {
        	throw new NHINDException(MimeError.Unexpected, "None of the the provided decryption certs were found in message's RecipientsInfo set.");
        }
        
        return retEntity;
    }

    /*
     * Construct an RecipientId.  Added private function to support multiple versions of BC libraries.
     */
    private RecipientId generateRecipientSelector(X509Certificate decryptCert)
    {
    	RecipientId retVal = null;
    	
    	try
    	{
    		retVal = new org.bouncycastle.cms.jcajce.JceKeyTransRecipientId(decryptCert);
    	}
    	catch (Throwable e)
    	{
    		if (log.isDebugEnabled())
    		{
    			StringBuilder builder = new StringBuilder("bcmail-jdk15-146 org.bouncycastle.cms.jcajce.JceKeyTransRecipientId class not found.");
    			builder.append("\r\n\tAttempt to fall back to bcmail-jdk15-140 org.bouncycastle.cms.RecipientId");
    			log.debug(builder.toString());
    		}
    	}

    	return retVal;

    }
    
    /**
     * Signs a message with the provided certificate.
     * @param message The message that will be signed.
     * @param signingCertificate The certificate used to sign the message.
     * @return A signed entity that consists of a multipart/signed entity containing the original entity and a message signature. 
     */    
    public SignedEntity sign(Message message, X509Certificate signingCertificate)
    {
        return this.sign(message.extractEntityForSignature(this.m_includeEpilogue), signingCertificate);
    }
    
      
    public SignedEntity sign(Message message, Collection<X509Certificate> signingCertificates)
    {
        return this.sign(message.extractEntityForSignature(this.m_includeEpilogue), signingCertificates);
    }    
    
    /**
     * Signs an entity with the provided certificate.
     * @param message The entity that will be signed.
     * @param signingCertificate The certificate used to sign the message.
     * @return A signed entity that consists of a multipart/signed entity containing the original entity and a message signature. 
     */  
    public SignedEntity sign(MimeEntity entity, X509Certificate signingCertificate)        
    {
    	Collection<X509Certificate> certs = new ArrayList<X509Certificate>();
    	certs.add(signingCertificate);
    	
        return this.sign(entity, certs);
    }
    
    /**
     * Signs an entity with the provided certificates.
     * @param message The entity that will be signed.
     * @param signingCertificates The certificates used to sign the message.
     * @return A signed entity that consists of a multipart/signed entity containing the original entity and a message signature. 
     */ 
    public SignedEntity sign(MimeEntity entity, Collection<X509Certificate> signingCertificates)
    {
        if (entity == null)
        {
            throw new IllegalArgumentException();
        }

        byte[] messageBytes = EntitySerializer.Default.serializeToBytes(entity);     // Serialize message out as ASCII encoded...
     
        MimeMultipart mm = this.createSignatureEntity(messageBytes, signingCertificates);
        SignedEntity retVal = null;
        
        try
        {
        
        	retVal = new SignedEntity(new ContentType(mm.getContentType()), mm);
        }
        catch (ParseException e)
        {
        	throw new MimeException(MimeError.InvalidHeader, e);
        }
        
        return retVal;
    }

    protected MimeMultipart createSignatureEntity(byte[] entity, Collection<X509Certificate> signingCertificates)
    {    	
    	MimeMultipart retVal = null;
    	try
    	{
	        final MimeBodyPart signedContent = new MimeBodyPart(new ByteArrayInputStream(entity));
	    		        
	    	final ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
	    	final SMIMECapabilityVector caps = new SMIMECapabilityVector();
	
	    	caps.addCapability(SMIMECapability.dES_EDE3_CBC);
	    	caps.addCapability(SMIMECapability.rC2_CBC, 128);
	    	caps.addCapability(SMIMECapability.dES_CBC);
	    	caps.addCapability(new ASN1ObjectIdentifier("1.2.840.113549.1.7.1"));	    	
	    	caps.addCapability(x509CertificateObjectsIdent);
	    	signedAttrs.add(new SMIMECapabilitiesAttribute(caps));  
	    	
	    	final List<X509Certificate>  certList = new ArrayList<X509Certificate>();
	    	CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
	    	for (X509Certificate signer : signingCertificates)
	    	{
	    		if (signer instanceof X509CertificateEx && isAllowedCertKey(signer))
	    		{	    			
	    			ContentSigner digestSigner = new JcaContentSignerBuilder(this.m_digestAlgorithm.getAlgName())
	    					.setProvider(CryptoExtensions.getJCESensitiveProviderName()).build(((X509CertificateEx) signer).getPrivateKey());
	    			
	    			
	    			gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder()
	    			  .setProvider(CryptoExtensions.getJCEProviderName()).build())
                      .build(digestSigner, signer));
	    			
	    			certList.add(signer);
	    		}
	    	}    	  	    		    	
	    	
	    	final JcaCertStore  certs = new JcaCertStore(certList);
	    	gen.addCertificates(certs);
	    	final CMSProcessableByteArray content = new CMSProcessableByteArray(entity);
	    	
	    	final CMSSignedData signedData = gen.generate(content);
	    	  	    	
	        final String  header = "signed; protocol=\"application/pkcs7-signature\"; micalg=" + 
	        		CryptoAlgorithmsHelper.toDigestAlgorithmMicalg(this.m_digestAlgorithm);           
	        
	        final String encodedSig = StringUtils.toEncodedString(Base64.encodeBase64(signedData.getEncoded(), true), StandardCharsets.UTF_8);
	        
	        retVal = new MimeMultipart(header.toString());
	        
	        final MimeBodyPart sig = new MimeBodyPart(new InternetHeaders(), encodedSig.getBytes("ASCII"));
            sig.addHeader("Content-Type", "application/pkcs7-signature; name=smime.p7s; smime-type=signed-data");
            sig.addHeader("Content-Disposition", "attachment; filename=\"smime.p7s\"");
            sig.addHeader("Content-Description", "S/MIME Cryptographic Signature");
            sig.addHeader("Content-Transfer-Encoding", "base64");
	                    
            retVal.addBodyPart(signedContent);
            retVal.addBodyPart(sig);

    	}   
    	catch (MessagingException e)
    	{
    		throw new MimeException(MimeError.InvalidMimeEntity, e);  		
    	}    	
    	catch (IOException e)
    	{
    		throw new SignatureException(SignatureError.InvalidMultipartSigned, e);  		
    	}   
    	catch (Exception e)
    	{
    		throw new NHINDException(MimeError.Unexpected, e);   		
    	} 	
    	return retVal;  
  	
    }
    
    /*
     * Construct an attribute table.  Added private function to support multiple versions of BC libraries.
     */
    public static AttributeTable createAttributeTable(ASN1EncodableVector signedAttrs)
    {
    	// Support for BC 146.... has a different constructor signature from 140
    	
    	AttributeTable retVal = null;
    	
    	
    	if (retVal == null)
    	{
        	try
        	{
        		/*
        		 * 146 version
        		 */
        		Constructor<AttributeTable> constr = AttributeTable.class.getConstructor(ASN1EncodableVector.class);
        		retVal = constr.newInstance(signedAttrs);
        	}
        	catch (Throwable t)
        	{
    			log.error("Attempt to use to bcmail-jdk15-146 DERObjectIdentifier(ASN1EncodableVector constructor failed.", t);
        	}
    	}
    	
    	return retVal;
    }

    //-----------------------------------------------------
    //
    // Signature Validation
    //
    //-----------------------------------------------------
    /**
     * Validates that a signed entity has a valid message and signature.  The signer's certificate is validated to ensure authenticity of the message.  Message
     * tampering is also checked with the message's digest and the signed digest in the message signature.
     * @param signedEntity The entity containing the original signed part and the message signature.
     * @param signerCertificate The certificate used to sign the message.
     * @param anchors A collection of certificate anchors used to determine if the certificates used in the signature can be validated as trusted certificates.
     */
    public void checkSignature(SignedEntity signedEntity, X509Certificate signerCertificate, Collection<X509Certificate> anchors) throws SignatureValidationException
    {
    	if (!isAllowedCertKey(signerCertificate))
    		throw new SignatureValidationException("Signing certificate key size/strength is not allowed");
    	
    	CMSSignedData signatureEnvelope = deserializeSignatureEnvelope(signedEntity);
    	    	
        SignerInformation logSigInfo = null;
    	try
    	{
	    	// there may be multiple signatures in the signed part... iterate through all the signing certificates until one
    		// is verified with the signerCertificate
    		for (SignerInformation sigInfo : (Collection<SignerInformation>)signatureEnvelope.getSignerInfos().getSigners())	 
    		{
    			logSigInfo = sigInfo;
    			// make sure the sender did not send the message with an explicitly disallowed digest algorithm
    			// such as MD5
    			
    			if (!isAllowedDigestAlgorithm(sigInfo.getDigestAlgOID()))
    				throw new SignatureValidationException("Digest algorithm " + sigInfo.getDigestAlgOID() + " is not allowed.");
    				
	    		if (sigInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(CryptoExtensions.getJCEProviderName()).build(signerCertificate)))
	    		{
	    			
	    			return; // verified... return
	    		}
	    			
    		}
	    	// at this point the signerCertificate cannot be verified with one of the signing certificates....
	    	throw new SignatureValidationException("Signature validation failure.");
    	}
    	catch (SignatureValidationException sve)
    	{
    		throw sve;
    	}
    	catch (Exception e)
    	{
    		throw new SignatureValidationException("Signature validation failure.", e);
    	}
    	finally
    	{
    		logDigests(logSigInfo);
    	}
    }                        
 	
    /**
     * Determines if a specific digest algorithm is allowed by policy and in conformance with the applicability statement.
     * This check can be turned off with the ENFORCE_STRONG_DIGESTS options parameter.  
     * As of ANSI/DS 2019-01-100-2021 approved on May 13, 2021, certain digests are explicitly disallowed.
     * The ENFORCE_STRONG_DIGESTS options parameter is only relevant of optional or digests listed as SHOULD NOT
     * be used.  This parameter DOES NOT apply to explicitly disallowed algorithms.
     * @return
     */
    protected boolean isAllowedDigestAlgorithm(String digestOID)
    {

    	/*
    	 * Dis-allow MD5 explicitly and SHA1
    	 * may include other algorithms in further implementations
    	 */
    	return (digestOID.equalsIgnoreCase(DigestAlgorithm.MD5.getOID()) || digestOID.equalsIgnoreCase(DigestAlgorithm.SHA1.getOID()) ||
    			digestOID.equalsIgnoreCase(DigestAlgorithm.SHA1WITHRSA.getOID())) ? false: true;
    	

    }
    
    /**
     * Determines if a specific encryption algorithm is allowed by policy and in conformance with the applicability statement.
     * This check can be turned off with the ENFORCE_STRONG_ENCRYPTION options parameter.
     * @return
     */
    protected boolean isAllowedEncryptionAlgorithm(String encryptionOID)
    {
    	if (!this.isStrongEncryptionEnforced())
    		return true;
    	
    	/*
    	 * Dis-allow those algorithms explicitly outlined as SHOULD- if section 2.7 of RFC5751
    	 * may include other algorithms in further implementations
    	 */
    	return encryptionOID.equalsIgnoreCase(EncryptionAlgorithm.DES_EDE3_CBC.getOID()) ? false : true;
    }
    
    /**
     * Determines if a certificate has a key of acceptable size/strength.
     * RSA keys MUST be at 2048 bits in length.
     * @param cert The certificate to validate.
     * @return True if the certificate has a valid size/strength.
     */
    protected boolean isAllowedCertKey(X509Certificate cert)
    {
    	// Check if it's an RSA key
    	if (cert.getPublicKey().getAlgorithm().contains("RSA"))
    	{
    		final RSAPublicKey rsaPk = (RSAPublicKey) cert.getPublicKey();
    		return rsaPk.getModulus().bitLength() >= 2048;
    	}
    	
    	return true;
    }
    
	protected void logDigests(SignerInformation sigInfo)
    {
    	// it is assumed that the verify function has already been called, other wise the getContentDigest function
    	// will fail
    	if (this.m_logDigest && sigInfo != null)
    	{
    		try
    		{
		        //get the digests
		        final Attribute digAttr = sigInfo.getSignedAttributes().get(CMSAttributes.messageDigest);
		        final ASN1Encodable hashObj = digAttr.getAttrValues().getObjectAt(0);
		        final byte[] signedDigest = ((ASN1OctetString)hashObj).getOctets();
		        final String signedDigestHex = org.apache.commons.codec.binary.Hex.encodeHexString(signedDigest);
		        
		        log.info("Signed Message Digest: {}",  signedDigestHex);
		           
		        // should have the computed digest now
		        final byte[] digest = sigInfo.getContentDigest();
		        final String digestHex = org.apache.commons.codec.binary.Hex.encodeHexString(digest);
		        log.info("Computed Message Digest: {}", digestHex);
    		}
    		catch (Throwable t)
    		{  /* no-op.... logging digests is a quiet operation */}
    	}
    }
	
	
    /**
     * Extracts the ASN1 encoded signature data from the signed entity.
     * @param entity The entity containing the original signed part and the message signature.
     * @return A CMSSignedData object that contains the ASN1 encoded signature data of the message.
     */
    public CMSSignedData deserializeSignatureEnvelope(SignedEntity entity)
    {

    	
    	if (entity == null)
        {
            throw new NHINDException();
        }

    	CMSSignedData signed = null;
    	
    	try
    	{
    		//signed = new SMIMESigned(entity.getMimeMultipart());
    		byte[] messageBytes = EntitySerializer.Default.serializeToBytes(entity.getContent());
            MimeBodyPart signedContent = null;
            
           	signedContent = new MimeBodyPart(new ByteArrayInputStream(messageBytes));
                     
           	signed = new CMSSignedData(new CMSProcessableBodyPart(signedContent), entity.getMimeMultipart().getBodyPart(1).getInputStream());
           	
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
        	throw new MimeException(MimeError.Unexpected, e);
    	}
    	
    	return signed;
    }

    
    public CMSSignedData deserializeEnvelopedSignature(MimeEntity envelopeEntity)
    {
        if (envelopeEntity == null)
        {
            throw new SignatureException(SignatureError.NullEntity);
        }

        if (!SMIMEStandard.isSignedEnvelope(envelopeEntity))
        {
            throw new SignatureException(SignatureError.NotSignatureEnvelope);
        }

        byte[] envelopeBytes = EntitySerializer.Default.serializeToBytes(envelopeEntity);

        return this.deserializeEnvelopedSignature(envelopeBytes);
    }

    public CMSSignedData deserializeEnvelopedSignature(byte[] messageBytes)
    {
    	CMSSignedData signed = null;
    	
    	try
    	{                     
           	signed = new CMSSignedData(messageBytes);           	
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
        	throw new MimeException(MimeError.Unexpected, e);
    	}
    	
    	return signed;
    }    
    
    
    @SuppressWarnings("deprecation")
	private void writePreEncypt(byte message[])
    {
    	String path = System.getProperty("user.dir") + "/tmp";
    	File tmpDir = new File(path);
    	
    	if (!tmpDir.exists())
    	{
    		if (!tmpDir.mkdir())
    			return;
    	}
    	
    	System.currentTimeMillis();
    	
    	File outFile = new File(path + "/preEncypt_" + System.currentTimeMillis() + ".eml");
    	

    	try
    	{
        	if (!outFile.exists())
        	{
        		if (!outFile.createNewFile())
        			return;
        	}
        	BufferedOutputStream oStream = new BufferedOutputStream(new FileOutputStream(outFile));
    		
    		oStream.write(message, 0, message.length);
    		oStream.flush();
    		IOUtils.closeQuietly(oStream);	
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    @SuppressWarnings("deprecation")
	private void writePostEncypt(byte message[])
    {
    	String path = System.getProperty("user.dir") + "/tmp";
    	File tmpDir = new File(path);
    	
    	if (!tmpDir.exists())
    	{
    		if (!tmpDir.mkdir())
    			return;
    	}
    	
    	System.currentTimeMillis();
    	
    	File outFile = new File(path + "/postEncypt_" + System.currentTimeMillis() + ".eml");
    	

    	try
    	{
        	if (!outFile.exists())
        	{
        		if (!outFile.createNewFile())
        			return;
        	}
        	BufferedOutputStream oStream = new BufferedOutputStream(new FileOutputStream(outFile));
    		
    		oStream.write(message, 0, message.length);
    		oStream.flush();
    		IOUtils.closeQuietly(oStream);			
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    
    @SuppressWarnings("deprecation")
	private void writePreDecrypt(byte message[])
    {
    	String path = System.getProperty("user.dir") + "/tmp";
    	File tmpDir = new File(path);
    	
    	if (!tmpDir.exists())
    	{
    		if (!tmpDir.mkdir())
    			return;
    	}
    	
    	System.currentTimeMillis();
    	
    	File outFile = new File(path + "/preDecrypt_" + System.currentTimeMillis() + ".eml");
    	

    	try
    	{
        	if (!outFile.exists())
        	{
        		if (!outFile.createNewFile())
        			return;
        	}
        	BufferedOutputStream oStream = new BufferedOutputStream(new FileOutputStream(outFile));
    		
    		oStream.write(message, 0, message.length);
    		oStream.flush();
    		IOUtils.closeQuietly(oStream);			
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    @SuppressWarnings("deprecation")
	private void writePostDecrypt(byte message[])
    {
    	String path = System.getProperty("user.dir") + "/tmp";
    	File tmpDir = new File(path);
    	
    	if (!tmpDir.exists())
    	{
    		if (!tmpDir.mkdir())
    			return;
    	}
    	
    	System.currentTimeMillis();
    	
    	File outFile = new File(path + "/postDecrypt_" + System.currentTimeMillis() + ".eml");
    	

    	try
    	{
        	if (!outFile.exists())
        	{
        		if (!outFile.createNewFile())
        			return;
        		
        	}
        	BufferedOutputStream oStream = new BufferedOutputStream(new FileOutputStream(outFile));
    		
    		oStream.write(message, 0, message.length);
    		oStream.flush();
    		IOUtils.closeQuietly(oStream);	
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}
