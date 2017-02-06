package no.ssb.vtl.script.operations;

import com.google.common.collect.Maps;
import no.ssb.vtl.model.Component;
import no.ssb.vtl.model.DataPoint;
import no.ssb.vtl.model.DataStructure;
import no.ssb.vtl.model.Dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.*;

public class CheckOperation implements Dataset{

    private static final List<String> ROWS_TO_RETURN_POSSIBLE_VALUES = Arrays.asList("not_valid", "valid", "all");
    private static final List<String> COMPONENTS_TO_RETURN_POSSIBLE_VALUES = Arrays.asList("measures", "condition");

    private final Dataset dataset;
    private final String rowsToReturn;
    private final String componentsToReturn;
    private final String errorCode;
    private final Integer errorLevel;
    private DataStructure cache;

    public CheckOperation(Dataset dataset, String rowsToReturn, String componentsToReturn, String errorCode, Integer errorLevel) {
        this.dataset = checkNotNull(dataset, "dataset was null");

        if (rowsToReturn != null) {
            checkArgument(!rowsToReturn.isEmpty(), "the rowsToReturn argument was empty");
            checkArgument(ROWS_TO_RETURN_POSSIBLE_VALUES.contains(rowsToReturn),
                    "the rowsToReturn argument has incorrect value: %s. Allowed values: %s",
                    rowsToReturn, Arrays.toString(ROWS_TO_RETURN_POSSIBLE_VALUES.toArray()));
            this.rowsToReturn = rowsToReturn;
        } else {
            this.rowsToReturn = "not_valid";
        }

        if (componentsToReturn != null) {
            checkArgument(!componentsToReturn.isEmpty(), "the componentsToReturn argument was empty");
            checkArgument(COMPONENTS_TO_RETURN_POSSIBLE_VALUES.contains(componentsToReturn),
                    "the componentsToReturn argument has incorrect value: %s. Allowed values: %s",
                    componentsToReturn, Arrays.toString(COMPONENTS_TO_RETURN_POSSIBLE_VALUES.toArray()));
            this.componentsToReturn = componentsToReturn;
        } else {
            this.componentsToReturn = "measures";
        }

        checkArgument(!("measures".equals(componentsToReturn) && "all".equals(rowsToReturn)), "cannot use 'all' with 'measures' parameter");

        checkDataStructure(this.dataset);

        if (errorCode != null) {
            checkArgument(!errorCode.isEmpty(), "the errorCode argument was empty");
            this.errorCode = errorCode;
        } else {
            this.errorCode = null;
        }

        this.errorLevel = errorLevel;
    }

    private void checkDataStructure(Dataset dataset) {
        int noIdentifiers = Maps.filterValues(dataset.getDataStructure().getRoles(), role -> role == Component.Role.IDENTIFIER)
                .size();
        checkArgument(noIdentifiers > 0, "dataset does not have identifier components");

        long noBooleanMeasures = dataset.getDataStructure().values().stream().filter(c -> c.isMeasure() && c.getType().equals(Boolean.class)).count();
        checkArgument(noBooleanMeasures < 2, "dataset has too many boolean measure components");
        checkArgument(noBooleanMeasures > 0, "dataset has no boolean measure component");
    }

    @Override
    public DataStructure getDataStructure() {
        if (cache == null) {
            Map<String, Component.Role> newRoles = new HashMap<>(dataset.getDataStructure().getRoles());
            Map<String, Class<?>> newTypes = new HashMap<>(dataset.getDataStructure().getTypes());
            Set<String> oldNames = dataset.getDataStructure().keySet();

            if (componentsToReturn == null || "measures".equals(componentsToReturn)) {
                removeAllComponentsButIdentifiersAndMeasures(newRoles, newTypes, oldNames);
            } else if ("condition".equals(componentsToReturn)) {
                removeAllComponentsButIdentifiers(newRoles, newTypes, oldNames);
                addComponent("CONDITION", newRoles, newTypes, Component.Role.MEASURE, Boolean.class);
            }

            addComponent("errorcode", newRoles, newTypes, Component.Role.ATTRIBUTE, String.class);
            addComponent("errorlevel", newRoles, newTypes, Component.Role.ATTRIBUTE, Integer.class);

            BiFunction<Object, Class<?>, ?> converter = dataset.getDataStructure().converter();
            cache = DataStructure.of(converter, newTypes, newRoles);
        }

        return cache;
    }

    private void addComponent(String componentName, Map<String, Component.Role> newRoles,
                              Map<String, Class<?>> newTypes, Component.Role role, Class<?> aClass) {
        newRoles.put(componentName, role);
        newTypes.put(componentName, aClass);
    }

    private void removeAllComponentsButIdentifiers(Map<String, Component.Role> newRoles,
                                                   Map<String, Class<?>> newTypes, Set<String> oldNames) {
        for (String oldName : oldNames) {
            if (newRoles.get(oldName) != Component.Role.IDENTIFIER) {
                newRoles.remove(oldName);
                newTypes.remove(oldName);
            }
        }
    }

    private void removeAllComponentsButIdentifiersAndMeasures(Map<String, Component.Role> newRoles,
                                                   Map<String, Class<?>> newTypes, Set<String> oldNames) {
        for (String oldName : oldNames) {
            if (newRoles.get(oldName) != Component.Role.IDENTIFIER && newRoles.get(oldName) != Component.Role.MEASURE) {
                newRoles.remove(oldName);
                newTypes.remove(oldName);
            }
        }
    }

    @Override
    public Stream<Tuple> get() {
        Stream<Tuple> tupleStream = dataset.get();

        DataPoint errorCodeDataPoint = getDataStructure().wrap("errorcode", errorCode);
        DataPoint errorLevelDataPoint = getDataStructure().wrap("errorlevel", errorCode);

        if (rowsToReturn == null || "not_valid".equals(rowsToReturn)) {
            tupleStream = tupleStream.filter(tuple -> tuple.values().stream()
                    .filter(dataPoint -> dataPoint.getComponent().isMeasure() && dataPoint.getType().equals(Boolean.class))
                    .anyMatch(dataPoint -> dataPoint.get().equals(false))).peek(e -> System.out.println("value: " + e));
        } else if ("valid".equals(rowsToReturn)) {
            tupleStream = tupleStream.filter(tuple -> tuple.values().stream()
                    .filter(dataPoint -> dataPoint.getComponent().isMeasure() && dataPoint.getType().equals(Boolean.class))
                    .anyMatch(dataPoint -> dataPoint.get().equals(true))).peek(e -> System.out.println("value: " + e));
        } //else if ("all".equals(rowsToReturn)) //all is not filtered

        if (componentsToReturn == null || "measures".equals(componentsToReturn)) {
            tupleStream = tupleStream.map(dataPoints -> {
                        List<DataPoint> dataPointsNewList = new ArrayList<>(dataPoints);
                        dataPointsNewList.add(errorCodeDataPoint);
                        dataPointsNewList.add(errorLevelDataPoint);
                        return Tuple.create(dataPointsNewList);
                    });
        } else if ("condition".equals(componentsToReturn)) {
            //TODO here
//            new DataPoint(getDataStructure().get("CONDITION")) {
//                @Override
//                public Object get() {
//                    return dataPoints.values().stream()
//                            .filter(dp -> dp.getRole() == Component.Role.MEASURE && dp.getType().equals(Boolean.class))
//                            .findFirst().orElseThrow(() -> new IllegalArgumentException("DataPoint of type Boolean and role MEASURE not found in stream"));
//                }
//            });
        }

        return tupleStream;
    }
}
