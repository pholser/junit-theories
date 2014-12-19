package org.junit.contrib.theories.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.contrib.theories.ParameterSignature;
import org.junit.contrib.theories.ParameterSupplier;
import org.junit.contrib.theories.PotentialAssignment;

public class EnumSupplier extends ParameterSupplier {
    private final Class<?> enumType;

    public EnumSupplier(Class<?> enumType) {
        this.enumType = enumType;
    }

    @Override public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
        List<PotentialAssignment> assignments = new ArrayList<>();
        for (Object each : enumType.getEnumConstants()) {
            assignments.add(PotentialAssignment.forValue(each.toString(), each));
        }

        return assignments;
    }
}
