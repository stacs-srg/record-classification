package uk.ac.standrews.cs.digitising_scotland.linkage.resolve;

import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.BirthFamilyGT;
import uk.ac.standrews.cs.digitising_scotland.linkage.resolve.distances.GFNGLNBFNBMNPOMDOMDistanceOverBirth;
import uk.ac.standrews.cs.digitising_scotland.util.MTree.DataDistance;
import uk.ac.standrews.cs.digitising_scotland.util.MTree.MTree;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.storr.impl.exceptions.StoreException;
import uk.ac.standrews.cs.storr.interfaces.IInputStream;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Attempt to perform linking using MTree matching
 * File is derived from KilmarnockLinker.
 * Created by al on 17/2/1017
 */
public class KilmarnockMTreeBirthBirthWithinDistanceGroundTruthChecker extends KilmarnockMTreeMatcherGroundTruthChecker {

    public static final String[] ARG_NAMES = {"births_source_path", "deaths_source_path", "marriages_source_path"};
    private MTree<BirthFamilyGT> birthMTree;

    public KilmarnockMTreeBirthBirthWithinDistanceGroundTruthChecker() throws StoreException, IOException, RepositoryException {
    }

    private void compute() throws Exception {

        timedRun("Creating Birth MTree", () -> {
            createBirthMTreeOverGFNGLNBFNBMNPOMDOM();
            return null;
        });

        timedRun("Forming families from Birth-Birth links", () -> {
            formFamilies();
            listFamilies();
            return null;
        });

        timedRun("Calculating linkage stats", () -> {
            calculateLinkageStats();
            return null;
        });
    }

    private void createBirthMTreeOverGFNGLNBFNBMNPOMDOM() throws RepositoryException, BucketException, IOException {

        System.out.println("Creating M Tree of births by GFNGLNBFNBMNPOMDOMDistanceOverBirth...");

        birthMTree = new MTree<BirthFamilyGT>(new GFNGLNBFNBMNPOMDOMDistanceOverBirth());

        IInputStream<BirthFamilyGT> stream = births.getInputStream();

        for (BirthFamilyGT birth : stream) {

            birthMTree.add(birth);
        }
    }

    /**
     * Try and form families from Birth M Tree data_array
     */
    private void formFamilies() {

        IInputStream<BirthFamilyGT> stream;
        try {
            stream = births.getInputStream();
        } catch (BucketException e) {
            System.out.println("Exception whilst getting births");
            return;
        }

        for (BirthFamilyGT b : stream) {

            // Calculate the neighbours of b, including b which is found in the rangeSearch
            List<DataDistance<BirthFamilyGT>> bsNeighbours = birthMTree.rangeSearch(b, 10);  // pronounced b's neighbours.

            // bs_neighbours_families is the set of families of neighbours that are different from bsFamily
            Set<Family> bs_neighbours_families = new TreeSet<Family>();

            Family bsFamily = families.get(b.getId()); // maybe null - is this right????

            // Add all of the families from bsNeighbours to bs_neighbours_families
            for (DataDistance<BirthFamilyGT> dd_to_bs_neighbour : bsNeighbours) {
                BirthFamilyGT bsNeighbour = dd_to_bs_neighbour.value;
                Family bs_neighbours_family = families.get(bsNeighbour.getId());
                if (bs_neighbours_family != null && bs_neighbours_family != bsFamily) {
                    bs_neighbours_families.add(bs_neighbours_family);
                }
            }

            Family thisFamily;

            if (bs_neighbours_families.size() == 1) { // just bsFamily in the set?
                // there are no "competing" family ids for this group of people
                // their id can remain the same

                thisFamily = bs_neighbours_families.iterator().next();

            } else {
                // there are (zero or) multiple "competing" family ids for this group of people
                // let's merge them
                thisFamily = new Family(b);
            }

            //  make all of bsNeighbours be in thisFamily
            for (DataDistance<BirthFamilyGT> dd : bsNeighbours) {
                BirthFamilyGT person = dd.value;
                families.put(person.getId(), thisFamily);
            }

            // if a person was previously in a different family, we merge them into thisFamily
            for (Family bs_neighbours_familiy : bs_neighbours_families) {
                for (BirthFamilyGT sibling : bs_neighbours_familiy.siblings) {

                    if (families.containsKey(sibling.getId())) {
                        families.put(sibling.getId(), thisFamily); //  replace person's family with the new one.
                    }
                }
            }
        }
    }

    //***********************************************************************************

    public static void main(String[] args) throws Exception {

        KilmarnockMTreeBirthBirthWithinDistanceGroundTruthChecker matcher = new KilmarnockMTreeBirthBirthWithinDistanceGroundTruthChecker();

        if (args.length >= ARG_NAMES.length) {

            String births_source_path = args[0];
            String deaths_source_path = args[1];
            String marriages_source_path = args[2];


            matcher.printDescription(args);
            matcher.ingestBDMRecords(births_source_path, deaths_source_path, marriages_source_path);
            matcher.compute();
        } else {
            matcher.usage();

        }
    }

    protected String[] getArgNames() {

        return ARG_NAMES;
    }
}
