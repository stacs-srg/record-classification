package uk.ac.standrews.cs.usp.parser.mahout;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import uk.ac.standrews.cs.usp.tools.fileutils.DSStoreRemover;

/**
 * Tests .DS_Store remover.
 * @author jkc25
 *
 */
public class DSStoreRemoverTest {

    /**
     * Creates a .DS_Store file then removes it. Passes if file is created and removed without exception.
     * @throws IOException is reading error occurs.
     */
    @Test
    public void test() throws IOException {

        File dsStore = new File("resources/.DS_Store");
        File test = new File("resources");
        if (!test.mkdirs()) {
            System.err.print("Could not create folder " + test.getAbsolutePath());
        }
        if (!dsStore.createNewFile()) {
            System.err.print("Could not create folder " + dsStore.getAbsolutePath());

        }
        dsStore.canRead();
        assertTrue(dsStore.isFile());
        assertTrue(dsStore.exists());
        File here = new File(".");
        System.out.println(here.list().length);
        DSStoreRemover dsr = new DSStoreRemover();
        dsr.remove(here);
        assertTrue(!dsStore.exists());
    }
}
