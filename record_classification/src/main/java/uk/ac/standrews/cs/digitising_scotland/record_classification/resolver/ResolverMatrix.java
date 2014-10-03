package uk.ac.standrews.cs.digitising_scotland.record_classification.resolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.Pair;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.code.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.code.Code;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.tokens.TokenSet;

/**
 * ResolverMatrix stores mappings between {@link Code}s and the list of {@link Pair}<TokenSet, Double> objects
 * that were classified as that code.
 * <p>
 * It implements methods that allow  for the reduction in size of the lists for each code via removing overlapping
 * codes and low confidence codes.
 * <p>
 * It also implements the getValidCodeTriples() method that allows the user to find all Code->Pair<TokenSet, Double>
 * mappings that are valid with respect to a given power set.
 *
 * @author frjd2
 * @author jkc25
 *         Created by fraserdunlop on 10/06/2014 at 14:57.
 */
public class ResolverMatrix {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolverMatrix.class);

    private static final int KEYSET_SIZE_LIMIT = 30;

    private static final int COMPLEXITY_UPPERLIMIT = 2000;

    private static final int LOWER_BOUND = 1;

    private boolean multipleClassifications;
    /**
     * The Code, List<Pair> matrix.
     */
    private Map<Code, List<Classification>> matrix;

    /**
     * Instantiates a new empty resolver matrix.
     */
    public ResolverMatrix(final boolean multipleClassifications) {

        this.multipleClassifications = multipleClassifications;
        matrix = new HashMap<>();
    }

    /**
     * Adds a {@link TokenSet} and {@link Pair}<Code, Double> to the matrix.
     * <p>
     * The Pair<Code, Double> should represent the output of an {@link AbstractClassifier} where the Code is the
     * returned code and the Double represents the confidence of that classification.
     *
     * @param tokenSet       the tokenSet to add
     * @param codeDoublePair the Pair<Code, Double> to add
     */
    public void add(final TokenSet tokenSet, final Pair<Code, Double> codeDoublePair) {

        Code code = codeDoublePair.getLeft();
        Double confidence = codeDoublePair.getRight();
        if (matrix.get(code) == null) {
            matrix.put(code, new ArrayList<Classification>());
        }
        matrix.get(code).add(new Classification(code, tokenSet, confidence));
    }

    /**
     * Gets the valid sets of {@link Classification}s from the matrix in the form of a List.
     * <p>
     * A {@link Classification} is defined as being valid if the union of the Set of TokenSets is a subset of the specified powerSet.
     * Any null entries in the matrix are not returned.
     *
     * @param originalSet the power set of valid tokenSets
     * @return List<Set<CodeTriple>> the List of Sets of valid {@link Classification}s
     */
    public List<Set<Classification>> getValidCodeTriples(final TokenSet originalSet) {

        chopUntilComplexityWithinBound(COMPLEXITY_UPPERLIMIT);

        resolveHierarchies();
        List<Set<Classification>> merged = new ArrayList<>();
        merged.add(null);

        if (matrix.keySet().size() > KEYSET_SIZE_LIMIT) {
            //TODO quick hack to test theory
            LOGGER.info("codeList too big - skipping");
        }
        else {
            //TODO refactor as a proper recursive functions
            for (Code code : matrix.keySet()) {
                merge(merged, matrix.get(code), code, originalSet);
            }
        }
        merged.remove(null);
        return merged;
    }

    /**
     * Calculates a numerical value for the complexity of the matrix.
     * This value is calculated by multiplying the size of each list by the size of every other list.
     * For example, a Map with 3 different codes, with lists of length 4, 5 and 6 would result in a complexity of
     * 120 being returned (4*5*6).
     *
     * @return the int numerical representation of the complexity of the matrix.
     */
    public long complexity() {

        long complexity = 1;
        for (Code code : matrix.keySet()) {
            if (!matrix.get(code).isEmpty()) {
                complexity = complexity * matrix.get(code).size();
            }
        }
        if (complexity < 1) {
            complexity = Integer.MAX_VALUE;
        }
        return complexity;
    }

    /**
     * Resolves hierarchies in the matrix by removing the ancestors of {@link Code}s in the matrix.
     * This utilises the ResolverUtils.removeAncestors() method to achieve this.
     */
    protected void resolveHierarchies() {

        Set<Code> keySet = new HashSet<>();
        keySet.addAll(matrix.keySet());

        if (multipleClassifications) {

            for (Code code : keySet) {
                Code ancestor = ResolverUtils.whichCodeIsAncestorOfCodeInSet(code, keySet);
                if (ancestor != null) {
                    matrix.get(code).addAll(matrix.get(ancestor));
                    matrix.remove(ancestor);
                }
            }
        }
        else {
            Map<Code, List<Classification>> singleColumnMatri = new HashMap<>();
            // pick any code here as it's not used in single classifications.
            Code c = matrix.keySet().iterator().next();
            singleColumnMatri.put(c, new ArrayList<Classification>());
            for (Code code : keySet) {
                singleColumnMatri.get(c).addAll(matrix.get(code));
            }
            matrix = singleColumnMatri;

        }
    }

    private void mergeForMutlipleClassifications(Set<Code> keySet) {

    }

    /**
     * Helper method used to enumerate all values in the matrix.
     *
     * @param merged      the merged
     * @param codeTriples the pairs
     * @param code        the code
     */
    private void merge(final List<Set<Classification>> merged, final List<Classification> codeTriples, final Code code, final TokenSet originalSet) {

        List<Set<Classification>> temporaryMerge = new ArrayList<>();
        for (Set<Classification> tripleSet : merged) {
            for (Classification triple : codeTriples) {
                Classification tempCodeTriple = new Classification(code, triple.getTokenSet(), triple.getConfidence());
                Set<Classification> tempTripleSet = new HashSet<>();
                if (tripleSet != null) {
                    tempTripleSet.addAll(tripleSet);
                }
                tempTripleSet.add(tempCodeTriple);
                if (ResolverUtils.tripleSetIsValid(tempTripleSet, originalSet)) {
                    temporaryMerge.add(tempTripleSet);
                }
            }
        }
        merged.addAll(temporaryMerge);
    }

    /**
     * Chop until complexity within bound.
     *
     * @param bound the bound
     */
    public void chopUntilComplexityWithinBound(final int bound) {

        int maxNoOfEachCode = (int) Math.pow(bound, 1.0 / (double) matrix.keySet().size());
        maxNoOfEachCode = Math.max(LOWER_BOUND, maxNoOfEachCode);
        for (Code code : matrix.keySet()) {
            Collections.sort(matrix.get(code), new CodeTripleComparator());
            matrix.put(code, matrix.get(code).subList(0, Math.min(matrix.get(code).size(), maxNoOfEachCode)));
        }
    }

    private static class CodeTripleComparator implements Comparator<Classification>, Serializable {

        private static final long serialVersionUID = -2746182512036694544L;

        @Override
        public int compare(final Classification o1, final Classification o2) {

            double measure1 = o1.getTokenSet().size() * o1.getConfidence();
            double measure2 = o2.getTokenSet().size() * o2.getConfidence();
            if (measure1 < measure2) {
                return 1;
            }
            else if (measure1 > measure2) {
                return -1;
            }
            else {
                return 0;
            }
        }
    }

    /**
     * Removes from the matrix any Pair<Code, Double> where the confidence value is lower than the confidence specified.
     *
     * @param confidence the confidence threshold
     */
    public void chopBelowConfidence(final Double confidence) {

        for (Code code : matrix.keySet()) {
            List<Classification> oldList = matrix.get(code);
            List<Classification> newList = new ArrayList<>();
            for (Classification triple : oldList) {
                if (triple.getConfidence() >= confidence) {
                    newList.add(triple);
                }
            }
            matrix.put(code, newList);
        }
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((matrix == null) ? 0 : matrix.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        ResolverMatrix other = (ResolverMatrix) obj;
        if (matrix == null) {
            if (other.matrix != null) { return false; }
        }
        else if (!matrix.equals(other.matrix)) { return false; }
        return true;
    }

    @Override
    public String toString() {

        return "ResolverMatrix [matrix=" + matrix + "]";
    }

}
