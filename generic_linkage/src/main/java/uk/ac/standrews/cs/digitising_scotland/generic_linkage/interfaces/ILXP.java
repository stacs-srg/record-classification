package uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces;

import org.json.JSONException;
import org.json.JSONWriter;

import java.util.Map;

/**
 * Interface to the LXP (labeled cross product class).
 * It provides a thin wrapper over a Map (providing name value lookup) along with identity and the ability to save and recover persistent versions (encoded in JSON).
 *
 * @author al
 */
public interface ILXP extends Map<String, String> {

    /**
     * @return the id of the record
     */
    int getId();

    /**
     * Writes the state of the LXP to a Bucket.
     *
     * @param writer the stream to which the state is written.
     */
    void serializeToJSON(JSONWriter writer) throws JSONException;

    void put( String key, int value );
}
