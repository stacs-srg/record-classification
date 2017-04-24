# `experiment` Command

The `experiment` command enables the experimentation mode. The experimentation mode:

1. repeats the instructions specified in a batch command file multiple times, and
2. once the repetitions are done, aggregates the repetition results (e.g. evaluation metrics such as precision and recall).

The output of each repetition is stored in folders in the current working directory, named `repetition_#` where `#` is the number of the repetition starting from `1`.

The options of the `experiment` command are:

* `-c` or `--commands` -- specifies the *mandatory* path to a file containing a batch of commands to be executed, where each line represents a command and its options.

* `-r` or `--repeat` -- specifies the number of times the experiment should be repeated. The default value of this option is set to `5`.
