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
package old.record_classification_old.feature_space_analysis;

import org.junit.Ignore;
import org.junit.Test;
import old.record_classification_old.datastructures.bucket.Bucket;
import old.record_classification_old.datastructures.code.CodeDictionary;
import old.record_classification_old.datastructures.code.CodeNotValidException;
import old.record_classification_old.exceptions.InputFormatException;
import old.record_classification_old.featurespaceanalysis.CodeReportFormatter;
import old.record_classification_old.featurespaceanalysis.FeatureSpaceAnalyser;
import old.record_classification_old.pipeline.BucketGenerator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 *
 * Created by fraserdunlop on 13/10/2014 at 15:52.
 */
@Ignore
public class FeatureSpaceAnalyserTest {

    @Test
    public void test() throws IOException, CodeNotValidException, InputFormatException {
        File trainingFile = new File(getClass().getResource("/TrainingDataModernCODFormatTest.txt").getFile());
        Bucket bucket = createTrainingBucket(trainingFile);
        FeatureSpaceAnalyser featureSpaceAnalyser = new FeatureSpaceAnalyser(bucket);
        File codeFile = new File(getClass().getResource("/modCodeDictionary.txt").getFile());
        CodeDictionary codeDictionary = new CodeDictionary(codeFile);
        HashMap<String, Integer> map = featureSpaceAnalyser.featureProfile(codeDictionary.getCode("R54"));
        System.out.println(map);
    }

    private Bucket createTrainingBucket(final File trainingFile) throws IOException, InputFormatException, CodeNotValidException {

        String codeDictionaryPath = getClass().getResource("/modCodeDictionary.txt").getFile();
        File codeDictionaryFile = new File(codeDictionaryPath);
        CodeDictionary codeDictionary = new CodeDictionary(codeDictionaryFile);
        BucketGenerator b = new BucketGenerator(codeDictionary);
        return b.generateTrainingBucket(trainingFile);
    }

    @Test
    public void reportTest() throws Exception, CodeNotValidException {
        File trainingFile = new File(getClass().getResource("/TrainingDataModernCODFormatTest.txt").getFile());
        Bucket bucket = createTrainingBucket(trainingFile);
        FeatureSpaceAnalyser featureSpaceAnalyser = new FeatureSpaceAnalyser(bucket);
        CodeReportFormatter formatter = new CodeReportFormatter(featureSpaceAnalyser);
        File codeFile = new File(getClass().getResource("/modCodeDictionary.txt").getFile());
        CodeDictionary codeDictionary = new CodeDictionary(codeFile);
        System.out.println(formatter.formatReport(codeDictionary.getCode("D69")));
    }

    @Test
    public void featureLinesRefactoringTest() throws Exception, CodeNotValidException {
        File trainingFile = new File(getClass().getResource("/TrainingDataModernCODFormatTest.txt").getFile());
        Bucket bucket = createTrainingBucket(trainingFile);
        FeatureSpaceAnalyser featureSpaceAnalyser = new FeatureSpaceAnalyser(bucket);
        CodeReportFormatter formatter = new CodeReportFormatter(featureSpaceAnalyser);
        File codeFile = new File(getClass().getResource("/modCodeDictionary.txt").getFile());
        CodeDictionary codeDictionary = new CodeDictionary(codeFile);
        System.out.println(formatter.formatReport(codeDictionary.getCode("J98")));
    }

}
