# Tools

This section describes tools that accompany the agent source code.

The reference implementation's agent source tree provides a set of tools to assist testing and development. The tools are located under the /agent/tools (assuming you clone the code under a directory called agent) directory in the source tree.

NOTE: You will also need maven installed to build the tools. After checking out the source and installing maven, build the tools by running the following command in the agent directory:

```
mvn clean install
```

These tools are also bundled together in the stock assembly tar.gz file under a directory named *tools*.

* [Certificate Generation](CertGen)
* [DNS Certificate Dumper](DNSDumper)
* [LDAP Certificate Dumper](LDAPDumper)