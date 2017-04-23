/*
 * Copyright 2012-2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module record-classification.
 *
 * record-classification is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * record-classification is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with record-classification. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command;

import org.apache.commons.csv.*;
import org.junit.runner.*;
import org.junit.runners.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

/**
 * @author Masih Hajiarab Derkani
 */
@RunWith(Parameterized.class)
public class LoadUnseenRecordsCommandTest extends LoadRecordsCommandTest {

    public LoadUnseenRecordsCommandTest(Bucket records, CSVFormat format, int id_index, int label_index, int code_index) {

        super(records, format, id_index, label_index, code_index);
    }

    @Override
    protected Bucket getActualRecords() {

        return configuration.getUnseenRecordsOptional().get();
    }

    @Override
    protected String getSubCommandName() {

        return LoadUnseenRecordsCommand.NAME;
    }
}
