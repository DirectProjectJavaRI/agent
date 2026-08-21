---
title: Cryptographer
---

# Cryptographer

Cryptographers are responsible for encrypting, decrypting, signing, and validating signatures and support multiple message container constructs. They are defined by the [Cryptographer](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/cryptography/Cryptographer.html) interface.

```
package org.nhindirect.stagent.cryptography;

public interface Cryptographer 
{
	
    public MimeEntity encrypt(MimeMultipart entity, X509Certificate encryptingCertificate);
    
    public MimeEntity encrypt(MimeMultipart mmEntity, Collection<X509Certificate> encryptingCertificates);
    
    public MimeEntity encrypt(MimeEntity entity, X509Certificate encryptingCertificate);
    
    public MimeEntity encrypt(MimeEntity entity,  Collection<X509Certificate> encryptingCertificates);

    public MimeEntity decrypt(Message message, X509CertificateEx decryptingCertificate);
    
    public MimeEntity decrypt(MimeEntity encryptedEntity, X509CertificateEx decryptingCertificate);
    
    public MimeEntity decrypt(MimeEntity encryptedEntity, Collection<X509CertificateEx> decryptingCertificates);
    
    public SignedEntity sign(Message message, X509Certificate signingCertificate);
    
    public SignedEntity sign(Message message, Collection<X509Certificate> signingCertificates);
    
    public SignedEntity sign(MimeEntity entity, X509Certificate signingCertificate);

    public SignedEntity sign(MimeEntity entity, Collection<X509Certificate> signingCertificates);
    
    public void checkSignature(SignedEntity signedEntity, X509Certificate signerCertificate, Collection<X509Certificate> anchors) throws SignatureValidationException;
    
    public CMSSignedData deserializeSignatureEnvelope(SignedEntity entity);
    
    public CMSSignedData deserializeEnvelopedSignature(MimeEntity envelopeEntity);
    
    public CMSSignedData deserializeEnvelopedSignature(byte[] messageBytes);

}
```

Although the cryptography classes do not enforce the content type of the messages provided to each method, the security and trust agent uses the following series of cryptography tasks, in order:

**Outgoing Messages**

1. Sign Message
2. Encrypt Message

**Incoming Messages**

1. Decrypt Message
2. Validate Signature

**NOTE:** All of the following method descriptions assume the SMIME implementation.

## Encrypt

The Encrypt method and its variants accept a message that needs to be encrypted and the public certificate of each recipient. The method generates a random symmetric key to encrypt the message based on the implementation's configured encryption algorithm, such as AES128. The message is encrypted using the symmetric key, and the key is then encrypted using each public certificate. Each encrypted version of the symmetric key is stored in the final message and can only be decrypted by the corresponding recipient's private key.

All variants result in the same output: a MimeEntity that contains an SMIME-encrypted version of the original message. The raw representation is base64 encoded.

## Decrypt

The Decrypt method and its variants accept a message that needs to be decrypted and the private certificate of each recipient. The message must be a valid encrypted message using the cryptographer's expected format, such as SMIME. This method uses the recipients' private keys to extract the symmetric key from the message. Only one valid private key needs to be found in the collection of certificates to extract the symmetric key. Once the symmetric key is extracted, the message content is decrypted using the algorithm specified in the message.

All variants result in the same output: a MimeEntity that contains the decrypted version of the original message.

## Sign

The Sign method and its variants accept a message that needs to be signed and the private certificate(s) of the sender. The method generates a digest of the message based on the implementation's configured digest algorithm, such as SHA256. It also uses other attributes, such as the signer's public key(s), to produce a digital signature using the provided private key.

All variants result in the same output: a SignedEntity object that contains the original message and a signature block. The raw representation of the SignedEntity is a multipart MIME that contains two parts: the original message in the first part and a detached signature in the second part. The signature block is base64 encoded.

## CheckSignature

The CheckSignature method asserts the validity and integrity of a signed message using the sender's public certificate. The method validates that the signature in the message's signature block matches the provided public certificate and validates that the message has not been tampered with, using the message digest.

