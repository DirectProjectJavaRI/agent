---
title: DNS Certificate Dumper
---

# DNS Certificate Dumper

The reference implementation source tree provides a tool for retrieving user and organizational certificates by email address or domain over DNS and writing the certificate to a DER-encoded file. The tool is located under the agent/tools directory in the source tree and is named dnsCertDumper.sh for Unix/Linux-based systems and dnsCertDumper.bat for Windows.

## DNS Certificate Resolution

The Direct Project defines a method for resolving public certificates using DNS CERT RR records. In some cases, it may be desirable to manually download a public certificate using DNS and dump it to a file. The dnsCertDumper tool uses the DNSCertificateResolver to locate certificates using an email address or a domain name. Certificates are located using the resolution algorithms defined by the Direct Project, meaning the resolver will look for user-level certificates first, then fall back to searching for organizational certificates if a user-level certificate cannot be found.

### dnsCertDumper

To run the tool, run the following command in the /java/agent/tools directory:

Windows:

```
dnsCertDumper.bat
```

Unix/Linux/MAC

```
./dnsCertDumper.sh
```

Running the tool without any parameters will display the options:

```
Usage:
java DNSCertDumper (options)...

options:
-add address		Email address of org/domain to retrieve certs for.

-server     		Comma delimited list of DNS servers used for lookup.
			Default: Local machine's configured DNS server(s)

-out  Out File		Optional output file name for the cert.
			Default: <email address>(<cert num>).der
```

* **Address:** This is the email address associated with the certificate you are searching for. This can also be a domain name if you are searching for an org-level certificate only.
* **Server:** If the server parameter is supplied, the underlying DNS resolver will use the supplied DNS server instead of the local machine's configured DNS server. This may be desirable if you are experiencing difficulties with your DNS provider.
* **Out:** The name of the file that will be generated. By default, the tool uses the email or domain name followed by the .der extension. In some cases, multiple certificates may be discovered; in this case, the tool will append an incrementing number, starting with 1, enclosed in parentheses to the out file name.

If one or more certificates are discovered, the files are written as DER-encoded files. If a file with the same name as the out file already exists, the tool will overwrite it.