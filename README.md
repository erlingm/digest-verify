# SHA256 verifier
Generates a digest for a file in users local file-system (or compares digest with given digest)

This project was created because I needed a simple tool to generate, or verify, a files digest on Windows.

It is compiled using Java 17.

SHA-256 is used by default, but any MessageDigest algorithm supported by Java may be used.
To get help, run without supplying arguments.

I use it mainly to compare digests on files I have downloaded from the Internet
to the digest also downloaded from the internet.

Therefor I have modified it a little, so it can guess which algorithm to use
based on the length of the first argument (the digest) when used this way, thereby
avoding the need to explicitly provide the algorithm explicitly in this use case.
