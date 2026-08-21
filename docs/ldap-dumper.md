---
title: LDAP Certificate Dumper
---

# LDAP Certificate Dumper

The reference implementation source tree provides a tool for retrieving user and organizational certificates by email address or domain using LDAP and writing the certificate to a DER-encoded file. The tool is located under the agent/tools directory in the source tree and is named ldapCertDumper.sh for Unix/Linux-based systems and ldapCertDumper.bat for Windows.

## LDAP Certificate Resolution

The Direct Project defines a method for resolving public certificates using LDAP. In some cases, it may be desirable to manually download a public certificate using LDAP and dump it to a file. The ldapCertDumper tool uses the LDAPCertificateResolver with the public SRV resolver to locate certificates using an email address or a domain name. Certificates are located using the resolution algorithms defined by the Direct Project, meaning the resolver will look for user-level certificates first, then fall back to searching for organizational certificates if a user-level certificate cannot be found.

### ldapCertDumper

To run the tool, run the following command in the /java/agent/tools directory:

Windows:

```
ldapCertDumper.bat
```

Unix/Linux/MAC

```
./ldapCertDumper.sh
```

Running the tool without any parameters will display the options:

```
Usage:
java LDAPCertDumper (options)...

options:
-add address		Email address of org/domain to retrieve certs for.

-out  Out File		Optional output file name for the cert.
			Default: <email address>(<cert num>).der
```

* **Address:** This is the email address associated with the certificate you are searching for. This can also be a domain name if you are searching for an org-level certificate only.
* **Out:** The name of the file that will be generated. By default, the tool uses the email or domain name followed by the .der extension. In some cases, multiple certificates may be discovered; in this case, the tool will append an incrementing number, starting with 1, enclosed in parentheses to the out file name.

If one or more certificates are discovered, the files are written as DER-encoded files. If a file with the same name as the out file already exists, the tool will overwrite it.