# digest-verify
Generates a digest for a file in users local file-system (or compares digest with given digest)

This project was created because I needed a simple tool to generate, or verify, a files digest on Windows.

It is compiled using Java 8, but it can be compiled and run with Java 7 as well. (pom needs to be updated to do so.)

SHA-256 is used by default, but any MessageDigest algorithm supported by Java may be used.
To get help, run without supplying arguments.
