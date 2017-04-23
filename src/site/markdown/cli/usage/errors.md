# Errors

## Existing configuration directory

When classli is run, it creates a hidden directory in the current working directory to store intermediate results. If an existing configuration directory is already present, the message:

    Configuration directory <config_dir> already exists. Use -f flag to init command to force deletion of existing directory.

indicates that the directory will not be overwritten. To force overwriting, use the -f flag to the init command. Alternatively, remove the hidden directory manually.

## Missing data file

The message:

	Failure while loading records.
	File <data_file> not found.

indicates that a specified gold standard or classification data file cannot be found, relative to the current working directory.

## Invalid data format

The message:

	Failure while reading a record from file <data_file>: check CSV format at specified line.
	File format error: java.io.IOException: (line <line_no>) invalid char between encapsulated token and delimiter.

indicates that a specified gold standard or classification data file does not conform to the required [CSV format](data-format.html).
