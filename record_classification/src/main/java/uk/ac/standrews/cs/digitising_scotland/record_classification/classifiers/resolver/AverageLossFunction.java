package uk.ac.standrews.cs.digitising_scotland.record_classification.classifiers.resolver;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multiset;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifiers.resolver.Interfaces.LossFunction;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.classification.Classification;

/**
 * Calculates the loss for a set of CodeTriples.
 * The loss function is defined as the average confidence of all the {@link uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.classification.Classification} in the set.
 * @author jkc25
 *
 */
public class AverageLossFunction implements LossFunction<Multiset<Classification>, Double> {

    @Override
    public Double calculate(final Multiset<Classification> set) {

        List<Double> confidences = new ArrayList<>();
        for (Classification triple : set) {
            confidences.add(Math.abs(triple.getConfidence()));
        }
        Double confidenceSum = 0.;
        for (Double conf : confidences) {
            confidenceSum += conf;
        }

        return confidenceSum / (double) confidences.size();
    }

}