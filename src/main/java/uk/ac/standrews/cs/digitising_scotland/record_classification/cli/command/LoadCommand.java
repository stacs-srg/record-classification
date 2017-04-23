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

import com.beust.jcommander.*;
import com.beust.jcommander.converters.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/**
 * Command to load a resource from the local file system.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = LoadCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.load.description")
public class LoadCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "load";

    /** The short name of the option that specifies the charset of the resource file to be load. **/
    public static final String OPTION_CHARSET_SHORT = "-c";

    /** The long name of the option that specifies the charset of the resource file to be load. **/
    public static final String OPTION_CHARSET_LONG = "--charset";

    /** The short name of the option that specifies the path to the resource file to be load. **/
    public static final String OPTION_SOURCE_SHORT = "-s";

    /** The long name of the option that specifies the path to the resource file to be load. **/
    public static final String OPTION_SOURCE_LONG = "--from";

    /** The short name of the option that specifies whether to override an existing resource with the same name. **/
    public static final String OPTION_FORCE_SHORT = "-o";

    /** The long name of the option that specifies whether to override an existing resource with the same name. **/
    public static final String OPTION_FORCE_LONG = "--overrideExisting";

    @Parameter(names = {OPTION_CHARSET_SHORT, OPTION_CHARSET_LONG}, descriptionKey = "command.load.charset.description")
    private CharsetSupplier charset_supplier = configuration.getDefaultCharsetSupplier();

    @Parameter(required = true, names = {OPTION_SOURCE_SHORT, OPTION_SOURCE_LONG}, descriptionKey = "command.load.source.description", converter = PathConverter.class)
    private Path source;

    @Parameter(names = {OPTION_FORCE_SHORT, OPTION_FORCE_LONG}, descriptionKey = "command.load.force.description")
    private boolean override_existing = false;

    public LoadCommand(final Launcher launcher) { super(launcher, NAME); }

    protected static abstract class Builder extends Command.Builder {

        private CharsetSupplier charset;
        private Path source;
        private boolean override_existing;

        public void setSourceCharset(CharsetSupplier charset) {

            this.charset = charset;
        }

        public void setSource(Path source) {

            this.source = source;
        }

        public void setOverrideExisting(boolean override_existing) {

            this.override_existing = override_existing;
        }

        @Override
        protected void populateArguments() {

            Objects.requireNonNull(source);

            addArgument(NAME);
            addArgument(OPTION_SOURCE_SHORT);
            addArgument(source);

            if (charset != null) {
                addArgument(OPTION_CHARSET_SHORT);
                addArgument(charset.name());
            }
            if (override_existing) {
                addArgument(OPTION_FORCE_SHORT);
            }
        }

        protected abstract void populateSubCommandArguments();
    }

    @Override
    public void run() {

        final Optional<Command> command = getSubCommand();

        if (command.isPresent()) {
            logger.fine(() -> "Detected sub command " + command);
            command.get().run();
        }
        else {
            logger.severe(() -> "No sub command detected to execute");
            throw new ParameterException("Please specify a sub command.");
        }
    }

    /**
     * Gets the charset of the resource to be loaded.
     * If no charset is specified via {@value #OPTION_CHARSET_SHORT} or {@value #OPTION_CHARSET_LONG} options, the
     * {@link Configuration#getDefaultCharsetSupplier() default charset} is used.
     *
     * @return the charset of the resource to be loaded
     */
    public Charset getCharset() {

        return charset_supplier.get();
    }

    /**
     * Gets the path to resource to be loaded.
     * The path is specified via {@value #OPTION_SOURCE_SHORT} or {@value #OPTION_SOURCE_LONG} options.
     *
     * @return the path to resource to be loaded
     */
    public Path getSource() {

        return source;
    }

    /**
     * Whether to override an existing resource with the same name.
     * This option may be set via {@value #OPTION_FORCE_SHORT} or {@value #OPTION_FORCE_LONG} options.
     * By default this option is disabled.
     *
     * @return whether to override an existing resource with the same name
     */
    public boolean isOverrideExistingEnabled() {

        return override_existing;
    }
}
