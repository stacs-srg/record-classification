# Installation on Windows

The following steps describe how to install `classli` on Windows.

1. If not already present, install [JRE](https://docs.oracle.com/javase/8/docs/technotes/guides/install/windows_jre_install.html#CHDEDHAJ) or [JDK](https://docs.oracle.com/javase/8/docs/technotes/guides/install/windows_jdk_install.html#CHDEBCCJ) version 8.0 or higher ([Windows System Requirements](https://docs.oracle.com/javase/8/docs/technotes/guides/install/windows_system_requirements.html)).
    - Once installed,[open a command prompt window](http://windows.microsoft.com/en-gb/windows-vista/open-a-command-prompt-window).
    - To check the Java installation, type `java -version` and press enter; you should see the Java version and build number.
    - If not, make sure the Java installation directory is added to the `PATH` environment variable ()see [help](https://java.com/en/download/help/path.xml)).
2. Download the `classli.exe` [binary file](https://builds.cs.st-andrews.ac.uk/job/digitising_scotland/lastSuccessfulBuild/artifact/record_classification/target/classli.exe).
3. Copy the downloaded file to the `%SystemRoot%\system32` directory, where `%SystemRoot%` is the directory in which Windows was installed, or to another directory of your choice.
    - If you do not know where that is, type `echo %SystemRoot%` in the command prompt window and press enter to print the path to the _systemroot_ directory.
    - Ensure that the installation directory is included in the `PATH` environment variable.
4. If you have modified the `PATH` environment variable, restart the computer for changes to take effect.
5. To check the installation, open a command prompt window and type `classli -h`, which should print a usage message.

