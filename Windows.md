# Set up for use on Windows as a standalone application
* Add .JAR to your PATHEXT environment variable (to allow using the filename without extension as a command)
* Check or set the association of .jar files to a command line version of java
  * Check: `assoc .jar`
    * Expect: `.jar=jarfile`
  * Check: `ftype jarfile`
    * Expect: `jarfile="C:\Program Files\Java\jdk-11.0.1\bin\java.exe" -jar "%1" %*`
    (or similar)
    * If `javaw.exe` is shown in stead of `java.exe`, you should change it (this is meant to be a "command line" application)
* Place the sha.jar file in your path

_The associated filetype (ftype) can be anything you like, so you can create your own if you want to. Remember to update the reference to it with the `assoc` command if you do._

**PS:** _This procedure does not work on Windows 11_

To create a module jar:

`jar --create --file mods\sha.jar --main-class no.moldesoft.app.sha256.Verifier -C target\classes .`

To run as modular jar:

`java -p mods -m no.moldesoft.sha.verifier`