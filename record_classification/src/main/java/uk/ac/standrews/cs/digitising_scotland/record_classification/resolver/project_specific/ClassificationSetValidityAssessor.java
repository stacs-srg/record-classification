package uk.ac.standrews.cs.digitising_scotland.record_classification.resolver.project_specific;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.code.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.tokens.TokenSet;
import uk.ac.standrews.cs.digitising_scotland.record_classification.resolver.Interfaces.ValidityAssessor;

import java.util.Set;

/**
 * Validity assessor for sets of classifications.
 * A Set<Classifiction> is said to be valid if the union of the token sets  of the set of
 * Classifications is a member of the power set of the TokenSet.
 * TODO check validity of hierarchies?
 * Created by fraserdunlop on 06/10/2014 at 16:37.
 */
public class ClassificationSetValidityAssessor implements ValidityAssessor<Set<Classification>,TokenSet> {

    /**
     * Assesses if the union of the token sets  of the set of
     * Classifications is a member of the power set of the TokenSet.
     * @param classifications the set of classifications to assess.
     * @param tokenSet the TokenSet to test against.
     * @return boolean.
     */
    @Override
    public boolean assess(Set<Classification> classifications, TokenSet tokenSet) {
        Multiset<TokenSet> tokenSetsFromClassifications = getTokenSetsFromClassifications(classifications);
        TokenSet unionOfTokenSets = getUnionOfTokenSets(tokenSetsFromClassifications);
        return tokenSet.containsAll(unionOfTokenSets) && noTokenAppearsInUnionMoreOftenThanInOriginalSet(tokenSet, unionOfTokenSets);
    }

    /**
     * Gets the union of a multiset of tokenSets.
     * @param tokenSets the tokenSet Multiset to create a union from.
     * @return the union of all sets in the Multiset - a TokenSet.
     */
    private TokenSet getUnionOfTokenSets(final Multiset<TokenSet> tokenSets) {
        Multiset<String> union = HashMultiset.create();
        for (TokenSet tokenSet : tokenSets) {
            for (String token : tokenSet) {
                union.add(token);
            }
        }
        return new TokenSet(union);
    }

    /**
     * Creates a Multiset of the token sets belonging to a set of Classifications.
     * @param classifications the classifications
     * @return the token sets from triple
     */
    private Multiset<TokenSet> getTokenSetsFromClassifications(final Set<Classification> classifications) {
        Multiset<TokenSet> tokenSets = HashMultiset.create();
        for (Classification classification : classifications) {
            tokenSets.add(classification.getTokenSet());
        }
        return tokenSets;
    }

    /**
     * Returns true if no token appears in union more often than in original set.
     * False otherwise.
     * @param originalTokenSet a tokenSet.
     * @param union a tokenSet.
     * @return boolean
     */
    private boolean noTokenAppearsInUnionMoreOftenThanInOriginalSet(final TokenSet originalTokenSet, final TokenSet union) {
        for (String token : union) {
            TokenSet originalCopy = new TokenSet(originalTokenSet);
            TokenSet unionCopy = new TokenSet(union);
            originalCopy.retainAll(new TokenSet(token));
            unionCopy.retainAll(new TokenSet(token));
            if (unionCopy.size() > originalCopy.size()) { return false; }
        }
        return true;
    }
}