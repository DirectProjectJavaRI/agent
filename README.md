# Overview

The security and trust agent is responsible for implementing the policies of the DirectProject [specification](http://wiki.directproject.org/w/images/e/e6/Applicability_Statement_for_Secure_Health_Transport_v1.2.pdf). It contains interfaces and implementations for resolving private and public certificates, message signing and validating message signatures, message encryption, and enforcing trust policies.

The agent is typically embedded in an application or service stack interfacing directly with an NHINDAgent implementation. However, because the agent is comprised of componentized sub systems, it is possible to view the age nt as a library of publicly consumable components and interfaces.


## Guides

This document describes how to develop against components of the security and trust agent module.

* [Development Guide](DevGuide) - This section describes how to consume different components of the module.
* [Tools](Tools) - This section describes various tools in the agent source tree used for testing and development.