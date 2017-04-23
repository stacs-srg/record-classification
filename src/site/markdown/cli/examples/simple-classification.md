
## Simple Classification of Records

This example aims to walk through the necessary steps to classify a basic collection of records.
In order to classify a set of records using `classli`, it is necessary to train the system first.

Before we start, let's create a new folder named *simple_classification_example*. 
This folder will contain all the input files we pass to `classli` and all the output files `classli` produces.

Assume we would like to classify animals into one of the following classes:

- amphibian
- insect
- bird
- fish
- mammal
- reptile

To do so, we first need a data set that contains a collection of animals that are already classified, called the training data set.
The training data set is made up of a CSV file containing three columns: id, animal and class.
Here is the content of the training data set:

    id,animal,class
    1,seal,mammal
    2,rabbit,mammal
    3,butterfly,insect
    4,salmon,fish
    5,cod,fish
    6,frog,amphibian
    7,robin,bird
    8,snake,reptile
    9,ant,insect
    10,moose,mammal
    11,monkey,mammal
    12,fly,insect
    13,lizard,reptile
    14,firefly,insect
    15,spider,insect
    16,parrot,bird
    17,toad,amphibian
    18,salamander,amphibian
    19,newt,amphibian
    20,gecko,reptile
    21,aligator,reptile
    22,viper,reptile
    23,tortoise,reptile
    24,eagle,bird
    25,sparrow,bird

Store the training data set into the folder we created earlier, in a file named *training_data.csv*.

The next thing we need is a set of records that we intend to classify, called unseen data set.
The unseen data set is made uo of a CSV file containing two columns: id and animal.
Here is the content of the unseen data set:

    id,animal
    1,rabbit
    2,butterfly
    3,cod
    4,frog
    5,robin

Also store the unseen data set into the folder we created earlier, in a file named *unseen_data.csv*.

We now have all we need to get `classli` do the job. 

Fire up the command line interface of your operating system (Command Prompt in windows, Terminal in Mac OS X and Shell in Linux).
Change the current directory to the one we created earlier by typing `cd`, followed by a space, followed by the path to the directory.

Initialise a new classification process by typing the following and press enter:

    classli init

Set the classifier using the following command:

    classli set --classifier EXACT_MATCH

In this example we use the `EXACT_MATCH` classifier. The next step is to load the training data set by executing the following command:

    classli load --from training_data.csv gold_standard -h -d "," -t 1.0 

The command above loads the training data set into the classli system. The *-h* specifies that the first line in the data file corresponds to column labels, the *-d ","* specifies values are comma-separated,
and finally *-t 1.0* specifies that 100% of the data should be used for training the classifier.

The next step is to load the unseen data set:

    classli load --from unseen_data.csv unseen -h -d ","

Now that the training and unseen data sets are loaded, we can clean them using the default built-in set of cleaners:

    classli clean --cleaner COMBINED

All the needed data is now loaded and cleaned. We now can begin to train the classifier:

    classli train

And finally classify the unseen records:

    classli classify -o classified_data.csv

The above command stores the classified records into a file called *classified_data.csv*, containing the following:

    ID,DATA,ORIGINAL_DATA,CODE,CONFIDENCE,DETAIL
    1,rabbit,rabbit,mammal,1.0,
    2,butterfli,butterfly,insect,1.0,
    3,cod,cod,fish,1.0,
    4,frog,frog,amphibian,1.0,
    5,robin,robin,bird,1.0,


The *classified_data.csv* file contains five columns:

- *ID* -- the id of the unseen record that is classified,
- *DATA* -- the data that was actually used for classification (since the data may be changed by cleaning),
- *ORIGINAL_DATA* -- the data that was loaded as part of the unseen data set,
- *CODE* -- the classification of the record,
- *CONFIDENCE* -- the confidence of the classifier in the classification of each record, and
- *DETAILS* -- optional details about the internal configuration of the classifier.

Notice how the data for record *2* is changed from *butterfly* to *butterfli*. This is change occurs as part of the cleaning process.
