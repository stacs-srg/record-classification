 
# Short-Term Tasks
 
- [x] Set up record-classification testing on Windows platform.
- [x] Implement string similarity classifier.
- [ ] Implement second string similarity classifier (GK).
- [ ] Implement Naive Bayes classifier (GK).
- [ ] Implement OLR classifier (GK).
- [x] Implement ensemble classifier.
- [x] Make this todo page.
- [ ] Implement classifier serialisation/de-serialisation.
- [ ] Add multiple user entry points to the project.
   - [ ] Train classifier on specified gold standard data and persist state.
      - [ ] to specified location
      - [ ] to default location? within resource tree?
   - [ ] Load classifier from persistent state, classify specified unseen data, save results.
      - [ ] from/to specified locations
      - [ ] from/to default locations?
- [ ] Automate Windows executable generation.
- [ ] Implement stemming and removal of stop words.
- [ ] Add text from HISCO definitions to training set.

# Queries and Comments

- [ ] Why bother checking for null in constructor of StringSimilarityClassifier? We don't check parameters in most situations.
- [ ] Not clear that conceptually StringSimilarityClassifier should extend ExactMatchClassifier - it's a different thing, not a particular kind of exact match. Also not sure about the similarity classifier first checking for exact match. Might be cleaner for the individual classifiers just to do one thing, and leave that issue to the ensembles.

# Medium/Long-Term Tasks

- [ ] Implement ensemble using confidence measures.
- [ ] Implement ensemble using ranked lists of possible classifications.
- [ ] Implement ensemble using accuracy metrics from training.