This method returns without incident if the signature can be validated. Otherwise, an exception is thrown.

**NOTE:** The default agent implementation does not use this method. Instead, it uses the MessageSignature interface to validate signatures on incoming messages during the trust validation stage.

## DeserializeSignatureEnvelope

The DeserializeSignatureEnvelope method and its variants are utility functions for extracting the CMS data embedded in the message signature block. The latest version of CMS is described by [RFC5652](http://tools.ietf.org/html/rfc5652).

## SMIMECryptographerImpl

The SMIMECryptographerImpl is an SMIME-specific implementation of the Cryptographer interface. Its internal algorithms use the SMIME specification to generate encrypted and signed message representations. It includes multiple constructors depending on the consumer's needs.

The class provides multiple constructors for various needs:

```
package org.nhindirect.stagent.cryptography;

public class SMIMECryptographerImpl implements Cryptographer
{
  public SMIMECryptographerImpl()
  
  public SMIMECryptographerImpl(EncryptionAlgorithm encryptionAlgorithm, DigestAlgorithm digestAlgorithm)
}
```

The following are the default values for each variable:

* encryptionAlgorithm - AES128
* digestAlgorithm - SHA256

**Strong Cryptographic Algorithms Enforcement**

Version 1.2 of the applicability statement removed the allowance of SHA-1 as a digest algorithm for outgoing messages; however, for backward compatibility, it allows for receiving messages that use lesser-strength digests. A similar statement can be made for encryption algorithms; however, no change has been made in version 1.2 to the strength of allowed encryption algorithms (AES128 is still the minimum allowed for sending).

For institutional or policy reasons, agent implementors may choose to enforce receiving messages only from other systems that comply with the higher-strength cryptographic algorithms. This policy can be set using the *ENFORCESTRONGDIGESTS* and *ENFORCESTRONGENCRYPTION* options parameters. For backward behavioral compatibility, these policies are set to false by default.

## PKCS11 Token Support

Some institutional and agency policies require a high level of protection for sensitive cryptographic material, specifically asymmetric private keys. A common protection method is to use a PKCS11 token, such as a hardware security module (HSM), where the private key is loaded into the token and the cryptographic operations that utilize the key are performed on the token instead of in the agent's process memory. The method for loading keys into the token is arbitrary, but generally, policies do not allow the private keys to be exposed in an unencrypted format when not present in the token. Once loaded into the token and "activated," the private key is only accessible to the token, meaning that cryptographic operations MUST be performed on the token.

The agent supports using PKCS11 tokens for cryptographic operations, and it has the ability to optimize the process. What does this mean? Both SMIME message decryption and signing use a two-phase process, where cryptographic operations are performed in both phases. For message signing, the first phase consists of computing a message digest, and the second phase digitally signs the digest using the sender's asymmetric private key. For message decryption, the first phase uses the recipient's asymmetric private key to decrypt a symmetric secret key, and this symmetric key is then used to decrypt the entire message in the second phase. In both scenarios, the private-key phase only operates on a small piece of information, whereas the other phase acts on the entire message. Because the small operations are the only ones that utilize the protected asymmetric private key, they are the only ones that really need to be performed on the token. Performing potentially large digest and message decryption operations on the token can quickly lead to performance bottlenecks, especially in deployments where the token is configured as a network appliance.

In Java, cryptographic operations are performed by JCE provider implementations, and these providers are configurable using various methods keyed by a provider name. PKCS11 tokens are mapped to JCE providers via the provider name, and routing cryptographic operations to a token is done by specifying the mapped JCE provider name in the appropriate cryptographic API call. The BouncyCastle libraries, utilized by the SMIMECryptographerImpl class, only allow signing and decryption operations to be routed to a single JCE provider. Because SMIME signing and decryption is actually a two-phase process, a different JCE provider could be used in each phase.

**Concurrent Programming**

All public methods of the SMIMECryptographerImpl are thread safe and can be called concurrently.