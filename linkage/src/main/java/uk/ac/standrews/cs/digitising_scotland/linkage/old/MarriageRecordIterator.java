package uk.ac.standrews.cs.digitising_scotland.linkage.old;

import uk.ac.standrews.cs.digitising_scotland.linkage.event_records.MarriageRecord;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.database.old.PartnershipIterator;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

import java.sql.SQLException;

/**
 * Created by graham on 28/05/2014.
 */
public class MarriageRecordIterator extends RecordIterator2<MarriageRecord> {

    private PartnershipIterator partnership_iterator;

    public MarriageRecordIterator() {

        try {
            partnership_iterator = new PartnershipIterator();
            size = partnership_iterator.size();

        } catch (SQLException e) {
            ErrorHandling.exceptionError(e, "Could not initialise iterator over partnerships.");
        }
    }

    @Override
    public boolean hasNext() {

        return partnership_iterator.hasNext();
    }

    @Override
    public MarriageRecord next() {

        return new MarriageRecord(null, null);
    }

    public void close() {

        try {
            partnership_iterator.close();

        } catch (SQLException e) {
            ErrorHandling.exceptionError(e, "Could not close iterator over partnerships.");
        }
    }
}