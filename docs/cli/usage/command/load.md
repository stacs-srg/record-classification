# `load` Command

The `load` is used for reading in gold standard and unseen records from a file on the local file system. The options of this command are:

* `-s` or `--from` -- the *mandatory* option that specifies the path to the source file from which to load gold standard or unseen records.
* `-c` or `--charset` -- the character encoding of the source file. If unspecified, the default character encoding is used; see [`set`](#set).
* `-n` or `--named` -- specifies a name for the records to be loaded. If unspecified the file name is used as the name of the loaded records.
* `-o` or `--overrideExisting` -- if specified, the load will overrider any existing gold standard or unseen records with the same name. This option is disable by default. 

In order to specify whether the source file contains unseen or gold standard records, the `load` command must be used in conjunction with one of its sub commands: `unseen` and `gold_standard`.

* `unseen` -- the sub command specifying the type of records in the source file as unseen records. The unseen records are records to be classified by the classifier, where each record consist of a unique numerical ID and a textual label. This sub command has the following options:
    * `-d` or `--delimiter` -- specifies the delimiter character of the tabular data in source file. If unspecified the default delimiter character is used; see [`set`](#set).
    * `-f` or `--format` -- specifies the tabular data format of the source file. If unspecified, the default csv format is used. See [`set`](#set) command for possible values of this option.
    * `-ii` or `--id_column_index` -- specifies the index of the column containing the ID associated to each record. The value is specified as a positive integer including `0`. `0` corresponds to the first column, `1` corresponds to the second column and so on. The default value of this option is `0`.
    * `-li` or `--label_column_index` -- specifies the index of the column containing the label (i.e. text or data) associated to each record. The value is specified as a positive integer including `0`. `0` corresponds to the first column, `1` corresponds to the second column and so on. The default value of this option is `1`.
    * `-h` or `--skip_header` -- whether to consider the first record in the source file as column headers and skip it. this option is disabled by default.

* `gold_standard` -- the sub command specifying the type of records in the source file as gold standard records. The gold standard records are records to be used to train and evaluate the classifier, where each record consist of a unique numerical ID, a textual label and a textual class. This sub command has the following options:
    * `-d` or `--delimiter` -- specifies the delimiter character of the tabular data in source file. If unspecified the default delimiter character is used; see [`set`](#set).
    * `-f` or `--format` -- specifies the tabular data format of the source file. If unspecified, the default csv format is used. See [`set`](#set) command for possible values of this option.
    * `-ii` or `--id_column_index` -- specifies the index of the column containing the ID associated to each record. The value is specified as a positive integer including `0`. `0` corresponds to the first column, `1` corresponds to the second column and so on. The default value of this option is `0`.
    * `-li` or `--label_column_index` -- specifies the index of the column containing the label (i.e. text or data) associated to each record. The value is specified as a positive integer including `0`. `0` corresponds to the first column, `1` corresponds to the second column and so on. The default value of this option is `1`.
    * `-ci` or `--class_column_index` -- specifies the index of the column containing the class (i.e. classification code) associated to each record. The value is specified as a positive integer including `0`. `0` corresponds to the first column, `1` corresponds to the second column and so on. The default value of this option is `2`.
    * `-h` or `--skip_header` -- whether to consider the first record in the source file as column headers and skip it. this option is disabled by default.
    *  `-t` or `--trainingRatio` -- specifies the ratio of gold standard records to be used for training. The value must be within inclusive range of _0.0_ to _1.0_. If not specified, the default training ratio is used; see [`set`](#set).

An execution of `load` command either reads gold standard records or unseen records. For example, the following command:

    load --from my_dataset.csv gold_standard -t 0.8 -h

loads  gold standard records from a file in the current working directory called _my_dataset.csv_ while skipping the first record, where 80% of the records will be used for training the classifier, and the remaining 20% will be used for evaluation of the classifier. Default values will be used for any of the unspecified options.

In another example, the following command:

    load --from my_other_dataset.csv unseen -ii 4 -li 5

loads unseen records from a file in the current working directory called my_other_dataset.csv_, where the ID of each record is specified in the fifth column, and the label of each record is specified in the sixth column. Default values will be used for any of the unspecified options.

