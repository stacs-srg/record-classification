package uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces;

import uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces.ILXPOutputStream;

/**
 * Created by al on 21/05/2014.
 */
public interface IPairWiseLinker {

    void pairwiseLink();

    boolean compare(IPair pair);

    /**
     * Adds a matched result to a result collection.
     * @param pair
     */
    void addToResults(final IPair pair, final ILXPOutputStream results);
}