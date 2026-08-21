---
title: Tools
---

# Tools

This section describes tools that accompany the agent source code.

The reference implementation's agent source tree provides a set of tools to assist with testing and development. The tools are located under the /agent/tools directory in the source tree (assuming you clone the code under a directory called agent).

**NOTE:** You will also need Maven installed to build the tools. After checking out the source and installing Maven, build the tools by running the following command in the agent directory:

```
mvn clean install
```

These tools are also bundled together in the stock assembly tar.gz file under a directory named *tools*.

* [Certificate Generation](cert-gen)
* [DNS Certificate Dumper](dns-dumper)
* [LDAP Certificate Dumper](ldap-dumper)