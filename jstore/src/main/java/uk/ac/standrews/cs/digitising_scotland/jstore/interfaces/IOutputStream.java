package uk.ac.standrews.cs.digitising_scotland.jstore.interfaces;

/**
 * Provides the interface to an output stream of labelled cross product records.
 * Created by al on 28/04/2014.
 */
public interface IOutputStream<T extends ILXP> {

    void add(T record);
}