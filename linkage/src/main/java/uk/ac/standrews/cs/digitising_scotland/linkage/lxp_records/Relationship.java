package uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records;

import uk.ac.standrews.cs.digitising_scotland.jstore.impl.exceptions.IllegalKeyException;
import uk.ac.standrews.cs.digitising_scotland.jstore.impl.exceptions.StoreException;
import uk.ac.standrews.cs.nds.persistence.PersistentObjectException;
import uk.ac.standrews.cs.nds.rpc.stream.JSONReader;

/**
 * Created by al on 03/10/2014.
 */
public class Relationship extends AbstractLXP {

    public Relationship() throws StoreException {
        super();
    }

    public Relationship(long label_id, JSONReader reader) throws PersistentObjectException, IllegalKeyException, StoreException {

        super(reader);

    }


}
