# `set` Command

The `set` command sets the value of configurable variables in the `classli` configuration. The configurable variables are specified with one of the following options:

* `-ch` or `--charset` -- specifies default [character encoding](https://en.wikipedia.org/wiki/Character_encoding) of input/output files, which should be used if no other encoding is specified. The value of this option can be one of:

    - `SYSTEM_DEFAULT` -- the default character encoding of the current operating system.
    - `ISO_8859_1` -- the [ISO_8859_1](https://en.wikipedia.org/wiki/ISO/IEC_8859-1) ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
    - `UTF_8` -- the [UTF-8](https://en.wikipedia.org/wiki/UTF-8), eight-bit [UCS](https://en.wikipedia.org/wiki/Universal_Coded_Character_Set) Transformation Format.
    - `UTF_16` -- the [UTF-16](https://en.wikipedia.org/wiki/UTF-16), sixteen-bit [UCS](https://en.wikipedia.org/wiki/Universal_Coded_Character_Set) Transformation Format, byte order identified by an optional byte-order mark.
    - `UTF_16BE` -- the [UTF-16](https://en.wikipedia.org/wiki/UTF-16), sixteen-bit [UCS](https://en.wikipedia.org/wiki/Universal_Coded_Character_Set) Transformation Format, big-endian byte order.
    - `UTF_16LE` -- the [UTF-16](https://en.wikipedia.org/wiki/UTF-16), sixteen-bit [UCS](https://en.wikipedia.org/wiki/Universal_Coded_Character_Set) Transformation Format, little-endian byte order
    - `US_ASCII` -- the seven-bit [ASCII](https://en.wikipedia.org/wiki/ASCII) , a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set

* `-c` or `--classifier` -- specifies the classifier to be used for classification of records. The possible values for this option are:

    - `EXACT_MATCH` -- classifies records, which exactly match the training records.
    - `STRING_SIMILARITY_LEVENSHTEIN` -- classifies records based their [Levenshtein](https://en.wikipedia.org/wiki/Levenshtein_distance) similarity to the training records.
    - `STRING_SIMILARITY_JARO_WINKLER` -- classifies records based their [Jaro Winkler](https://en.wikipedia.org/wiki/Jaro–Winkler_distance) similarity to the training records.
    - `STRING_SIMILARITY_JACCARD` -- classifies records based their [Jaccard](https://en.wikipedia.org/wiki/Jaccard_index) similarity to the training records.
    - `STRING_SIMILARITY_DICE` -- classifies records based their [Dice](http://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient) similarity to the training records.
    - `OLR` -- classifies records using an [online machine learning](https://en.wikipedia.org/wiki/Online_machine_learning) algorithm based on a [logistic regression](https://en.wikipedia.org/wiki/Logistic_regression) model. It is a heavily modified version of [Mahout's Logistic Regression implementation](https://mahout.apache.org/users/classification/logistic-regression.html).
    - `NAIVE_BAYES` -- classifies records a (naive bayes classifier)[https://en.wikipedia.org/wiki/Naive_Bayes_classifier]. It is heavily based on [Weka](http://www.cs.waikato.ac.nz/ml/weka/) implementation of [naive bayes](http://weka.sourceforge.net/doc.dev/weka/classifiers/bayes/NaiveBayes.html).
    - `VOTING_ENSEMBLE_EXACT_ML_SIMILARITY` -- classifies records using collective voting of `EXACT_MATCH`, `OLR`, `NAIVE_BAYES`, `STRING_SIMILARITY_LEVENSHTEIN`, STRING_SIMILARITY_JARO_WINKLER`, `STRING_SIMILARITY_JACCARD` and `STRING_SIMILARITY_DICE` classifiers. 
    - `VOTING_ENSEMBLE_EXACT_SIMILARITY` -- classifies records using collective voting of `EXACT_MATCH`, `STRING_SIMILARITY_LEVENSHTEIN`, STRING_SIMILARITY_JARO_WINKLER`, `STRING_SIMILARITY_JACCARD` and `STRING_SIMILARITY_DICE` classifiers. 

* `-d`, `--delimiter` -- specifies the default delimiter character of input/output tabular data files. The value of this option may be specified as a single character. If multiple characters are specified, the first character will be considered as the delimiter.

* `-r` or `--randomSeed` -- specifies the seed of the internal random number generator. By default the internal random number generator us non-deterministic. Setting the random seed results in deterministic selection of training and evaluation records. The value of the seed is specified as a 64-bit integer value.

* `-s` or `--serializationFormat` -- specifies the format by which to persist the classifier. The possible values for this option are:

    - `JAVA_SERIALIZATION` -- use Java object serialisation. 
    - `JSON` -- use human-readable [JSON](https://en.wikipedia.org/wiki/JSON) format.
    - `JSON_COMPRESSED` -- use compressed human-readable [JSON](https://en.wikipedia.org/wiki/JSON) format.
    
* `-t` or `--trainingRatio` -- specifies the default proportion of _gold standard_ records to use for training the classifier. The remaining gold standard records are used for evaluation of the classifier via the [`evaluate`](#evaluate) command. The value of this option is specified as a numver between _0.0_ to _1.0_.
                 
* `-it` or `--internalTrainingRecordRatio` -- specifies the default proportion of _training_ records to use for training the classifier. The remaining training records will be used by the classifier for self-evaluation mechanism. The value of this option is specified as a numver between _0.0_ to _1.0_.

* `-f` or `--format` -- specifies the default CSV format of input/output tabular data files. The possible values for this option are:
    - `RFC4180` -- the comma separated format as defined by [RFC 4180](http://tools.ietf.org/html/rfc4180). See [RFC4180](https://commons.apache.org/proper/commons-csv/archives/1.2/apidocs/org/apache/commons/csv/CSVFormat.html#RFC4180)
    - `RFC4180_PIPE_SEPARATED` -- the format as defined by [RFC 4180](http://tools.ietf.org/html/rfc4180), but with `|` (pipe) as delimiter.
    - `DEFAULT` -- based on `RFC4180` allowing empty lines. See [DEFAULT](https://commons.apache.org/proper/commons-csv/archives/1.4/apidocs/org/apache/commons/csv/CSVFormat.html#DEFAULT)
    - `EXCEL` -- based on `RFC4180` allowing missing column names. Please note that the delimiter character used by Excel is locale dependent, it may be necessary to customize the delimiter character via `-d` or `--delimiter` options. See [EXCEL](https://commons.apache.org/proper/commons-csv/archives/1.2/apidocs/org/apache/commons/csv/CSVFormat.html#EXCEL)
    - `MYSQL` -- the default [MySQL](http://dev.mysql.com/doc/refman/5.1/en/load-data.html) format used by the SELECT INTO OUTFILE and LOAD DATA INFILE operations. This is a tab-delimited format with a LF character as the line separator. Values are not quoted and special characters are escaped with `\`. See [MYSQL](https://commons.apache.org/proper/commons-csv/archives/1.2/apidocs/org/apache/commons/csv/CSVFormat.html#MYSQL)
    - `TDF` -- the tab-delimited format. See [TFD](https://commons.apache.org/proper/commons-csv/archives/1.2/apidocs/org/apache/commons/csv/CSVFormat.html#TDF)

At least one option must be specified when calling the `set` command. For example, execution of the following command:

    set --delimiter "|"

sets the default delimiter to `|` (pipe) character. Since the pipe character is a special character in command-line environment, it has been surrounded by double quotes.

In another example, the following command:

    set -t 0.7

sets the default training ratio to 70%. This means unless otherwise specified by default 70% of loaded gold standard data will be used for training the classifier and the remaining 30% will be used for evaluation.

It is also possible to set multiple values at once. For example, the following command:

    set -c OLR -r 42 -s JSON

sets the classifier to the _online logistic regression_ classifier, the random seed to _42_ and the classifier serialization format to _JSON_. 
