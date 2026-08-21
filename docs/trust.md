---
title: Trust
---

# Trust

Arguably the most important aspect of the security and trust specification is the trust model. Security and encryption algorithms can ensure that a message is securely transported from one location to another without being compromised or tampered with, but what value is a message if you do not trust its contents? In theory, anyone can set up a HISP, create certificates, and claim to have some type of authoritative credentials. A HISP user may be able to irrefutably validate their identity according to their credentials (certificates), but how do you know you can trust the content of the message? The security and trust model allows a HISP to "filter" and accept messages only from HISPs that it deems trustworthy. Transitively, a HISP should only allow users to create destinations or "accounts" that it deems trustworthy. This leads into the subject of identity proofing, which is outside the scope of the trust model. However, as a rule of thumb, only HISPs that follow and can demonstrate that they abide by good certificate practices and identity proofing procedures should be trusted.

Trust is not an absolute indicator of the truth of a message's content; it is subjective, based only on how much a recipient wants to trust the sender. In the context of health care data, trust in content can influence life-changing decisions, and in extreme cases, life-or-death decisions, and needs to be dealt with accordingly.

The security and trust model provides a great deal of flexibility in determining trust between HISPs and even individual users. Because messages are signed using X509 standards, public key infrastructure (PKIX) can be used to "filter" messages based on entities called trust anchors. Every signing X509 certificate is created by a certificate authority, and a public certificate authority file can be used to validate the authenticity and issuer of an X509 certificate. In the simplest case, a certificate authority (CA) is a trust anchor. If a HISP or user trusts a particular trust anchor, then all certificates created by that anchor are considered trusted. PKIX allows a great amount of flexibility and granularity in terms of certificate validation with trust anchors. CAs can create child CAs or signing certificates, which can in turn create their own certificates; this process is called chaining. When validating trust, any certificate in the chain can be used as a trust anchor (a trust anchor is also referred to as the most trusted certificate in a certificate chain), but all certificates in the hierarchy between the certificate and the trust anchor must be present to validate trust. At the most granular level, a signing certificate itself may be used as its own trust anchor.

The agent library supports the trust model through the TrustModel class and the TrustAnchorResolver interface. The resolver is responsible for locating anchors for a particular destination. As with certificate resolvers, there are different implementation models based on how trust anchors are located and how they should be applied for a specific destination (organization level vs. individual user).

**NOTE:** Trust anchors should not be confused with CAs used between SMTP or HTTP clients and servers. They essentially serve the same purpose (validating trust between two entities), but they satisfy different use cases.

## TrustAnchorResolver

The TrustAnchorResolver interface specifies two methods for locating and applying trust anchors: one for incoming messages and one for outgoing messages.

```
package org.nhindirect.stagent.trust;

public interface TrustAnchorResolver 
{
	CertificateResolver getOutgoingAnchors();

	CertificateResolver getIncomingAnchors();
}
```

The return value of each method is simply a CertificateResolver (trust anchors are simply certificates), but the set of certificates returned by the certificate resolver is governed by a different set of rules. With a regular CertificateResolver, the return value of the getCertificates method is a collection of certificates that represent the destination address parameter. Trust anchors are different in that calling getCertificates returns the configured set of trust anchors that a particular sender trusts. Depending on the CertificateResolver implementation and configuration, the configured set may be the same for all senders in a given domain or may be different for each user. In some cases it may be the same for every sender regardless of domain.

In many cases, the CertificateResolver for outgoing and incoming messages returns the same set of trust anchors. However, there may be cases where a HISP or user trusts sending to particular HISPs and/or users, but trusts a subset or completely different set of HISPs and/or users when receiving messages.

## DefaultTrustAnchorResolver

The DefaultTrustAnchorResolver is the default implementation of the TrustAnchorResolver interface. It allows multiple configurations of trust anchors, ranging from a single set of trust anchors used for both incoming and outgoing messages to full-blown CertificateResolver implementations.

The first set of constructors takes a set of trust anchors and creates a UniformCertificateStore as the certificate resolver. Each variation that only takes one parameter uses the same store for both incoming and outgoing certificate stores.
```
public DefaultTrustAnchorResolver(Collection<X509Certificate> anchors) 

public DefaultTrustAnchorResolver(Collection<X509Certificate> outgoingAnchors, Collection<X509Certificate> incomingAnchors)

public DefaultTrustAnchorResolver(X509Store anchors)

public DefaultTrustAnchorResolver(X509Store outgoingAnchors, X509Store incomingAnchors)
```

