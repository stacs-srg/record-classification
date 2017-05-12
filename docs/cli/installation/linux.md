
# Installation on Linux

The following steps describes how to install `classli` on Linux.

1. If not already present, install [JRE](https://docs.oracle.com/javase/8/docs/technotes/guides/install/linux_jre.html) or [JDK](https://docs.oracle.com/javase/8/docs/technotes/guides/install/linux_jdk.html) version 8.0 or higher.
    - Once installed, open a new `bash` shell.
    - To check the Java installation, type `java -version` and press enter; you should see the Java version and build number.
    - If not, make sure the Java installation directory is added to the `PATH` environment variable (see [help](https://java.com/en/download/help/path.xml)).
2. Download the `classli.tar` [tar file](https://builds.cs.st-andrews.ac.uk/job/digitising_scotland/lastSuccessfulBuild/artifact/record_classification/target/classli.tar) and extract the contents.
4. Copy the extracted `classli` file to `/usr/local/bin`, or another directory of your choice.
    - Ensure that the installation directory is included in the `PATH` environment variable.
5. To check the installation, open a new `bash` shell and type `classli -h`, which should print a usage message.
