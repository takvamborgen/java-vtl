package no.ssb.vtl.script.operations;

/*-
 * ========================LICENSE_START=================================
 * Java VTL
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Kohl
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import no.ssb.vtl.model.AbstractDatasetOperation;
import no.ssb.vtl.model.Component;
import no.ssb.vtl.model.DataPoint;
import no.ssb.vtl.model.DataStructure;
import no.ssb.vtl.model.Dataset;
import no.ssb.vtl.model.Order;
import no.ssb.vtl.script.error.VTLRuntimeException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;

/**
 * Union operator
 */
public class UnionOperation extends AbstractDatasetOperation {
    
    public UnionOperation(Dataset... dataset) {
        this(asList(dataset));
    }

    public UnionOperation(List<Dataset> datasets) {
        super(datasets);
        Iterator<Dataset> iterator = datasets.iterator();
        DataStructure firstDataStructure = iterator.next().getDataStructure();
        while (iterator.hasNext())
            checkDataStructures(firstDataStructure, iterator.next().getDataStructure());
    }
    
    @Override
    protected DataStructure computeDataStructure() {
        return getChildren().get(0).getDataStructure();
    }
    
    private void checkDataStructures(DataStructure baseDataStructure, DataStructure nextDataStructure) {
        // Identifiers and attribute should be equals in name, role and type.
        Set<String> requiredNames = nonAttributeNames(baseDataStructure);
        Set<String> providedNames = nonAttributeNames(nextDataStructure);

        checkArgument(
                requiredNames.equals(providedNames),
                "dataset was incompatible with the required data structure, missing: %s, unexpected %s",
                Sets.difference(requiredNames, providedNames),
                Sets.difference(providedNames, requiredNames)
        );

        Map<String, Component.Role> requiredRoles = Maps.filterKeys(baseDataStructure.getRoles(), requiredNames::contains);
        Map<String, Component.Role> providedRoles = Maps.filterKeys(nextDataStructure.getRoles(), requiredNames::contains);
    
        checkArgument(
                requiredRoles.equals(providedRoles),
                "dataset was incompatible with the required data structure, missing: %s, unexpected %s",
                Sets.difference(requiredRoles.entrySet(), providedRoles.entrySet()),
                Sets.difference(providedRoles.entrySet(), requiredRoles.entrySet())
        );

        Map<String, Class<?>> requiredTypes = Maps.filterKeys(baseDataStructure.getTypes(), requiredNames::contains);
        Map<String, Class<?>> providedTypes = Maps.filterKeys(nextDataStructure.getTypes(), requiredNames::contains);
    
        checkArgument(
                requiredTypes.equals(providedTypes),
                "dataset was incompatible with the required data structure, missing: %s, unexpected %s",
                Sets.difference(requiredTypes.entrySet(), providedTypes.entrySet()),
                Sets.difference(providedTypes.entrySet(), requiredTypes.entrySet())
        );

    }

    private Set<String> nonAttributeNames(DataStructure dataStructure   ) {
        return Maps.filterValues(dataStructure.getRoles(), role -> role != Component.Role.ATTRIBUTE).keySet();
    }
            
    @Override
    public Stream<DataPoint> getData() {
        List<Dataset> datasets = getChildren();
        if (datasets.size() == 1) {
            return datasets.get(0).getData();
        }

        if (datasets.size() == 2) {
            if (datasets.get(0).equals(datasets.get(1))) {
                return datasets.get(0).getData();
            }
        }

        // TODO: Attribute propagation.
        Order order = Order.create(getDataStructure())
                .putAll(rolesInOrder(getDataStructure(), Order.Direction.DESC, Component.Role.IDENTIFIER, Component.Role.MEASURE))
                .build();
        Set<DataPoint> bucket = Sets.newTreeSet(order);
        Set<DataPoint> seen = Collections.synchronizedSet(bucket);
        return getChildren().stream().flatMap(Dataset::getData)
                .peek((o) -> {
                    if (seen.contains(o)) {
                        //TODO: define an error code encoding. See VTL User Manuel "Constraints and errors"
                        throw new VTLRuntimeException("The resulting dataset from a union contains duplicates", "VTL-1xxx", o);
                    }
                })
                .peek(bucket::add);
    }
    
    private Map<Component, Order.Direction> rolesInOrder(DataStructure dataStructure, Order.Direction desc, Component.Role... roles) {
        ImmutableSet<Component.Role> roleSet = Sets.immutableEnumSet(Arrays.asList(roles));
        return dataStructure.values().stream()
                .filter(component -> roleSet.contains(component.getRole()))
                .collect(Collectors.toMap(o -> o, o -> desc));
    }
    
    @Override
    public Optional<Map<String, Integer>> getDistinctValuesCount() {
        return Optional.empty();
    }
    
    @Override
    public Optional<Long> getSize() {
        return Optional.empty();
    }
}
