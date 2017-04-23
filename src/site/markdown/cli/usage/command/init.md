# `init` Command

The `init` command initialises a new workflow that gets persisted on your hard drive. This means when `classli` program is closed and re-opened, it will remember where things were left off. The options of this command are:

* `-f` or `--force` -- enables replacement of any existing configuration folder upon initialisation. This option is not mandatory. By default this option is disabled.

To execute this command, type the following and press enter:

    classli init

The execution of the command above will result in creation of a folder called `.classli` in the current working directory. This is where all the `classli` files and settings will be stored. The execution of this command in a working directory that already contains a folder named `.classli` will result in failure; to override an existing `.classli` folder, the _force_ parameter must be set:
 
    classli init -f

Alternatively, the _force_ can be set using its long name:

    classli init --force
