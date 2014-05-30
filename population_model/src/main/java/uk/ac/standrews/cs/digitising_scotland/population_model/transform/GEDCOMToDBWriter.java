package uk.ac.standrews.cs.digitising_scotland.population_model.transform;

import org.gedcom4j.model.Family;
import org.gedcom4j.model.FamilyEvent;
import org.gedcom4j.model.FamilyEventType;
import org.gedcom4j.model.Individual;
import org.gedcom4j.parser.GedcomParser;
import org.gedcom4j.parser.GedcomParserException;
import uk.ac.standrews.cs.digitising_scotland.util.DateManipulation;
import uk.ac.standrews.cs.digitising_scotland.population_model.config.PopulationProperties;
import uk.ac.standrews.cs.digitising_scotland.population_model.database.DBConnector;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.Person;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.PersonFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reads in a GEDCOM file and populates a database.
 *
 * GEDCOM specification: http://homepages.rootsweb.ancestry.com/~pmcbride/gedcom/55gctoc.htm
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class GEDCOMToDBWriter implements AutoCloseable {

    private final Connection connection;
    private final GedcomParser parser;
    private final PersonFactory person_factory;

    public GEDCOMToDBWriter(final String path_string) throws IOException, GedcomParserException, SQLException {

        connection = new DBConnector(PopulationProperties.DATABASE_NAME).createConnection();
        person_factory = new PersonFactory();

        parser = new GedcomParser();
        parser.load(path_string);
    }

    public int importPeople() throws ParseException, SQLException {

        final Collection<Individual> individuals = parser.gedcom.individuals.values();
        for (final Individual individual : individuals) {

            importPerson(person_factory.createPerson(individual));
        }
        return individuals.size();
    }

    public int importFamilies() throws ParseException, SQLException {

        final Collection<Family> families = parser.gedcom.families.values();
        for (final Family family : families) {

            importFamily(family);
        }
        return families.size();
    }

    public void close() throws SQLException {

        connection.close();
    }

    private void importFamily(final Family family) throws ParseException, SQLException {

        final int familyXref = Integer.valueOf(stripAtSymbols(family.xref));
        final int husbandXref = Integer.valueOf(stripAtSymbols(family.husband.xref));
        final int wifeXref = Integer.valueOf(stripAtSymbols(family.wife.xref));

        String marriage_date_string = "";
        for (final FamilyEvent event : family.events) {
            if (event.type == FamilyEventType.MARRIAGE) {
                marriage_date_string = event.date.toString();
            }
        }

        final Date marriage_date = DateManipulation.stringSQLToDate(marriage_date_string);

        final List<Integer> childXrefs = new ArrayList<Integer>();
        for (final Individual child : family.children) {
            childXrefs.add(Integer.valueOf(stripAtSymbols(child.xref)));
        }

        importFamily(familyXref, marriage_date, husbandXref, wifeXref, childXrefs);
    }

    private void importPerson(final Person fp) throws SQLException {

        try (final PreparedStatement statement = connection.prepareStatement("INSERT INTO " + PopulationProperties.PERSON_TABLE_NAME + " VALUES( ?,?,?,?,?,?,?,?,? );")) {

            statement.setInt(1, fp.getID());
            statement.setString(2, String.valueOf(fp.getGender()));
            statement.setString(3, fp.getFirstName());
            statement.setString(4, fp.getSurname());
            statement.setDate(5, fp.getBirthDate());
            statement.setDate(6, fp.getDeathDate());
            statement.setString(7, fp.getOccupation());
            statement.setString(8, fp.getCauseOfDeath());
            statement.setString(9, fp.getAddress());

            statement.executeUpdate();
        }
    }

    private void importFamily(final int family_id, final Date marriage_date, final int husband_id, final int wife_id, final List<Integer> child_ids) throws SQLException {

        insertPartnership(family_id, marriage_date);
        insertPartner(family_id, husband_id);
        insertPartner(family_id, wife_id);

        for (final Integer childID : child_ids) {
            insertChild(family_id, childID);
        }
    }

    private void insertPartnership(final int family_id, final Date marriage_date) throws SQLException {

        // TODO factor out prepared statement
        try (final PreparedStatement statement = connection.prepareStatement("INSERT INTO " + PopulationProperties.PARTNERSHIP_TABLE_NAME + " VALUES( ?,? );")) {

            statement.setInt(1, family_id);
            statement.setDate(2, marriage_date);
            statement.executeUpdate();
        }
    }

    private void insertPartner(final int family_id, final int partner_id) throws SQLException {

        // TODO factor out prepared statement
        try (final PreparedStatement statement = connection.prepareStatement("INSERT INTO " + PopulationProperties.PARTNERSHIP_PARTNER_TABLE_NAME + " VALUES( ?,? );")) {

            statement.setInt(1, partner_id);
            statement.setInt(2, family_id);
            statement.executeUpdate();
        }
    }

    private void insertChild(final int family_id, final int child_id) throws SQLException {

        // TODO factor out prepared statement
        try (final PreparedStatement statement = connection.prepareStatement("INSERT INTO " + PopulationProperties.PARTNERSHIP_CHILD_TABLE_NAME + " VALUES( ?,? );")) {

            statement.setInt(1, child_id);
            statement.setInt(2, family_id);
            statement.executeUpdate();
        }
    }

    private String stripAtSymbols(final String reference) {

        return reference.substring(1, reference.length() - 1);
    }
}
