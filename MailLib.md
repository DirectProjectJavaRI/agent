# Mail Library

The agent module contains various utility mail classes to facilitate implementing the security and trust implementation.

## MimeEntity

The [MimeEnity](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/mail/MimeEntity.html) class is an extension of the Java Mail [MimeBodyPart](http://java.sun.com/products/javamail/javadocs/javax/mail/internet/MimeBodyPart.html) with utility functions to determine if the entity consists of a multiple part and serialization to a byte array.

## NHINDAddress

The [NHINDAddress](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/NHINDAddress.html) class is an extension of the JavaMail [InternetAddress](http://java.sun.com/products/javamail/javadocs/javax/mail/internet/InternetAddress.html) with utility methods and attributes to bind certificates, store trust anchors, and set the trust status. It also include simple parsing parsing.

## WrappedMessage

The [WrappedMessage](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/mail/WrappedMessage.html) class is a utility class for wrapping messaging in an RFC822 container and copying headers from the original message to the container. It also provides unwrapping methods.

## Message

The [Message](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/mail/Message.html) class is an extension of the Java Mail [MimeMessage](http://java.sun.com/products/javamail/javadocs/javax/mail/internet/MimeMessage.html) with utility methods to get specific header information in raw format and serialization to a byte array.

## MessageEnvelope

The [MessageEnvelope](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/MessageEnvelope.html) interface is a message wrapper that holds the original message and additional attributes to categorize routing information such as reject recipients and domain recipients.

## DefaultMessageEnvelope

The [DefaultMessageEnvelope](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/DefaultMessageEnvelope.html) class is the default implementation of the MessageEnvelope interface.

## IncomingMessage

The [IncomingMessage](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/IncomingMessage.html) class is an extension of the DefaultMessageEnvelope that exposes message signatures and CMS data.

## OutgoingMessage

The [OutgoingMessage](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/OutgoingMessage.html) class is an extension of the DefaultMessageEnvelope. At this time is does provide any other functionality above and beyond DefaultMessageEnvelope other than strong typing.

## EntitySerializer

The [EntitySerializer](http://api.directproject.info/agent/2.2.1/apidocs/org/nhindirect/stagent/parser/EntitySerializer.html) class is a utility class for serializing and deserializing message to and from different message structures and raw representations.

