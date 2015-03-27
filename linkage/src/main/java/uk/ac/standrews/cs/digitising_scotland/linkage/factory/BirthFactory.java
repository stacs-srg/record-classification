package uk.ac.standrews.cs.digitising_scotland.linkage.factory;


import uk.ac.standrews.cs.jstore.impl.exceptions.IllegalKeyException;
import uk.ac.standrews.cs.jstore.interfaces.ILXPFactory;
import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Birth;
import uk.ac.standrews.cs.nds.persistence.PersistentObjectException;
import uk.ac.standrews.cs.nds.rpc.stream.JSONReader;

/**
 * Created by al on 03/10/2014.
 */
public class BirthFactory extends TFactory<Birth> implements ILXPFactory<Birth> {


    public BirthFactory(long required_label_id) {
        this.required_type_labelID = required_label_id;
    }

    @Override
    public Birth create(long persistent_object_id, JSONReader reader) throws PersistentObjectException, IllegalKeyException {
        return new Birth(persistent_object_id, reader);
    }


}
