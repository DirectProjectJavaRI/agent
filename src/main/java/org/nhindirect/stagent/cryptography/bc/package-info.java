/**
 * Classes in this package override certain BouncyCaslte logic to resolve a bug in
 * which the SMIME symmetric key is not property decrypted when an HSM is involved.
 * Some HSM providers return back some type of representation that is not the private key
 * data when the getEncoded() method is called on a secret key object that is backed 
 * by a JCE/HSM implementation. 
 */
package org.nhindirect.stagent.cryptography.bc;