The other set of constructors takes a specific CertificateResolver implementation. Each implementation is configured to its own specification and allows for very granular trust anchor resolution.

```
    public DefaultTrustAnchorResolver(CertificateResolver anchors)

    @Inject
    public DefaultTrustAnchorResolver(@OutgoingTrustAnchors CertificateResolver outgoingAnchors, 
    		@IncomingTrustAnchors CertificateResolver incomingAnchors)
```

As with the previous set, the first constructor uses the same resolver for both incoming and outgoing messages. The last constructor is used by Guice for dependency injection.

## UniformCertificateStore

The UniformCertificateStore is a very simple certificate resolver for trust anchors. It is initialized from either a set of certificates or an [X509Store](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/cert/X509Store.html). In the latter case, the UniformCertificateStore initializes itself by calling getAllCertificates on the X509 store.

The UniformCertificateStore satisfies the use case where every address owned by the agent or consumer code uses the same set of trust anchors. This is typically used in a test environment or a single-domain HISP that does not support separate trust per user.

## TrustAnchorCertificateStore

The TrustAnchorCertificateStore is more sophisticated than the UniformCertificateStore in that it supports unique sets of trust anchors per domain. When a consumer calls getCertificates, the store uses the domain information from the address and looks up the trust anchors for that particular domain.

The TrustAnchorCertificateStore has one constructor.

```
public TrustAnchorCertificateStore(Map<String, Collection<X509Certificate>> certs)
```

The constructor takes a map of strings which equates to a list of domain names. Each domain name maps to a collection of trust anchors for that particular domain. **NOTE:** The domain name is case-insensitive, so looking up example.com and EXAMPLE.com will result in the same set of trust anchors.

## Binding Addresses to Trust Anchors

Part of the agent's logic is to resolve trust anchors for recipients of an incoming message and resolve trust anchors for the sender of an outgoing message. The agent places every message into a MessageEnvelope container. The MessageEnvelope interface and its default implementation parse and store the sender and recipient addresses into NHINDAddress structures. The NHINDAddress structure contains the method setTrustAnchors, which takes a collection of X509 certificates that should be used as trust anchors for that address. Typically, for incoming messages, the agent looks up the trust anchors for each domain recipient and sets the trust anchors for each recipient. For outgoing messages, the agent looks up the trust anchors for the sender and sets the trust anchors for that sender. The TrustModel class expects that the trust anchors will be bound to each address appropriately (for incoming and outgoing messages) before calling methods on it.

## TrustModel

The [TrustModel](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/trust/TrustModel.html) class enforces the trust portion of the security and trust specification. Although the TrustModel exposes two constructors, it is only really necessary to use the default empty constructor unless you need to subclass the [TrustChainValidator](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/trust/TrustChainValidator.html) class.

The trust model supports different levels of trust called [TrustEnforcementStatus](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/trust/TrustEnforcementStatus.html). During the enforcement phase, each address is flagged with a status indicating whether the address trusts the incoming or outgoing message. It is up to the agent or client code to assert what level of trust status it will allow through the security and trust processes. The default threshold level is Success_Offline.

### Enforce(IncomingMessage)

This method enforces the trust policy for incoming messages using the following algorithm:

* Validates that a signature exists on the message.
* Iterates through each domain recipient and, for each, iterates through that recipient's trust anchor set looking for a trust anchor that validates one of the signing certificates in the message's signature block using trust chain validation. This includes certificate path chain validation, certificate expiration, and revocation checking. Usually there is only one signing certificate, but there may be more than one if the sender supports multiple circles of trust. If the signing certificate cannot be validated by the trust anchors, that recipient's status is flagged as not trusted, and the message is not delivered to that recipient. In the agent, this recipient is added to the rejected recipient list of the MessageEnvelope.
* Validates the signature block on the message to ensure it has not been tampered with.

### Enforce(OutgoingMessage)

This method enforces the trust policy for outgoing messages using the following algorithm:

* Iterates through each recipient and looks for a trust anchor in the sender's trust anchor set that validates each recipient's certificates. This includes certificate path chain validation, certificate expiration, and revocation checking. Usually there is only one certificate per recipient, but there may be more than one if the recipient supports multiple circles of trust. If a recipient's certificates cannot be validated against one of the sender's trust anchors, that recipient's trust status is flagged as failed, and the message is not sent to that recipient. In the agent, this recipient is added to the rejected recipient list of the MessageEnvelope.