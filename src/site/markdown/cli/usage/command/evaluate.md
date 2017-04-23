# `evaluate` Command
The `evaluate` command evaluates the performance of the classifier in terms of precision, accuracy and recall. In order to use this command, firstly, the classifier must be set via the [`set`](#set) command, and secondly, at least one evaluation record must be loaded, i.e. at least one gold standard record collection must be loaded with training ratio of less that _1.0_ (see [`load` command](#load)). It is not mandatory to train the classifier prior to evaluation. However, evaluating an untrained classifier can result in poor performance measures. Typically, it is recommended to train the classifier prior to evaluation. The `evaluate` command offers the following option:

* `-o` or `--outputEvaluationRecordsTo` -- the path in which to store the classified evaluation records. If unspecified, the classified evaluation records will not be stored.

For instance, the following command:

    evaluate -o evaluation_records_classified.csv

will evaluate the classifier with the evaluation records previously loaded (see [`load` command](#load)), and stores the classified evaluation records into a file within the current working directory called _evaluation_records_classified.csv_.  
