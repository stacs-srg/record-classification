/*
 * record_classification - Automatic record attribute classification.
 * Copyright © 2012-2016 Digitising Scotland project
 * (http://digitisingscotland.cs.st-andrews.ac.uk/record_classification/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli;

import org.junit.*;
import org.omg.CORBA.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.config.*;

import java.nio.file.*;

import static org.junit.Assert.*;

/**
 * @author Masih Hajiarab Derkani
 */
public class LauncherTest {

    @Test
    public void testSystemPropertiesAreSet() throws Exception {

        assertTrue(Boolean.valueOf(System.getProperty(Launcher.JVM_HEADLESS_PROPERTY)));
    }

    @Test
    public void testConfigurationIsSLoadedAtWorkingDirectory() throws Exception {

        final Launcher launcher = new Launcher();
        assertNotNull(launcher.getConfiguration());
        assertEquals(Configuration.DEFAULT_WORKING_DIRECTORY, launcher.getConfiguration().getWorkingDirectory());

        final Path working_directory = Paths.get("test-working-dir");
        final Launcher launcher_with_working_dir = new Launcher(working_directory);
        assertNotNull(launcher_with_working_dir.getConfiguration());
        assertEquals(working_directory, launcher_with_working_dir.getConfiguration().getWorkingDirectory());
    }
}
