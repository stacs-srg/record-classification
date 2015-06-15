/*
 * Copyright 2015 Digitising Scotland project:
 * <http://digitisingscotland.cs.st-andrews.ac.uk/>
 *
 * This file is part of the module record_classification.
 *
 * record_classification is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * record_classification is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with record_classification. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package old.record_classification_old.tools.fileutils;

import com.healthmarketscience.jackcess.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.util.FileManipulation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Ignore
public class AccessDatabaseDumperTest {

    private static final String DATABASE_NAME = "testDatabase";
    private static final String TABLE_NAME = "testTable";
    private static final String EXPECTED_TABLE_FILE_NAME = "databaseDumperExpected";
    private static final String ACCESS_SUFFIX = "mdb";

    Path test_directory_path;
    Path database_file_path;
    Path table_file_path;
    Path expected_table_file_path;

    @Before
    public void setUp() throws IOException, URISyntaxException {

        test_directory_path = Files.createTempDirectory(null);
        database_file_path = test_directory_path.resolve(appendSuffix(DATABASE_NAME, ACCESS_SUFFIX));

        table_file_path = test_directory_path.resolve(appendSuffix(TABLE_NAME, AccessDatabaseDumper.TAB_SEPARATED_SUFFIX));
        expected_table_file_path = Paths.get(getClass().getResource(appendSuffix(EXPECTED_TABLE_FILE_NAME, AccessDatabaseDumper.TAB_SEPARATED_SUFFIX)).toURI());
    }

    @Ignore
    @Test
    public void dumpedDatabaseFileContainsExpectedContents() throws IOException {

        Database database = createDatabase();
        createTable(database);
        dumpTableToFile(database);

        FileManipulation.assertThatFilesHaveSameContent(table_file_path, expected_table_file_path);
    }

    private Database createDatabase() throws IOException {

        return DatabaseBuilder.create(Database.FileFormat.V2010, database_file_path.toFile());
    }

    private void createTable(Database database) throws IOException {

        Table table = new TableBuilder(TABLE_NAME).addColumn(new ColumnBuilder("ID", DataType.INT)).addColumn(new ColumnBuilder("Foo", DataType.TEXT)).addColumn(new ColumnBuilder("Bar", DataType.TEXT)).toTable(database);

        table.addRow(0, "foo", "bar");
        table.addRow(1, "foo1", "bar1");
        table.addRow(2, "foo2", "bar2");
    }

    private void dumpTableToFile(Database database) throws IOException {

        AccessDatabaseDumper dumper = new AccessDatabaseDumper(database);
        dumper.writeTablesToFile(test_directory_path);
    }

    private String appendSuffix(String database_name, String suffix) {

        return database_name + "." + suffix;
    }
}