# Running an Experiment

This example aims to illustrate how to use `classli` to run an experiment.
Running an experiment involves executing a batch command file for a number of times and produce aggregated results, e.g. aggregated precision/recall measurements if experiment includes evaluation of a classifier.

Before we start, let's create a new folder named *experiment_example*. 
This folder will contain all the input files we pass to `classli` and all the output files `classli` produces.

To run an experiment we first need a batch command file, which contains steps in our experiment.
Here is the batch command file we are going to use in this example:

    # Step 1
    init
    
    # Step 2
    set --classifier EXACT_MATCH --seed 42
    
    # Step 3
    load --from training_data.csv gold_standard -h -d "," -t 0.8
    
    # Step 4
    clean -c COMBINED
    
    # Step 5
    train
    
    # Step 6
    evaluate -o classified_evaluation_data.csv
    

The lines starting with *#* are comments and are ignored by `classli`. The explanation of the steps are:

- *Step 1* -- initialise a new classification process
- *Step 2* -- set the classifier to EXACT_MATCH, and seed the random number generator with *42* in order to make the results reproducible 
- *Step 3* -- load comma-separated gold standard data from *training_data.csv* file at the current working directory, and skip header record, where 80% of records are used for training and the remaining 20% are used for evaluation.
- *Step 4* -- clean loaded gold standard and unseen data using all the predefined cleaners.
- *Step 5* -- train the classifier using the loaded training records (i.e. 80% of the loaded gold standard data).
- *Step 6* -- evaluate the classifier using the remaining 20% of the cleaned gold standard data, and store the classified evaluation records in a file called *classified_evaluation_data.csv* at the current working directory.

Store the batch command file into a file named *batch.txt* within the folder we created earlier. 
In this example we are going to re-use the training data set from the [simple classification example](simple_classification.html).
Make sure the *training_data.csv* is also copied into the folder.

Fire up the command line interface of your operating system (Command Prompt in windows, Terminal in Mac OS X and Shell in Linux).
Change the current directory to the one we created earlier by typing `cd`, followed by a space, followed by the path to the directory.

We are now ready to run the experiment:

    classli experiment --commands batch.txt --repeat 3

The above command runs the commands specified in *batch.txt* *3* times, where the results of each repetition is stored in folders called *repetition_1*, *repetition_2* and *repetition_3*.
The aggregated results of the evaluation is printed in the console:

    Number Of Classes:             7.00 ± 0.00
    Number Of Classifications:     5.00 ± 0.00
    Number Of True Positives:      0.00 ± 0.00
    Number Of True Negatives:      25.00 ± 0.00
    Number Of False Negatives:     5.00 ± 0.00
    Number Of False Positives:     5.00 ± 0.00
    Macro Average Accuracy:        0.83 ± 0.00
    Macro Average F1:              0.00 ± 0.00
    Macro Average Precision:       NaN ± NaN
    Macro Average Recall:          0.00 ± 0.00
    Micro Average Accuracy:        0.71 ± 0.00
    Micro Average F1:              0.00 ± 0.00
    Micro Average Precision:       0.00 ± 0.00
    Micro Average Recall:          0.00 ± 0.00

The *±* values are the confidence intervals with 95% confidence level.
