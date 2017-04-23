# Batch Command Execution Example

In order to execute multiple commands at once, the commands need to be specified in a text file, where each line in the file corresponds to a command and its options.
Here is an example:

    # Step 1: initialise classli
    init

    # Step 2: set the classifier to EXACT_MATCH
    set --classifier EXACT_MATCH

    # Step 3: load gold standard data from file 'gs.csv' in current working directory,
    #         skip header record, use all records for training
    load --from gs.csv gold_standard -h -t 1.0

    # Step 4: load unseen data from file 'unseen.csv' in current working directory,
    #         skip header record
    load --from unseen.csv unseen -h

    # Step 5: clean loaded gold standard and unseen data using all predefined cleaners
    clean -c COMBINED

    # Step 6: train classifier using all cleaned gold standard data (no internal evaluation)
    train -it 1.0

    # Step 7: classify unseen records, store classified records in file
    #         'classified-unseen.csv' in current working directory
    classify -o classified-unseen.csv
    
The lines starting with `#` character are comments; they are ignored by the `classli`. The comments in the above example explain what each of the 8 commands do.
Additionally, any line that only contains whitespace characters in the batch commands file is also ignored.

The above commands do not specify all the possible options of `classli`. 
For any unspecified option, `classli` uses internal default values.
For example, in steps 3 and 4 gold standard records (from `gs.csv`) and unseen records (from `unseen.csv`) are loaded respectively. 
There are a lot of options we can specify to tell `classli` how to read the two CSV files. These options include `-d` for delimiter (the character by which the values are separated), `-c` for character encoding and many more.
Since these options are not specified the default values are used, in this case `,` as delimiter and the operating system's default character encoding. See [usage](usage.html) for a complete list of all the options and their default values.

The gold standard file, `gs.csv`, must have at least 3 columns: id, label and class. The `-h` option in step 3 specifies that the first row in `gs.csv` contains the column labels and should not be considered as part of the data. Here is an example gold standard data file:

    id,label,class
    1,fish,swims
    2,dog,barks
    3,cat,purrs

The unseen file, `unseen.csv`, must have at least 2 columns: id and label. Here is an example unseen data file:

    id,label
    1,fish
    2,dog


To run the commands in batch mode, they need to be stored in a text file. In this example we assume they are stored in a file called `commands.txt`.
The file is then passed to `classli` for execution:

    classli -c commands.txt

The `-c` option enables batch command execution mode in `classli`, and `commands.txt` appearing after `-c` option specifies where to find the file containing the commands.

Example files:

* [commands.txt](batch/commands.txt)
* [gs.csv](batch/gs.csv)
* [unseen.csv](batch/unseen.csv)
