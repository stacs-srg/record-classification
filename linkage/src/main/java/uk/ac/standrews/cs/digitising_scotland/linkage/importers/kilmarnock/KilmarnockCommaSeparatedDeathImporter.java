package uk.ac.standrews.cs.digitising_scotland.linkage.importers.kilmarnock;

import uk.ac.standrews.cs.digitising_scotland.linkage.importers.RecordFormatException;
import uk.ac.standrews.cs.digitising_scotland.linkage.importers.commaSeparated.CommaSeparatedDeathImporter;
import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Death;
import uk.ac.standrews.cs.digitising_scotland.linkage.normalisation.DateNormalisation;
import uk.ac.standrews.cs.storr.impl.exceptions.BucketException;
import uk.ac.standrews.cs.storr.impl.exceptions.IllegalKeyException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.util.dataset.DataSet;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Death.*;


/**
 * Utility classes for importing records in digitising scotland format
 * Created by al on 8/11/2016.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class KilmarnockCommaSeparatedDeathImporter extends CommaSeparatedDeathImporter {

    private static final String[][] RECORD_LABEL_MAP = {

            // Information available that doesn't currently fit:

            // "day of reg", "month of reg", "year of reg"

            {ORIGINAL_ID, "ID"},

            {YEAR_OF_REGISTRATION, "year of reg"},

            {REGISTRATION_DISTRICT_NUMBER, "identifier"},

            {REGISTRATION_DISTRICT_SUFFIX, "register identifier"},

            {ENTRY, "entry no"},

            // *********************************

            {FORENAME, "forename(s) of deceased"}, {SURNAME, "surname of deceased"},

            {SEX, "sex"},

            // *********************************

            {DEATH_YEAR, "year"}, {DEATH_DAY, "day"},

            {AGE_AT_DEATH, "age at death"},

            // *********************************

            {MOTHERS_FORENAME, "mother's forename"},

            {MOTHERS_MAIDEN_SURNAME, "mother's maiden surname"},

            {MOTHER_DECEASED, "if mother deceased"},

            // *********************************

            {FATHERS_FORENAME, "father's forename"},

            {FATHERS_SURNAME, "father's surname"},

            {FATHERS_OCCUPATION, "father's occupation"},

            {FATHER_DECEASED, "if father deceased"},

            // *********************************

            {OCCUPATION, "occupation"},

            {MARITAL_STATUS, "marital status"},

            {SPOUSES_OCCUPATIONS, "spouse's occ"},

            {COD_A, "cause of death"}
    };

    private static final String[] UNAVAILABLE_RECORD_LABELS = {

            // Fields not present in Kilmarnock dataset.

            CHANGED_FORENAME, CHANGED_SURNAME, CHANGED_MOTHERS_MAIDEN_SURNAME, CORRECTED_ENTRY, IMAGE_QUALITY, CHANGED_DEATH_AGE, COD_B, COD_C, PLACE_OF_DEATH, DATE_OF_BIRTH, CERTIFYING_DOCTOR, MOTHERS_SURNAME
    };


    public String[][] get_record_map() { return RECORD_LABEL_MAP; }

    public String[] get_unavailable_records() { return UNAVAILABLE_RECORD_LABELS; }

    /**
     * @param deaths   the bucket from which to import
     * @param deaths_source_path containing the source records in digitising scotland format
     * @return the number of records read in
     * @throws IOException
     * @throws RecordFormatException
     * @throws BucketException
     */
    public  int importDigitisingScotlandDeaths(IBucket<Death> deaths, String deaths_source_path) throws IOException, RecordFormatException, BucketException {

        DataSet data = new DataSet(Paths.get(deaths_source_path));
        int count = 0;

        for (List<String> record : data.getRecords()) {

            Death death_record = importDigitisingScotlandDeath(data, record);
            deaths.makePersistent(death_record);
            count++;

        }

        return count;
    }

    /**
     * Fills in a record.
     */
    private Death importDigitisingScotlandDeath(DataSet data, List<String> record) throws IOException, RecordFormatException, IllegalKeyException {

        Death death = new Death();

        addAvailableSingleFields(data, record, death, RECORD_LABEL_MAP);
        addAvailableCompoundFields(data, record, death);
        addAvailableNormalisedFields(data, record, death);
        addUnavailableFields(death, UNAVAILABLE_RECORD_LABELS);

        return death;
    }

    public void addAvailableCompoundFields(final DataSet data, final List<String> record, final Death death) {

        death.put(SPOUSES_NAMES, combineFields(data, record, "forename of spouse", "surname of spouse"));
        death.put(PLACE_OF_DEATH, combineFields(data, record, "address 1", "address 2", "address 3"));
    }

    public void addAvailableNormalisedFields(DataSet data, List<String> record, Death death) {

        death.put(DEATH_MONTH, DateNormalisation.normaliseMonth(data.getValue(record, "month")));
    }
}