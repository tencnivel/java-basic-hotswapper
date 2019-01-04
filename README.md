# Java basic hotswapper
A java hotswapper to automatically redefine classes that have received in-body code changes.
Works with any class including JEE beans.

## Quickstart

  * Modify your JEE server to enable JPDA (see *Prerequisite*).
  * Download the executable jar from the *dist* directory
  * Execute the executable jar passing the argument of the JDI port and the directories 
containing the classes of the project

```
java -jar java-basic-hotswapper-jar-with-dependencies.jar JDIPortNumber dir1 [dir2 dir3 ...]
```

 

## Motivation of the project

Redeploying a project in a JEE servers is quite fast and it is not that bad to have to redeploy 
a project when important changes (eg. adding/renaming a class, adding/renaming a method, 
changing the signature of a method, adding/renaming an attribute ) are made to the code.

The real loss of time and productivity is to have to redeploy a project whenever we do 
a modification to the code of a method.

JDKs (since which version?) are shipped with all we need to reload classes to take into those 
in-body code changes.

JRebel has gotten too expensive, hotswapAgent doesn't work well if you use symlinks to
 deploy your project, IDE's hotswapping has too much adherence to the IDE (the IDE may 
not support it, the server needs to be ran from the IDE).


## Prerequisite

### Enable JPDA in your server so that JDI can attach to it
eg. in wildfly/kboss, uncomment the following line in standalone.conf:

```
# Sample JPDA settings for remote socket debugging
JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"
```

### Using the executable jar

The excutable jar (embedding all required dependencies) can be found in the 'dist' directory.



### Using the sources

NOTE: tools.jar from the openjdk is provided in the *repository* directory of the project. 
A version of tools.jar coming from a jdk8 can attach to JVM instance from an older jdk. 
Maybe the reverse also work (not tested).

If you want to use another version of tools.jar, you will need to add it manually to your .m2 
repository because as of today (2019-01-03) tools.jar are not available on the public maven repositories.
 
Here is how you can install tools.jar from you jdk (located in the lib folder) in your local maven dependencies :

```
mvn install:install-file -Dfile=path-to-tools.jar \
                         -DgroupId=choose.a.groupId \
                         -DartifactId=choose-an-artifactId \
                         -Dversion=choose.a.version \
                         -Dpackaging=jar
```


## Usage

NOTES: 

 * If using symlink, declare the source directory and not the symlink, if not WatchDir 
will not see the changes to the class files.
 * The program can be executed before the server is started because it attaches to the JVM
at every change (and not at startup)

### Using the executable jar

```
java -jar java-basic-hotswapper-jar-with-dependencies.jar JDIPortNumber dir1 [dir2 dir3 ...]
```

eg. 

```
java -jar java-basic-hotswapper-jar-with-dependencies.jar 8787 /home/myhome/myprojects/oneproject/target/classes
```

### Using the sources
The main class is in WatchDir.java


## HOWTO build a new executable jar

```
mvn clean compile assembly:single
```

