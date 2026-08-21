---
title: Overview
---

# Overview

The security and trust agent is responsible for implementing the policies of the DirectProject [specification](http://wiki.directproject.org/w/images/e/e6/Applicability_Statement_for_Secure_Health_Transport_v1.2.pdf). It contains interfaces and implementations for resolving private and public certificates, signing messages and validating message signatures, encrypting messages, and enforcing trust policies.

The agent is typically embedded in an application or service stack that interfaces directly with an NHINDAgent implementation. However, because the agent is composed of componentized subsystems, it is also possible to use the agent as a library of independently consumable components and interfaces.

## Guides

This document describes how to develop against components of the security and trust agent module.

* [Development Guide](dev-guide) - This section describes how to consume different components of the module.
* [Tools](tools) - This section describes various tools in the agent source tree used for testing and development.