package uk.ac.standrews.cs.digitising_scotland.linkage.injesters;

import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.BirthFamilyGT;
import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Death;
import uk.ac.standrews.cs.digitising_scotland.linkage.lxp_records.Marriage;
import uk.ac.standrews.cs.storr.impl.StoreFactory;
import uk.ac.standrews.cs.storr.impl.exceptions.RepositoryException;
import uk.ac.standrews.cs.storr.impl.exceptions.StoreException;
import uk.ac.standrews.cs.storr.interfaces.IBucket;
import uk.ac.standrews.cs.storr.interfaces.IStore;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Module to initialise the store ready for injesting of BBM records.
 * Created by al on 22/3/2017.
 * @author al@st-andrews.ac.uk
 */

public class InitialiseStorr {

    private static final String input_repo_name = "BDM_repo";               // Repository containing event records
    private static final String births_name = "birth_records";              // Name of bucket containing birth records (inputs).
    private static final String marriages_name = "marriage_records";        // Name of bucket containing marriage records (inputs).
    private static final String deaths_name = "death_records";              // Name of bucket containing death records (inputs).

    // Bucket declarations

    protected IBucket<BirthFamilyGT> births;                                // Bucket containing birth records (inputs).
    protected IBucket<Marriage> marriages;                                  // Bucket containing marriage records (inputs).
    protected IBucket<Death> deaths;                                        // Bucket containing death records (inputs).

    private static final String[] ARG_NAMES = {"store_path"};


    public InitialiseStorr( String store_path ) throws StoreException, IOException, RepositoryException {

        System.out.println( "Creating Storr in " + store_path );
        StoreFactory.setStorePath( Paths.get( store_path) );
        IStore store = StoreFactory.makeStore();
        System.out.println( "Storr successfully created in " + store_path );
    }

    //***********************************************************************************

    public static void usage() {

        System.err.println("Usage: run with " + String.join(" ", ARG_NAMES));
    }

    public static void main(String[] args) throws Exception {

        if (args.length >= ARG_NAMES.length) {

            String store_path = args[0];
            new InitialiseStorr( store_path );

        } else {
            usage();
        }
    }


}