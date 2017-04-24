# Installation on OS X

The following steps describe how to install `classli` on OS X.

1. If not already present, install [JRE](https://docs.oracle.com/javase/8/docs/technotes/guides/install/mac_jre.html#CHDGECEB) or [JDK](https://docs.oracle.com/javase/8/docs/technotes/guides/install/mac_jdk.html) version 8.0 or higher (see [FAQ](https://docs.oracle.com/javase/8/docs/technotes/guides/install/mac_install_faq.html)).
    - Once installed, open the terminal app `/Applications/Utilities/Terminal.app`.
    - To check the Java installation, type `java -version` and press enter; you should see the Java version and build number.
    - If not, make sure the Java installation directory is added to the `PATH` environment variable (see [help](https://java.com/en/download/help/path.xml)).
2. Download the `classli.tar` [tar file](https://builds.cs.st-andrews.ac.uk/job/digitising_scotland/lastSuccessfulBuild/artifact/record_classification/target/classli.tar) and extract the contents.
3. Copy the extracted `classli` file to `/usr/local/bin`, or another directory of your choice.
    - Ensure that the installation directory is included in the `PATH` environment variable.
4. To check the installation, open a new terminal window and type `classli -h`, which should print a usage message.
