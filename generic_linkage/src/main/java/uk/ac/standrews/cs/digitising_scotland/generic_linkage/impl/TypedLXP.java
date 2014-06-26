package uk.ac.standrews.cs.digitising_scotland.generic_linkage.impl;

import uk.ac.standrews.cs.digitising_scotland.generic_linkage.impl.types.Type;
import uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces.ILXP;
import uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces.ILabels;
import uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces.ITypedLXP;
import uk.ac.standrews.cs.digitising_scotland.util.ErrorHandling;

/**
 * Created by al on 20/06/2014.
 */
public class TypedLXP extends LXP implements ITypedLXP  {
    @Override
    public void put(String key, int value) throws TypedLXPException {

        if( compatible_field_type( key, Type.INT ) ) {
            super.put(key, Integer.toString(value));
        }
    }

    @Override
    public int getInt(String key) throws NumberFormatException, TypedLXPException {

        if( compatible_field_type( key, Type.INT ) ) {

            return Integer.parseInt(super.get(key));
        }
        return 0; // not reached
    }

    @Override
    public void putRef(String key, int id) throws TypedLXPException {

        if( compatible_field_type( key, Type.REFERENCE ) ) {
            super.put(key, Integer.toString(id));
        }

    }

    @Override
    public int getRef(String key) throws TypedLXPException {
        if( compatible_field_type( key, Type.REFERENCE ) ) {

            return Integer.parseInt(super.get(key));
        }
        return 0; // not reached
    }

    @Override
    public ILXP getReferend(String key) throws TypedLXPException {

        if( compatible_field_type( key, Type.REFERENCE ) ) {
            int ref = getRef(key);
            // make into an ILXP by lookup in store!
            return null;
        }
        return null;  // not reached
    }

    @Override
    public void put(String key, float value) throws TypedLXPException {
        if( compatible_field_type( key, Type.FLOAT ) ) {

            super.put(key, Float.toString(value));
        }
    }

    @Override
    public float getFloat(String key) throws TypedLXPException {
        if( compatible_field_type( key, Type.FLOAT ) ) {

            return Float.parseFloat(super.get(key));
        }
        return 0.0F; // Not reached
    }

    private boolean compatible_field_type( String key, Type expected_type ) throws TypedLXPException {
        String class_name = super.get( "TYPE" ); // TODO consider the depenencies here - this is defined in CommonLabels but need it somewehere generically.
        if( class_name == null ) { // field doesn't exist
            return false;
        }
        // Have found a class string which by convention is a Labels class.
        try {
                Class c = Class.forName(class_name);  // get the class associated with the type - by convention TYPE field contains class name of ILabels meta information
                Object o = c.newInstance();
                Type field_type = ((ILabels) o).getType(key); // expect the class to implement the ILabels interface
                if( field_type.equals(expected_type) ) {
                    return true;
                } else {
                    throw new TypedLXPException( "Incompatible types" ); // TODO put a better error in here.
                }

        } catch (ClassNotFoundException e) { // Can't find class - give up.
            ErrorHandling.error( "Class not found: " +  class_name );
            return false;
        } catch (InstantiationException e) {
            ErrorHandling.error( "Instantiation exception for: " +  class_name );
            return false;
        } catch (IllegalAccessException e) {
            ErrorHandling.error( "Illegal Access exception for: " +  class_name );
            return false;
        }
    }
}