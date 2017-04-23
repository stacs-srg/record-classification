# `clean` Command
The `clean` command performs cleaning of unseen and gold standard records, typically performed prior to classification. There are 3 ways to clean records:

1. cleaning using one or more predefined cleaners,
3. stop words removal using a user-defined collection of stop words, and
2. spelling correction using a user-defined dictionary.

To clean using one or more predefined cleaners the following must be set option

* `-c` or `--cleaner` -- the option specifying the predefined cleaners with which to clean all loaded unseen and gold standard records. The possible values for this option are:

    - `PUNCTUATION` -- removes punctuation characters.
    - `LOWER_CASE` -- converts the record labels to lower case.
    - `ENGLISH_STOP_WORDS` -- removes a predefined set of English stop words from record labels. The predefined set of english stop words include: "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will" and "with".
    - `PORTER_STEM` -- performs stemming using [Porter stemming algorithm](https://en.wikipedia.org/wiki/Stemming). See [PorterStemFilter](https://lucene.apache.org/core/4_0_0/analyzers-common/org/apache/lucene/analysis/en/PorterStemFilter.html).
    - `CONSISTENT_CLASSIFICATION_CLEANER_CORRECT` -- corrects the classification of any inconsistently classified records to the most popular.
    - `CONSISTENT_CLASSIFICATION_CLEANER_REMOVE` -- removes any inconsistently classified records.
    - `TRIM_CLASSIFICATION_CODE` -- removes white-space characters fom the beginning/end of classification codes associated to each record.
    - `COMBINED` -- applies all available text cleaners and corrects inconsistent classifications.

One or more predefined cleaners can be specified in a single clean command. For example, the following command:

    clean -c LOWER_CASE ENGLISH_STOP_WORDS PUNCTUATION

converts all the loaded record labels to lower case, removes predefined list of english stop words from the labels, and finally removes punctuation characters from the labels.

To clean stop words using a custom list of stop words the `stop_words` sub command is used. The `stop_words` sub command offers the following options:

* `-s` or `--from` -- the _mandatory_ option, which specifies the path to the source file that contains the stop words, one stop word per line.
* `-c` or `--charset` -- specifies the character encoding of the source file. If unspecified, the default character encoding is used; see [`set`](#set).
* `-cs` or `--caseSensitive` -- If present, specifies that the stop words in the source file are case sensitive.

To correct spelling of record labels using a custom dictionary the `spelling` sub command is used. The spelling correction replaces the words in the labels with words in the dictionary if their similarity is above a given threshold. The `spelling` sub command offers the following options:

* `-s` or `--from` -- the _mandatory_ option, which specifies the path to the source file that contains the dictionary of words, one word/phrase per line.
* `-c` or `--charset` -- specifies the character encoding of the source file. If unspecified, the default character encoding is used; see [`set`](#set).
* `-a` or `--accuracyThreshold` -- specifies the similarity threshold, above which to replace words in record labels with words in the dictionary. The theshold value is specified as a number between inclusive range of _0.0_ to _1.0_. For instance, threshold of _0.0_ results in the replacement of all the words in the labels with words in the dictionary. A threshold of _1.0_ result in on change in the record labels, since only words that are exactly the same will be replaced. The default threshold is set to _0.5_, meaning 50% or more similarity between words will result in word replacement.
* `-d` or `--distanceFunction` -- specifies the algorithm by which to calculate the similarity between words in the labels and words in the dictionary. If this option unspecified the `JARO_WINKLER` function is used. The possible values for this option are:

    - `N_GRAMS_2` -- the [nGram](https://en.wikipedia.org/wiki/N-gram) distance function of size _2_.
    - `N_GRAMS_3` -- the [nGram](https://en.wikipedia.org/wiki/N-gram) distance function of size _3_.
    - `N_GRAMS_4` -- the [nGram](https://en.wikipedia.org/wiki/N-gram) distance function of size _4_.
    - `N_GRAMS_5` -- the [nGram](https://en.wikipedia.org/wiki/N-gram) distance function of size _5_.
    - `N_GRAMS_6` -- the [nGram](https://en.wikipedia.org/wiki/N-gram) distance function of size _6_.
    - `N_GRAMS_7` -- the [nGram](https://en.wikipedia.org/wiki/N-gram) distance function of size _7_.
    - `LEVENSTEIN` -- the [Levenstein](https://en.wikipedia.org/wiki/Levenshtein_distance) distance function.
    - `DAMERAU_LEVENSHTEIN` -- the [Damerau–Levenshtein](https://en.wikipedia.org/wiki/Damerau–Levenshtein_distance) distance function.
    - `JARO_WINKLER` -- the [Jaro-Winkler](https://en.wikipedia.org/wiki/Jaro–Winkler_distance) distance function.
