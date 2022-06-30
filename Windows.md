# Set up for use on Windows as a standalone application
* Add .JAR to your PATHEXT environment variable
* Check or set the association of .jar files to a command-line version of java
  * Check: assoc .jar
    * Expect: .jar=jarfile
  * Check: ftype jarfile
    * Expect: jarfile="C:\Program Files\Java\jdk-11.0.1\bin\java.exe" -jar "%1" %*
    (or similar)
    * If javaw.exe in stead of java.exe, you will need to change it
* Place the sha.jar file in your path
