package uk.ac.standrews.cs.digitising_scotland.linkage.factory;


import uk.ac.standrews.cs.jstore.impl.exceptions.IllegalKeyException;
import uk.ac.standrews.cs.jstore.interfaces.ILXPFactory;
import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Death;
import uk.ac.standrews.cs.nds.persistence.PersistentObjectException;
import uk.ac.standrews.cs.nds.rpc.stream.JSONReader;

/**
 * Created by al on 03/10/2014.
 */
public class DeathFactory extends TFactory<Death> implements ILXPFactory<Death> {


    public DeathFactory(long deathlabelID) {
        this.required_type_labelID = deathlabelID;
    }


    @Override
    public Death create(long persistent_object_id, JSONReader reader) throws PersistentObjectException, IllegalKeyException {
        return new Death(persistent_object_id, reader);
    }

}
