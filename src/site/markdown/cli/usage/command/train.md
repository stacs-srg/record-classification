# `train` Command

The `train` command trains the classifier with the loaded gold standard records. In order to use this command, firstly, the classifier must be set via the [`set`](#set) command, and secondly, at least one training record must be loaded, i.e. at least one gold standard record collection must be loaded with training ratio of more that _0.0_ (see [`load` command](#load)). The `train` command offers the following option:

* `-it` or `--internalTrainingRecordRatio` -- the ratio of gold standard records to be used for training as opposed to internal evaluation. If unspecified, the default internal training ratio is used; see ([`set`](#set)) The value must be within inclusive range of _0.0_ to _1.0_.

For example, the following command:

    train -it 0.9

trains the classifier with 90% of the loaded training records, where the remaining 10% of the training records are used by the classifier to evaluate its own performance.

**Note:** internal evaluation is necessary in order to calculate the confidence measure for a classification; if the internal training ratio is set to _1.0_ (i.e. 100%), all of the loaded training records will be used for training the classifier. The classifier will not perform internal evaluation, and therefore, is unable to calculate confidence for any classified records.
