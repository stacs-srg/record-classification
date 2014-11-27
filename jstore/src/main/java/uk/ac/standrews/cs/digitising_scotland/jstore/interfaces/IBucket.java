package uk.ac.standrews.cs.digitising_scotland.jstore.interfaces;

import uk.ac.standrews.cs.digitising_scotland.jstore.impl.exceptions.BucketException;

import java.io.IOException;

/**
 * The interface for a Bucket (a repository of LXP records).
 * Each record in the repository is identified by id.
 */
public interface IBucket<T extends ILXP> {

    /**
     * @param id - the identifier of the LXP record for which a reader is required.
     * @return an LXP record with the specified id, or null if the record cannot be found
     */
    T get(int id) throws BucketException;

    /**
     * Writes the state of a record to a bucket
     *
     * @param record whose state is to be written
     */
    void put(T record) throws BucketException;

    /**
     * @param id
     * @return the filepath corresponding to record with identifier id in this bucket (more public than it should be).
     */
    String filePath(int id);

    /**
     * @return an input Stream containing all the LXP records in this Bucket
     */
    IInputStream<T> getInputStream() throws BucketException;

    /**
     * @return an output Stream which supports the writing of records to this Bucket
     */
    IOutputStream<T> getOutputStream();

    /**
     * @return the name of the bucket
     */
    String getName();

    /**
     * @return the repository in which the bucket is located
     */
    IRepository getRepository();

    /**
     * @param id - an id to lookup
     * @return true if the bucket contains the given id
     */
    boolean contains(int id);

    /**
     * @param kind - the content kind of the bucket to be set
     */
    void setKind(BucketKind kind);

    /**
     * @return the kind of the bucket
     */
    BucketKind getKind();

    int getTypeLabelID();

    void setTypeLabelID(int id) throws IOException;

}
