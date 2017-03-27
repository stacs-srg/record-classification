package uk.ac.standrews.cs.digitising_scotland.linkage.resolve.repo_based;

import uk.ac.standrews.cs.digitising_scotland.linkage.experiments.Experiment;
import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.BirthFamilyGT;
import uk.ac.standrews.cs.digitising_scotland.linkage.resolve.Family;
import uk.ac.standrews.cs.digitising_scotland.linkage.resolve.distances.GFNGLNBFNBMNPOMDOMDistanceOverBirth;
import uk.ac.standrews.cs.digitising_scotland.util.MTree.DataDistance;
import uk.ac.standrews.cs.digitising_scotland.util.MTree.MTree;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.storr.impl.exceptions.StoreException;
import uk.ac.standrews.cs.util.tools.PercentageProgressIndicator;
import uk.ac.standrews.cs.util.tools.ProgressIndicator;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Attempt to perform linking using MTree matching
 * File is derived from KilmarnockLinker.
 * Created by al on 17/2/1017
 */
public class MTreeBirthBirthThresholdNNGroundTruthChecker extends BDMExperiment {

    public static final String[] ARG_NAMES = {"store_path","repo_name","family_distance_threshold"};
    private MTree<BirthFamilyGT> birth_MTree;
    private float match_family_distance_threshold;

    MTreeBirthBirthThresholdNNGroundTruthChecker(String store_path, String repo_name, float match_family_distance_threshold) throws StoreException, IOException, RepositoryException {

        super(store_path,repo_name);
        this.match_family_distance_threshold = match_family_distance_threshold;
    }

//    KilmarnockMTreeBirthBirthThresholdNNGroundTruthChecker() throws StoreException, IOException, RepositoryException {
//
//    }

    private void compute() throws Exception {

        timedRun("Creating Birth MTree", () -> {
            createBirthMTreeOverGFNGLNBFNBMNPOMDOM();
            return null;
        });

        timedRun("Forming families from Birth-Birth links", () -> {
            formFamilies();
            return null;
        });

        printFamilies();

        timedRun("Calculating linkage stats", () -> {
            printLinkageStats();
            return null;
        });

        System.out.println("Finished");
    }

    void createBirthMTreeOverGFNGLNBFNBMNPOMDOM() throws RepositoryException, BucketException, IOException {

        ProgressIndicator indicator = new PercentageProgressIndicator(10);
        indicator.setTotalSteps(getBirthsCount());

        birth_MTree = new MTree<>(new GFNGLNBFNBMNPOMDOMDistanceOverBirth());

        for (BirthFamilyGT birth : births.getInputStream()) {

            birth_MTree.add(birth);
            indicator.progressStep();
        }
    }

    /**
     * Try and form families from Birth M Tree data_array
     */
    void formFamilies() throws BucketException {

        ProgressIndicator indicator = new PercentageProgressIndicator(10);
        indicator.setTotalSteps(getBirthsCount());

        for (BirthFamilyGT to_match : births.getInputStream()) {

            DataDistance<BirthFamilyGT> matched = birth_MTree.nearestNeighbour(to_match);

            if (matched.distance < match_family_distance_threshold && matched.value != to_match) {
                addBirthsToMap(to_match, matched);
            }
            indicator.progressStep();
        }
    }

    /**
     * Adds a birth record to a family map.
     *
     * @param searched the record that was used to search for a match
     * @param found_dd the data distance that was matched in the search
     */
    private void addBirthsToMap(BirthFamilyGT searched, DataDistance<BirthFamilyGT> found_dd) {

        BirthFamilyGT found = found_dd.value;

        long searched_key = searched.getId();
        long found_key = found.getId();

        if (!person_to_family_map.containsKey(searched_key) && !person_to_family_map.containsKey(found_key)) {

            // Not seen either birth before.
            // Create a new Family and add to map under both keys.
            Family new_family = new Family(searched);
            new_family.siblings.add(found);
            person_to_family_map.put(searched_key, new_family);
            person_to_family_map.put(found_key, new_family);
            return;
        }

        // Don't bother with whether these are the same family or not, or if the added values are already in the set
        // Set implementation should deal with this.
        if (person_to_family_map.containsKey(searched_key) && !person_to_family_map.containsKey(found_key)) {

            // Already seen the searched birth => been found already
            Family f = person_to_family_map.get(searched_key);
            f.siblings.add(found);
        }

        if (person_to_family_map.containsKey(found_key) && !person_to_family_map.containsKey(searched_key)) {

            // Already seen the found birth => been searched for earlier
            Family f = person_to_family_map.get(found_key);
            f.siblings.add(searched);
        }
    }

    //***********************************************************************************

    public static void main(String[] args) throws Exception {

        Experiment experiment = new Experiment(ARG_NAMES, args, MethodHandles.lookup().lookupClass());

        if (args.length >= ARG_NAMES.length) {

            String store_path = args[0];
            String repo_name = args[1];
            String family_distance_threshold_string = args[2];

            MTreeBirthBirthThresholdNNGroundTruthChecker matcher = new MTreeBirthBirthThresholdNNGroundTruthChecker(store_path, repo_name, Float.parseFloat(family_distance_threshold_string));

            experiment.printDescription();

            matcher.compute();

        } else {
            experiment.usage();
        }
    }
}