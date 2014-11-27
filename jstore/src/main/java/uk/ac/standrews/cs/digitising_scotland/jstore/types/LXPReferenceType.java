package uk.ac.standrews.cs.digitising_scotland.jstore.types;

import uk.ac.standrews.cs.digitising_scotland.jstore.impl.exceptions.KeyNotFoundException;
import uk.ac.standrews.cs.digitising_scotland.jstore.impl.LXP;
import uk.ac.standrews.cs.digitising_scotland.jstore.interfaces.ILXP;
import uk.ac.standrews.cs.digitising_scotland.jstore.interfaces.IReferenceType;
import uk.ac.standrews.cs.digitising_scotland.jstore.interfaces.IType;
import uk.ac.standrews.cs.digitising_scotland.util.ErrorHandling;
import uk.ac.standrews.cs.digitising_scotland.util.FileManipulation;
import uk.ac.standrews.cs.nds.persistence.PersistentObjectException;
import uk.ac.standrews.cs.nds.rpc.stream.JSONReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Created by al on 2/11/2014.
 * A class representing reference types that may be encoded above LXP storage layer (optional)
 */
public class LXPReferenceType implements IReferenceType {

    private ILXP typerep;

    public LXPReferenceType(String json_encoded_type_descriptor_file_name) {

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(json_encoded_type_descriptor_file_name), FileManipulation.FILE_CHARSET)) {

            this.typerep = new LXP(new JSONReader(reader));

        } catch (PersistentObjectException e) {
            ErrorHandling.exceptionError(e, "persistent object exception reading types file: " + json_encoded_type_descriptor_file_name);
        } catch (IOException e) {
            ErrorHandling.exceptionError(e, "IO exception reading types file: " + json_encoded_type_descriptor_file_name);
        }
    }

    public LXPReferenceType(LXP typerep) {
        this.typerep = typerep;
    }

    public ILXP get_typerep() {
        return typerep;
    }

    @Override
    public boolean isReferenceType() {
        return true;
    }

    @Override
    public boolean isBaseType() {
        return false;
    }

    @Override
    public LXPReferenceType getReferenceType() {
        return this;
    }

    @Override
    public LXPBaseType getBaseType() {
        return null;
    }

    @Override
    public Collection<String> getLabels() {
        return typerep.getKeys();
    }

    @Override
    public IType getFieldType(String label) throws KeyNotFoundException {
        if (typerep.containsKey(label)) {
            String value = typerep.get(label);
            return Types.stringToType(value);
        } else return LXPBaseType.UNKNOWN;
    }

    public int getId() {
        return typerep.getId();
    }

    @Override
    public ILXP getRep() {
        return typerep;
    }

}
