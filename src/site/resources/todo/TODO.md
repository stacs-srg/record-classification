 
# Short-Term Tasks
 
## For Zen & Kostas

- [x] Improve the user-friendliness of the classification process.
   - [x] The ability to use spell checking from the CLI.
- [x] Spell checking cleaning.
   - [x] General English dictionary.
   - [x] Domain-specific dictionary: text from HISCO definition.
   - [x] Mechanism for specifying additional dictionaries.
- [ ] Tests.
   - [ ] Set up Maven integration test.
   - [ ] End to end test for separate training and testing.
   - [ ] Test for variation between repetitions.
   - [ ] Test for voting classifier.
- [x] Assemble dataset overview lists for causes of death, and linked data.
- [x] Custom stop word list.
- [x] Implement stop word cleaning that considers data as a whole, i.e. a word is a stop word if it appears solely.
- [x] Add the confidence column in output file.

## For Richard before Edinburgh Meeting

- [x] Occupation classification runs.
   - [x] Cambridge data set.
   - [x] Jeff's UK data set.
   - [x] Decide what others from various census data sets.
   - [x] Visualise the relationship between ancestor distance and classification confidence.
   - [x] Check for inconsistent coding to sub category heading in gold standard; if inconsistent rerun experiments.
   - [x] Produce summary classification metrics per major and minor HISCO groups.
   
## General

- [ ] Implement second string similarity classifier (GK).
- [x] Add multiple user entry points to the project.
   - [x] Train classifier on specified gold standard data and persist state to specified location
   - [x] Load classifier from persistent state, classify specified unseen data, save results from/to specified locations
- [ ] Merge single and multiple classifier API, and parameterise the number of classifications per record.
- [ ] Spell checking cleaning.
   - [ ] Domain-specific dictionaries: text from ICD10 definition.
   - [ ] Domain-specific dictionaries: text from archaic medical terms.
- [x] White-space characters at the begining/end of classification codes causes error in NaiveBayesClassifier training.
   Possible solutions:
      - Check if such codings exist and fail.
      - Define coding scheme format in a class and clean data according to the coding scheme format (e.g. regex)
      - Clean classification codes by code.trim, assuming no such code should exist.
- [ ] Train different classifiers for different parts of the HISCO hierarchy.
- [x] Set up record-classification testing on Windows platform.
- [x] Implement string similarity classifier.
- [x] Implement Naive Bayes classifier (GK).
- [x] Implement OLR classifier (GK).
- [x] Implement ensemble classifier.
- [x] Implement classifier serialisation/de-serialisation.
- [x] Implement stemming.
- [x] Implement removal of stop words.
- [x] Convert HISCO definitions to three column gold standard data format.
- [x] Merge duplicate classification process functionality in process package
- [x] Enable reproducible training data selection when comparing multiple classifiers.
- [x] Implement String record level cleaning.
- [x] Train using multiple files e.g. HISCO dictionary.
- [x] Spell checking cleaner.
- [x] Generate EXE file for record classification cli using Launch4j, winrun4j and such.

# Medium/Long-Term Tasks

- [x] Implement ensemble using confidence measures.
- [ ] Implement ensemble using ranked lists of possible classifications.
- [x] Implement ensemble using accuracy metrics from training - feedback per-class quality as proxy for confidence.
- [ ] Control over whether persisted models include sensitive training data.
- [x] Project tools.
   - Story board?
   - Issue tracking?
   - High-level log of what's been done.
   - Specify deadlines.
   - Tagging of content, tasks, etc.
   - Date/Time at which a task was completed.
   - Trello?
   - YouTrack


# Queries and Comments

- [x] Why bother checking for null in constructor of StringSimilarityClassifier? We don't check parameters in most situations.
- [x] Not clear that conceptually StringSimilarityClassifier should extend ExactMatchClassifier - it's a different thing, not a particular kind of exact match. Also not sure about the similarity classifier first checking for exact match. Might be cleaner for the individual classifiers just to do one thing, and leave that issue to the ensembles.
- [x] Probably nicer to split into Cleaner and Checker interfaces, where the first changes stuff and the second just checks.

# Experiments

## System Experiments

- [ ] Dictionary experiments
   - [ ] Threshold based word replacement
   - [ ] Best match word replacement
   - [ ] Word frequency analysis
- [ ] System scalability experiment -- the Goal is to find out:
   - [ ] how much classification/training each classifier can cope with.
   - [ ] investigate the relationship between training time and classification precision/accuracy.
- [ ] Meaningfulness of confidence values of each classification.
   - [ ] Investigate how the confidence values for each classification relates to gold_standard.
- [ ] How often classifiers in voting ensemble agree with eachother.

# Meetings

## Edinburgh meeting at 12th October 2015

- [x] Ask Zen:
   - What did he mean by words that should be counted as stop words if appear alone?
      - This will result in empty strings that *should* be classified as UNCLASSIFIED?
      - Alternatively, we can have a list of strings that are classified as UNCLASSIFIED in gold standard.
- [ ] What coding schemes can be made public?
- [ ] Can the column names of a secret data be made public?
- [ ] Discuss basis for experiments with publishable results and writing papers.
- [ ] Ask social scientists: 
   - In general, should an empty string be classified as UNCLASSIFIED?

# Infrastructure

- [ ] Need for the ability to store/retrieve encrypted datasets.
- [ ] Ciesvium's purpose needs focus. Should do one thing and do it well.
   - [ ] CSV stream
   - [ ] encrypted CSV stream
   - [ ] CSV to Java Bean


