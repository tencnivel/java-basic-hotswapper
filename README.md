# java-basic-hotswapper
A java hotswapper to automatically redefine classes that have received in-body code changes.

## Motivation of the project

Redeploying a project in a JEE servers is quite fast and it is not that bad to have to redeploy 
a project when important changes (eg. adding/renaming a class, adding/renaming a method, 
changing the signature of a method, adding/renaming an attribute ) are made to the code.

The real loss of time and productivity is to have to redeploy a project whenever we do 
a modification to the code of a method.

JDKs (since which version?) are shipped with all we need to reload classes to take into those 
in-body code changes.

JRebel has gotten too expensive, hotswapagent doesn't work well if you use symlinks to
 deploy your project, IDE's hotswapping has too much adherence to the IDE (the IDE may 
not support it, the server needs to be ran from the IDE).


## Prerequisite

### Enable JPDA in yout server so that we can attach to it
eg. in wildfly/kboss, find the following in standalone.conf:

```
# Sample JPDA settings for remote socket debugging
JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=n"
```

### If using the executable jar: download to your computer


### If using the sources: add tools.jar to your local maven repository

Install tools.jar from you jdk (in the lib folder) in your local maven dependencies

```
mvn install:install-file -Dfile=path-to-your-artifact-jar \
                         -DgroupId=your.groupId \
                         -DartifactId=your-artifactId \
                         -Dversion=version \
                         -Dpackaging=jar
```

NOTE: tools.jar can come from a more recent JDK than the JVM to which we are attaching.

## Usage

Execute the main file located in **WatchDir** with arguments of the listening port for JDI and 
the paths of the directories containing the classes we want to automatically reload at changes.

```
java WatchDir JDIPortNumber dir1 [dir2 dir3 ...]
```

NOTES: 

 * If using symlink, declare the source directory and not the symlink, if not WatchDir 
will not see the changes to the class files.
 * The program can be executed before the server is started because it attaches to the JVM
at every change (and not at startup)
