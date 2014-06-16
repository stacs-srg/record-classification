package uk.ac.standrews.cs.usp.parser.preprocessor;

/**
 * Interface that defines the methods that implementing classes doing data preprocessing must follow.
 * @author jkc25
 *
 */
public interface iPreProcessor {

    /**
     * Process the specified file.
     * @param fileName the file to process
     * @return true of successful
     */
    boolean process(String fileName);
}
