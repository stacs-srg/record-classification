package uk.ac.standrews.cs.digitising_scotland.linkage.resolve;

import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.BirthFamilyGT;
import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Marriage;
import uk.ac.standrews.cs.digitising_scotland.linkage.resolve.distances.GFNGLNBFNBMNPOMDOMDistanceOverMarriage;
import uk.ac.standrews.cs.digitising_scotland.util.MTree.DataDistance;
import uk.ac.standrews.cs.digitising_scotland.util.MTree.MTree;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.storr.impl.exceptions.StoreException;
import uk.ac.standrews.cs.storr.interfaces.IInputStream;

import java.io.IOException;
import java.util.List;

/**
 * Output on std out matches between births and marriages in a csv format
 * The output is the number of matches between births and marriages at edit distances 0,1,2.. RANGE_MAX
 * File is derived from KilmarnockLinker.
 * Created by al on 17/2/1017
 */
public class KilmarnockMTreeBirthMarriageRangeCSVGenerator extends KilmarnockMTreeMatcherGroundTruthChecker {

    private static final String[] ARG_NAMES = {"births_source_path", "deaths_source_path", "marriages_source_path"};

    final static int RANGE_MAX = 15;

    private MTree<Marriage> marriageMtree;

    private KilmarnockMTreeBirthMarriageRangeCSVGenerator() throws StoreException, IOException, RepositoryException {

    }

    private void compute() throws Exception {

        timedRun("Creating Marriage MTree", () -> {
            createMarriageMTreeOverGFNGLNBFNBMNPOMDOM();
            return null;
        });

        timedRun("Outputting matches", () -> {
            outputRangeSearchMatchesBetweenBirthsAndMarriages();
            return null;
        });
    }

    private void createMarriageMTreeOverGFNGLNBFNBMNPOMDOM() throws RepositoryException, BucketException, IOException {

        marriageMtree = new MTree<>(new GFNGLNBFNBMNPOMDOMDistanceOverMarriage());

        IInputStream<Marriage> stream = marriages.getInputStream();

        for (Marriage marriage : stream) {

            marriageMtree.add(marriage);
        }
    }

    /**
     * Output number of matches between births and marriages in csv format.
     */
    private void outputRangeSearchMatchesBetweenBirthsAndMarriages() {

        IInputStream<BirthFamilyGT> stream;
        try {
            stream = births.getInputStream();
        } catch (BucketException e) {
            System.out.println("Exception whilst getting births");
            return;
        }

        for (BirthFamilyGT b : stream) {

            Marriage marriage_query = new Marriage();
            marriage_query.put(Marriage.GROOM_FORENAME, b.getFathersForename());
            marriage_query.put(Marriage.GROOM_SURNAME, b.getFathersSurname());
            marriage_query.put(Marriage.BRIDE_FORENAME, b.getMothersForename());
            marriage_query.put(Marriage.BRIDE_SURNAME, b.getMothersMaidenSurname());
            marriage_query.put(Marriage.PLACE_OF_MARRIAGE, b.getPlaceOfMarriage());

            marriage_query.put(Marriage.MARRIAGE_DAY, b.getString(BirthFamilyGT.PARENTS_DAY_OF_MARRIAGE));
            marriage_query.put(Marriage.MARRIAGE_MONTH, b.getString(BirthFamilyGT.PARENTS_MONTH_OF_MARRIAGE));
            marriage_query.put(Marriage.MARRIAGE_YEAR, b.getString(BirthFamilyGT.PARENTS_YEAR_OF_MARRIAGE));

            for (int range = 0; range < RANGE_MAX; ) {

                List<DataDistance<Marriage>> results = marriageMtree.rangeSearch(marriage_query, range++);
                System.out.print(results.size());
                if (range != RANGE_MAX) {
                    System.out.print(",");
                }
            }
            System.out.println();
        }
    }

    //***********************************************************************************

    public static void main(String[] args) throws Exception {

        KilmarnockMTreeBirthMarriageRangeCSVGenerator matcher = new KilmarnockMTreeBirthMarriageRangeCSVGenerator();

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
