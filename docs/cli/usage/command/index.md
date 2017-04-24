# Usage

Once `classli` is [installed](../install/), it is possible to start using the program via the command line. Open the command-line interface on your operating system (Terminal on OS X, Command Prompt on Windows, or Bash on Linux/Unix operating systems), type `classli -h` and press enter. You will see the usage message being printed explaining how to use classli. Do not panic; this document aims to explain what the options are, what they mean and how to use them. The `classli -h` commands and options instruct it to do what it is required it to do.

The format by which the command and options are specified consists of 6 parts:

    classli [options] [command] [command-options] [sub-command] [sub-command-options]
    '--.--' '---.---' '---.---' '-------.-------' '-----.-----' '---------.---------'
       1        2         3             4               5                 6         

Part 1, `classli`, specifies the name of the program to execute, telling the command-line interface what program to start.

Parts 2 to 6 specify the command-line parameters to be passed to `classli` by the command-line interface. The `classli` command-line parameters are either _options_ or _commands_. Options are specified by their name, always starting with a `-` (hyphen). Each option typically has a short name and a long name, which can be used interchangeably. The short names are useful for quickly specifying commands without having to press lots of keystrokes. The long commands are useful for human readability of the instructions, providing more clues to what an instruction does without having to read the documentations.

Similar to options, commands are also specified by their name, except command names never start with `-`. The command names look more like a verb or a noun. Each command can have its own commands (i.e. sub-command). Further, each command and sub-command can have options. This document explains the possible options and commands of `classli`, starting with possible options for part 2.

The possible options of part 2 are:

* `-c` or `--commands` -- specifies the path to a file containing a batch of commands to be executed, where each line represents a command and its options. Please note, if this option is specified, the program enters the batch execution mode and will only execute the commands specified in the file.
* `-h` or `--help` -- displays the usage information on the console.
* `-v` or `--verbosity` -- specifies the level of information printed into the console by classli. The possible values for this option are:

    - `ALL` -- print all messages, regardless of the level of severity.
    - `SEVERE` -- only print severe error messages.
    - `WARNING` -- print warning and severe error messages.
    - `INFO` -- print informational, warnings and severe error message.
    - `OFF` -- do not print any messages.

The following pages explain parts 3 to 6, the commands and options offered by `classli`:

- [`init`](init.html)
- [`set`](set.html)
- [`load`](load.html)
- [`clean`](clean.html)
- [`train`](train.html)
- [`evaluate`](evaluate.html)
- [`classify`](classify.html)
- [`experiment`](experiment.html)
