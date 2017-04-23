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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.la4j.matrix.*;
import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.core.*;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class ProcessObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 6315686309716673307L;

    public ProcessObjectMapper() {

        // Needed for classes with private fields without setters e.g. Bucket.
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // Needed for fields typed as interfaces or abstract classes e.g. 'classifier' in ClassificationContext.
        enableDefaultTyping();

        // Needed for getter methods that don't correspond to fields e.g. 'getDescription' in StringSimilarityMetricWrapper.
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Needed for non-camel-case field names e.g. in ClassificationContext.
        setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        // Needed for .
        disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

        // To make serialized data easier to read.
        enable(SerializationFeature.INDENT_OUTPUT);

        SimpleModule module = new SimpleModule();

        configureSerializers(module);
        configureMixins(module);

        registerModule(module);
    }

    private void configureSerializers(SimpleModule module) {

        module.addSerializer(Duration.class, new JsonSerializer<Duration>() {
            @Override
            public void serialize(Duration duration, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

                jsonGenerator.writeString(duration.toString());
            }
        });
        module.addDeserializer(Duration.class, new JsonDeserializer<Duration>() {
            @Override
            public Duration deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

                try {
                    return Duration.parse(jsonParser.getText());
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });


        module.addSerializer(Instances.class, new JsonSerializer<Instances>() {
            @Override
            public void serialize(Instances instances, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

                jsonGenerator.writeStartArray();
                for (int i = 0; i < instances.numAttributes(); i++) {
                    Attribute attribute = instances.attribute(i);
                    jsonGenerator.writeObject(attribute);
                }
                jsonGenerator.writeEndArray();
            }
        });
        module.addDeserializer(Instances.class, new JsonDeserializer<Instances>() {
            @Override
            public Instances deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

                ArrayList<Attribute> attributes = new ArrayList<>();

                JsonToken token = jsonParser.nextToken(); // START_OBJECT

                while (token.isStructStart()) {

                    token = jsonParser.nextToken(); // FIELD_NAME
                    String name = jsonParser.nextTextValue();

                    token = jsonParser.nextToken(); // FIELD_NAME
                    int type = jsonParser.nextIntValue(0);

                    token = jsonParser.nextToken(); // FIELD_NAME
                    token = jsonParser.nextToken(); // START_ARRAY
                    String c = jsonParser.nextTextValue(); // class name
                    token = jsonParser.nextToken(); // START_OBJECT
                    token = jsonParser.nextToken(); // FIELD_NAME
                    token = jsonParser.nextToken(); // START_ARRAY

                    String[] values = jsonParser.readValueAs(String[].class);

                    token = jsonParser.nextToken(); // END_OBJECT
                    token = jsonParser.nextToken(); // END_ARRAY
                    token = jsonParser.nextToken(); // FIELD_NAME

                    int index = jsonParser.nextIntValue(0);

                    token = jsonParser.nextToken(); // FIELD_NAME
                    token = jsonParser.nextToken();
                    double weight = jsonParser.readValueAs(Double.class);

                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();
                    token = jsonParser.nextToken();

                    if (type == 1) {

                        // TODO may need to check for duplicate values -
                        // Exception: Unable to determine structure as arff (Reason: java.lang.IllegalArgumentException: A nominal attribute (att2) cannot have duplicate labels (72740).).
                        Attribute attribute = new Attribute(name, Arrays.asList(values), index);
                        attributes.add(attribute);

                    } else {

                        Attribute attribute = new Attribute(name, (ArrayList<String>) null, index);
                        for (String value : values) {
                            attribute.addStringValue(value);
                        }
                        attributes.add(attribute);
                    }
                }

                return new Instances("bucket", attributes, 0);
            }
        });
    }

    private void configureMixins(SimpleModule module) {

        module.setMixInAnnotation(DenseMatrix.class, DenseMatrixMixin.class);
        module.setMixInAnnotation(NaiveBayesMultinomialText.class, JsonIdentityInfoMixin.class);
        module.setMixInAnnotation(Capabilities.class, JsonIdentityInfoMixin.class);
        module.setMixInAnnotation(Capabilities.class, CapabilitiesMixin.class);
        module.setMixInAnnotation(Instance.class, InstanceMixin.class);
        module.setMixInAnnotation(Attribute.class, AttributeMixin.class);
        module.setMixInAnnotation(NominalAttributeInfo.class, NominalAttributeInfoMixin.class);
    }
}
