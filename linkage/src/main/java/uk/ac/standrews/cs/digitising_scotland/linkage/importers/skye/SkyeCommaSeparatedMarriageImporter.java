package uk.ac.standrews.cs.digitising_scotland.linkage.importers.skye;

import uk.ac.standrews.cs.digitising_scotland.linkage.importers.commaSeparated.CommaSeparatedMarriageImporter;
import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Marriage;
import uk.ac.standrews.cs.digitising_scotland.linkage.normalisation.DateNormalisation;
import uk.ac.standrews.cs.util.dataset.DataSet;

import java.util.List;

import static uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Marriage.*;

/**
 * Utility classes for importing records in digitising scotland format
 * Created by al on 21/3/2017.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class SkyeCommaSeparatedMarriageImporter extends CommaSeparatedMarriageImporter {

    public static final String[][] RECORD_LABEL_MAP = {

            // Information available that doesn't currently fit:

            // "groom's mother's occ"
            // "bride's mother's occ"
            // "groom's mother's other names"
            // "bride's mother's other name/s"

            {ORIGINAL_ID, "ID"},

            {YEAR_OF_REGISTRATION, "IOS_YrofReg"},

            {REGISTRATION_DISTRICT_NUMBER, "rd identifier"},

            {REGISTRATION_DISTRICT_SUFFIX, "register identifier"},

            {ENTRY, "entry number"},

            // *********************************

            {BRIDE_FORENAME, "forename of bride"}, {BRIDE_SURNAME, "surname of bride"},

            // *********************************

            {GROOM_FORENAME, "forename of groom"}, {GROOM_SURNAME, "surname of groom"},

            // *********************************

            {MARRIAGE_YEAR, "year"}, {MARRIAGE_DAY, "day"},

            // *********************************

            {BRIDE_AGE_OR_DATE_OF_BIRTH, "age of bride"}, {GROOM_AGE_OR_DATE_OF_BIRTH, "age of groom"},

            // *********************************

            {BRIDE_FATHERS_FORENAME, "bride's father's forename"},

            {BRIDE_FATHERS_SURNAME, "bride's father's surname"},

            {BRIDE_MOTHERS_FORENAME, "bride's mother's forename"},

            {BRIDE_MOTHERS_MAIDEN_SURNAME, "bride's mother's maiden surname"},

            // *********************************

            {GROOM_FATHERS_FORENAME, "groom's father's forename"},

            {GROOM_FATHERS_SURNAME, "groom's father's surname"},

            {GROOM_MOTHERS_FORENAME, "groom's mother's forename"},

            {GROOM_MOTHERS_MAIDEN_SURNAME, "groom's mother's maiden surname"},

            // *********************************

            {BRIDE_MARITAL_STATUS, "marital status of bride"},

            {BRIDE_DID_NOT_SIGN, "did bride sign?"},

            {BRIDE_OCCUPATION, "occupation of bride"},

            {BRIDE_FATHER_OCCUPATION, "bride's father's occupation"},

            {BRIDE_FATHER_DECEASED, "if bride's father deceased"},

            {BRIDE_MOTHER_DECEASED, "if bride's mother deceased"},

            // *********************************

            {GROOM_MARITAL_STATUS, "marital status of groom"},

            {GROOM_DID_NOT_SIGN, "did groom sign?"},

            {GROOM_OCCUPATION, "occupation of groom"},

            {GROOM_FATHERS_OCCUPATION, "groom's father's occupation"},

            {GROOM_FATHER_DECEASED, "if groom's father deceased"},

            {GROOM_MOTHER_DECEASED, "if groom's mother deceased"},

    };

    public static final String[] UNAVAILABLE_RECORD_LABELS = {

            // Fields not present in Skye dataset.

            DENOMINATION, CHANGED_GROOM_FORENAME, IMAGE_QUALITY, CHANGED_GROOM_SURNAME, CHANGED_BRIDE_SURNAME, CORRECTED_ENTRY, CHANGED_BRIDE_FORENAME
    };

    public String[][] get_record_map() {
        return RECORD_LABEL_MAP;
    }

    public String[] get_unavailable_records() {
        return UNAVAILABLE_RECORD_LABELS;
    }

    public void addAvailableNormalisedFields(DataSet data, List<String> record, Marriage marriage) {

        marriage.put(MARRIAGE_MONTH, DateNormalisation.normaliseMonth(data.getValue(record, "month")));
    }

    public void addAvailableCompoundFields(final DataSet data, final List<String> record, final Marriage marriage) {

        marriage.put(BRIDE_ADDRESS, combineFields(data, record, "address of bride 1", "address of bride 2"));
        marriage.put(GROOM_ADDRESS, combineFields(data, record, "address of groom 1", "address of groom 2"));
        marriage.put(PLACE_OF_MARRIAGE, combineFields(data, record, "place of marriage 1", "place of marriage 2"));
    }
}
