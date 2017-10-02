package no.ssb.vtl.model;

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

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class StaticDataset implements Dataset {

    private final DataStructure structure;
    private final List<DataPoint> data;

    private StaticDataset(DataStructure structure, List<DataPoint> data) {
        this.structure = structure;
        this.data = data;
    }

    @Override
    public Stream<DataPoint> getData() {
        return data.stream();
    }

    @Override
    public Optional<Map<String, Integer>> getDistinctValuesCount() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> getSize() {
        return Optional.of((long) data.size());
    }

    @Override
    public DataStructure getDataStructure() {
        return structure;
    }

    public static ValueBuilder create(DataStructure structure) {
        return new ValueBuilder(structure);
    }

    public static ValueBuilder create(DataStructure.Builder structure) {
        return new ValueBuilder(structure.build());
    }

    public static StructureBuilder create() {
        return new StructureBuilder();
    }

    public static class StructureBuilder {

        DataStructure.Builder builder = DataStructure.builder();

        public StructureBuilder addComponent(String key, Component.Role role, Class<?> type) {
            builder.put(key, role, type);
            return this;
        }

        public ValueBuilder addPoints(Object... values) {
            return new ValueBuilder(builder.build()).addPoints(values);
        }

        public ValueBuilder addPoints(VTLObject... values) {
            return new ValueBuilder(builder.build()).addPoints(values);
        }

        public ValueBuilder addPoints(DataPoint point) {
            return new ValueBuilder(builder.build()).addPoints(point);
        }

        public StructureRolesBuilder withName(String... names) {
            return new StructureRolesBuilder(Arrays.asList(names));
        }

    }

    // make writing tests easier.
    public static class StructureRolesBuilder {

        private final List<String> names;

        private StructureRolesBuilder(List<String> names) {
            this.names = names;
        }

        public StructureTypesBuilder andRoles(Component.Role... roles) {
            return new StructureTypesBuilder(names, Arrays.asList(roles));
        }
    }

    // make writing tests easier.
    public static class StructureTypesBuilder {

        private final List<String> names;
        private final List<Component.Role> roles;

        private StructureTypesBuilder(List<String> names, List<Component.Role> roles) {
            checkArgument(names.size() == roles.size(), "inconsistent roles count.");
            this.names = checkNotNull(names);
            this.roles = checkNotNull(roles);
        }

        public ValueBuilder andTypes(Class<?>... types) {
            List<Class<?>> typesList = Arrays.asList(types);
            checkArgument(roles.size() == typesList.size(), "inconsistent types count.");
            DataStructure.Builder builder = DataStructure.builder();
            for (int i = 0; i < typesList.size(); i++)
                builder.put(names.get(i), roles.get(i), typesList.get(i));
            return new ValueBuilder(builder.build());
        }
    }

    public static class ValueBuilder {

        private final DataStructure structure;
        private final ArrayList<DataPoint> data = Lists.newArrayList();

        public ValueBuilder(DataStructure structure) {
            this.structure = checkNotNull(structure);
        }

        public ValueBuilder addPoints(Object... values) {
            return addPoints(Arrays.stream(values).map(VTLObject::of).collect(Collectors.toList()));
        }

        public ValueBuilder addPoints(VTLObject... values) {
            return addPoints(DataPoint.create(Arrays.asList(values)));
        }

        public ValueBuilder addPoints(DataPoint point) {
            structure.asMap(point); // only to check.
            data.add(point);
            return this;
        }

        public StaticDataset build() {
            return new StaticDataset(structure, data);
        }
    }
}
