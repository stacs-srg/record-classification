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

import com.google.common.base.*;
import com.google.common.io.*;
import org.apache.commons.io.*;
import org.junit.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author masih
 */
public class ExperimentCommandTest extends CommandTest {

    @Test(expected = RuntimeException.class)
    public void expectFailureIfCommandsNotSpecified() throws Exception {

        new ExperimentCommand.Builder().run(launcher);
    }

    @Test
    public void testExperimentRepetitionsFolderAreCreated() throws Exception {

        final File batch = temporary.newFile();
        final Command.BatchBuilder batch_builder = new Command.BatchBuilder();

        final InitCommand.Builder init = new InitCommand.Builder();
        init.setForce(true);

        batch_builder.add(init);
        batch_builder.build(batch, StandardCharsets.UTF_8);

        final ExperimentCommand.Builder experiment = new ExperimentCommand.Builder();
        experiment.setCommands(batch.toPath());
        experiment.run(launcher);

        for (int i = ExperimentCommand.FIRST_REPETITION_NUMBER; i <= ExperimentCommand.DEFAULT_REPETITION_COUNT; i++) {
            final File repetition_home = new File(working_directory.toFile(), ExperimentCommand.REPETITION_WORKING_DIRECTORY_PREFIX + i);
            assertTrue(Files.isDirectory(repetition_home.toPath()));
        }
    }
}
