package uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning;

import org.apache.lucene.analysis.core.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.spell.*;
import org.apache.lucene.store.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.*;

/**
 * Replaces tokens in the data with words in a given dictionary based on a similarity threshold.
 * By default this class picks the first suggested word for each token of the data.
 * If no suggestion exists for a token, the token itself is suggested.
 *
 * @author Masih Hajiarab Derkani
 */
public class SuggestiveCleaner implements TextCleaner, Closeable {

    private static final String SPACE = " ";
    public static final int NUMBER_OF_SUGGESTIONS = 5;
    private final SpellChecker spell_checker;

    /**
     * Constructs a new instance of this class with {@link JaroWinklerDistance jaro winkler} string distance function and accuracy threshold of {@value SpellChecker#DEFAULT_ACCURACY}.
     *
     * @param dictionary the dictionary to use for word suggestion
     * @throws IOException if failure occurs while indexing dictionary
     */
    public SuggestiveCleaner(Dictionary dictionary) throws IOException {

        this(dictionary, new JaroWinklerDistance(), SpellChecker.DEFAULT_ACCURACY);
    }

    /**
     * Constructs a new instance of this class.
     *
     * @param dictionary the dictionary to use for word suggestion.
     * @param distance_function the fuction by which to calculate similarity of data tokens to words in the dictionary
     * @param accuracy_threshold the minimum acceptable accuracy for a suggested word
     * @throws IOException if failure occurs while indexing dictionary
     */
    public SuggestiveCleaner(Dictionary dictionary, StringDistance distance_function, float accuracy_threshold) throws IOException {

        if (accuracy_threshold < 0.0 || accuracy_threshold > 1.0) {
            throw new IllegalArgumentException("the accuracy threshold must be within inclusive range of 0.0 to 1.0");
        }

        spell_checker = new SpellChecker(new RAMDirectory(), distance_function);
        spell_checker.setAccuracy(accuracy_threshold);
        spell_checker.indexDictionary(dictionary, new IndexWriterConfig(new SimpleAnalyzer()), true);
    }

    @Override
    public String cleanData(final String data) {

        return new TokenList(data).stream().map(this::suggest).reduce(this::joinWithSpace).orElseGet(() -> data);
    }

    private String joinWithSpace(final String one, final String other) {return one + SPACE + other;}

    private String suggest(final String token) {

        try {
            final String[] suggestions = getSuggestions(token);
            return selectSingleSuggestion(token, suggestions);
        }
        catch (IOException e) {
            return token;
        }
    }

    private String[] getSuggestions(final String token) throws IOException {

        return spell_checker.suggestSimilar(token, NUMBER_OF_SUGGESTIONS);
    }

    /**
     * Decides which word should be suggested for the token.
     *
     * @param token the original token for which the suggestions are generated
     * @param suggestions the generated suggestions
     * @return a single suggested word for the token.
     */
    protected String selectSingleSuggestion(String token, String[] suggestions) {

        return suggestions.length > 0 ? suggestions[0] : token;
    }

    @Override
    public void close() throws IOException {

        spell_checker.close();
    }
}
