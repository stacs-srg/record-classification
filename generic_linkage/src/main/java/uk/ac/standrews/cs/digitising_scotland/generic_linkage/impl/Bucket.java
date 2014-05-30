package uk.ac.standrews.cs.digitising_scotland.generic_linkage.impl;

import org.json.JSONException;
import org.json.JSONWriter;
import uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces.IBucket;
import uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces.ILXP;
import uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces.ILXPInputStream;
import uk.ac.standrews.cs.digitising_scotland.generic_linkage.interfaces.ILXPOutputStream;
import uk.ac.standrews.cs.digitising_scotland.util.FileManipulation;
import uk.ac.standrews.cs.nds.persistence.PersistentObjectException;
import uk.ac.standrews.cs.nds.rpc.stream.JSONReader;
import uk.ac.standrews.cs.nds.util.ErrorHandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Bucket implements IBucket {

    private String base_path; // the name of the parent directory in which this bucket (itself a directory) is stored.
    private String name;     // the name of this bucket - used as the directory name
    private File directory;  // the directory implementing the bucket storage

    /**
     * Creates a handle on a bucket.
     * Assumes that bucket has been created already using a factory - i.e. the directory already exists.
     *
     * @param name      the name of the bucket (also used as directory name)
     * @param base_path the path of the parent directory
     */
    public Bucket(final String name, final String base_path) throws IOException {

        this.name = name;
        this.base_path = base_path;
        String dir_name = dirPath();
        directory = new File(dir_name);
        if (!directory.isDirectory()) {
            throw new IOException("Bucket Directory: " + dir_name + " does not exist");
        }
    }

    @Override
    public ILXP get(final int id) throws PersistentObjectException, IOException {

        JSONReader reader = getReader(id);
        LXP lxp = new LXP(id, reader);

        // TODO add method to close JSONReader, which should close encapsulated reader, and close here
        return lxp;
    }

    @Override
    public void put(final ILXP record) throws IOException, JSONException {

        Path path = Paths.get(this.filePath(record.getId()));

        if (Files.exists(path)) {
            throw new IOException("File already exists - LXP records in buckets may not be overwritten");
        }

        try (Writer writer = Files.newBufferedWriter(path, FileManipulation.FILE_CHARSET)) {

            record.serializeToJSON(new JSONWriter(writer));
        }
    }

    @Override
    public String filePath(final int id) {
        return filePath(Integer.toString(id));
    }

    protected File baseDir() {
        return directory;
    }

    protected String dirPath() {

        return base_path + File.separator + name;
    }

    public String filePath(final String id) {
        return dirPath() + File.separator + id;
    }

    @Override
    public ILXPInputStream getInputStream() {
        try {
            return new BucketBackedInputStream(this, baseDir());
        } catch (IOException e) {
            ErrorHandling.exceptionError(e, "Cannot open input Stream for bucket " + name);
            return null;
        }
    }

    @Override
    public ILXPOutputStream getOutputStream() {
        return new BucketBackedOutputStream(this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    private JSONReader getReader(final int id) throws IOException {

        String path = filePath(id);
        BufferedReader reader = Files.newBufferedReader(Paths.get(path), FileManipulation.FILE_CHARSET);

        return new JSONReader(reader);
    }
}
