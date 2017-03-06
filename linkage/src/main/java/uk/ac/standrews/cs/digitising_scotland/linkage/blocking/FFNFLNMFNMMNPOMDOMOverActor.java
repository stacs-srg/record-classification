package uk.ac.standrews.cs.digitising_scotland.linkage.blocking;

import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.*;
import uk.ac.standrews.cs.digitising_scotland.util.*;
import uk.ac.standrews.cs.storr.impl.exceptions.*;
import uk.ac.standrews.cs.storr.interfaces.*;

import java.io.*;

/**
 * This class blocks on streams of Role records.
 * The categories of blocking are first name of parents along with place of marriage and date of marriage over streams of Role records
 * This should form family groups
 * This pattern was suggested at Raasay Colloquium by Eilidh.
 * These are unique tags for all vital event records.
 * Created by al on 30/8/16
 */

public class FFNFLNMFNMMNPOMDOMOverActor extends AbstractBlocker<Role> {

    public FFNFLNMFNMMNPOMDOMOverActor(final IBucket<Role> roleBucket, final IRepository output_repo, ILXPFactory<Role> tFactory) throws BucketException, RepositoryException, IOException {

        super(roleBucket.getInputStream(), output_repo, tFactory);
    }

    /**
     * @param record - a Person record to be blocked
     * @return the blocking keys - one for baby and one for FATHER
     */
    public String[] determineBlockedBucketNamesForRecord(final Role record) throws NoSuitableBucketException {

        switch (record.getRole()) {
            case PRINCIPAL:
                return determineBlockedBucketNamesForPrincipal(record);
            case FATHER:
                return determineBlockedBucketNamesForFather(record);
            case MOTHER:
                return determineBlockedBucketNamesForMother(record);
            case BRIDE:
                return determineBlockedBucketNamesForBrideOrGroom(record);
            case GROOM:
                return determineBlockedBucketNamesForBrideOrGroom(record);

            default:
                throw new NoSuitableBucketException("No match");
        }
    }

    /*
     * @param record - a Person record to be blocked who is in the role of PRINCIPAL
     * @return the blocking key based on FNLN of mother and FATHER and their place and date of marriage
     */
    public String[] determineBlockedBucketNamesForPrincipal(final Role record) throws NoSuitableBucketException {

        // Note will concat null strings into key if any fields are null - working hypothesis - this doesn't matter.

        try {
            final String normalised_father_forename = normaliseName(record.getFathersForename());
            final String normalised_father_surname = normaliseName(record.getFathersSurname());
            final String normalised_mother_forename = normaliseName(record.getMothersForename());
            final String normalised_mother_maiden_surname = normaliseName(record.getMothersMaidenSurname());

            String bucket_name = concatenate(normalised_father_forename, normalised_father_surname, normalised_mother_forename, normalised_mother_maiden_surname, record.getPlaceOfMarriage(), record.getDateOfMarriage());
            return new String[]{bucket_name};
        }
        catch (KeyNotFoundException e) {
            ErrorHandling.exceptionError(e, "Key not found");
            throw new NoSuitableBucketException(e);
        }
        catch (TypeMismatchFoundException e) {
            ErrorHandling.exceptionError(e, "Type mismatch");
            throw new NoSuitableBucketException(e);
        }
    }

    /*
     * @param record - a Person record to be blocked who is in the role of FATHER
     * @return the blocking key based on FNLN of mother and FATHER and their place and date of marriage
     */
    private String[] determineBlockedBucketNamesForFather(Role record) throws NoSuitableBucketException {

        // Note will concat null strings into key if any fields are null - working hypothesis - this doesn't matter.

        try {
            final String normalised_father_forename = normaliseName(record.getForename());
            final String normalised_father_surname = normaliseName(record.getSurname());

            // Get the mother's names from the original record.
            ILXP primary = record.getOriginalRecord();

            final String normalised_mother_forename = normaliseName((String) primary.get(KillieBirth.MOTHERS_FORENAME));
            final String normalised_mother_maiden_surname = normaliseName((String) primary.get(KillieBirth.MOTHERS_MAIDEN_SURNAME));

            String bucket_name = concatenate(normalised_father_forename, normalised_father_surname, normalised_mother_forename, normalised_mother_maiden_surname, record.getPlaceOfMarriage(), record.getDateOfMarriage());
            return new String[]{bucket_name};
        }
        catch (KeyNotFoundException e) {
            ErrorHandling.exceptionError(e, "Key not found");
            throw new NoSuitableBucketException(e);
        }
        catch (TypeMismatchFoundException e) {
            ErrorHandling.exceptionError(e, "Type mismatch");
            throw new NoSuitableBucketException(e);
        }
    }

    /*
     * @param record - a Person record to be blocked who is in the role of mother
     * @return the blocking key based on FNLN of mother and FATHER and their place and date of marriage
     */
    private String[] determineBlockedBucketNamesForMother(Role record) throws NoSuitableBucketException {

        // Note will concat null strings into key if any fields are null - working hypothesis - this doesn't matter.

        StringBuilder builder = new StringBuilder();

        try {
            // Get the father's names from the original record.
            ILXP primary = record.getOriginalRecord();

            final String normalised_father_forename = normaliseName((String) primary.get(KillieBirth.FATHERS_FORENAME));
            final String normalised_father_surname = normaliseName((String) primary.get(KillieBirth.FATHERS_SURNAME));

            final String normalised_mother_forename = normaliseName(record.getForename());
            final String normalised_mother_surname = normaliseName(record.getSurname());

            String bucket_name = concatenate(normalised_father_forename, normalised_father_surname, normalised_mother_forename, normalised_mother_surname, record.getPlaceOfMarriage(), record.getDateOfMarriage());
            return new String[]{bucket_name};
        }
        catch (KeyNotFoundException e) {
            ErrorHandling.exceptionError(e, "Key not found");
            throw new NoSuitableBucketException(e);
        }
        catch (TypeMismatchFoundException e) {
            ErrorHandling.exceptionError(e, "Type mismatch");
            throw new NoSuitableBucketException(e);
        }
    }

    /*
     * @param record - a Person record to be blocked who is in the role of bride or groom
     * @return the blocking key based on FNLN of bride and groom and their place and date of marriage
     */
    private String[] determineBlockedBucketNamesForBrideOrGroom(Role record) throws NoSuitableBucketException {

        // Note will concat null strings into key if any fields are null - working hypothesis - this doesn't matter.

        try {
            // Get the spouses' names from the original record.
            ILXP primary = record.getOriginalRecord();

            final String normalised_groom_forename = normaliseName((String) primary.get(Marriage.GROOM_FORENAME));
            final String normlaised_groom_surname = normaliseName((String) primary.get(Marriage.GROOM_SURNAME));

            final String normalised_bride_forename = normaliseName((String) primary.get(Marriage.BRIDE_FORENAME));
            final String normalised_bride_surname = normaliseName((String) primary.get(Marriage.BRIDE_SURNAME));

            String bucket_name = concatenate(normalised_groom_forename, normlaised_groom_surname, normalised_bride_forename, normalised_bride_surname, record.getPlaceOfMarriage(), record.getDateOfMarriage());
            return new String[]{bucket_name};
        }
        catch (KeyNotFoundException e) {
            ErrorHandling.exceptionError(e, "Key not found");
            throw new NoSuitableBucketException(e);
        }
        catch (TypeMismatchFoundException e) {
            ErrorHandling.exceptionError(e, "Type mismatch");
            throw new NoSuitableBucketException(e);
        }
    }
}

