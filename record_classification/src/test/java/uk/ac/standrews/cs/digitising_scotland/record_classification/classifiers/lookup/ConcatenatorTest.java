package uk.ac.standrews.cs.digitising_scotland.record_classification.classifiers.lookup;

import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifiers.lookup.Concatenator;

/**
 * Tests the {@link Concatenator} class.
 * @author jkc25
 *TODO Write more tests
 */
public class ConcatenatorTest {

    @Test
    public void testSimpleStringConcat() {

        String testString = "Here is a String for testing";

        TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(testString));
        CharSequence delimiter = ",";
        String concatenatedString = Concatenator.concatenate(tokenizer, CharTermAttribute.class, delimiter);
        Assert.assertEquals("Here,is,a,String,for,testing", concatenatedString);
    }

    @Test
    public void testSimpleStringConcat2() {

        String testString = "Here, is a String for testing";

        TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(testString));
        CharSequence delimiter = ",";
        String concatenatedString = Concatenator.concatenate(tokenizer, CharTermAttribute.class, delimiter);
        Assert.assertEquals("Here,is,a,String,for,testing", concatenatedString);
        System.out.println(concatenatedString);
    }

    @Test
    public void testSpaceAtEndOfString() {

        String testString = "Here, is a String for testing ";

        TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(testString));
        CharSequence delimiter = ",";
        String concatenatedString = Concatenator.concatenate(tokenizer, CharTermAttribute.class, delimiter);
        Assert.assertEquals("Here,is,a,String,for,testing", concatenatedString);
    }

    @Test
    public void testSingleWord() {

        String testString = "Testing";

        TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(testString));
        CharSequence delimiter = ",";
        String concatenatedString = Concatenator.concatenate(tokenizer, CharTermAttribute.class, delimiter);
        Assert.assertEquals("Testing", concatenatedString);
    }

    @Test
    public void testNewLineAsDelimiter() {

        String testString = "One, Two, Three";
        TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(testString));
        CharSequence delimiter = "\n";
        String concatenatedString = Concatenator.concatenate(tokenizer, CharTermAttribute.class, delimiter);
        String expected = "One\nTwo\nThree";
        Assert.assertEquals(expected, concatenatedString);
    }

    @Test
    public void testEmptyString() {

        String testString = "";
        String expected = "";

        TokenStream tokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(testString));
        CharSequence delimiter = "\n";
        String concatenatedString = Concatenator.concatenate(tokenizer, CharTermAttribute.class, delimiter);
        Assert.assertEquals(expected, concatenatedString);
    }
}