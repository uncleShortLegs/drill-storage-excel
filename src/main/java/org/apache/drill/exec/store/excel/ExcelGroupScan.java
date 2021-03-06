/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.excel;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Preconditions;
import org.apache.drill.common.exceptions.ExecutionSetupException;
import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.exec.physical.PhysicalOperatorSetupException;
import org.apache.drill.exec.physical.base.AbstractGroupScan;
import org.apache.drill.exec.physical.base.PhysicalOperator;
import org.apache.drill.exec.physical.base.ScanStats;
import org.apache.drill.exec.physical.base.SubScan;
import org.apache.drill.exec.proto.CoordinationProtos;
import org.apache.drill.exec.store.StoragePluginRegistry;

import java.io.IOException;
import java.util.List;

/**
 * Created by mnasyrov on 11.08.2017.
 */
@JsonTypeName("excel-scan")
@SuppressWarnings("WeakerAccess")
public class ExcelGroupScan extends AbstractGroupScan {

    private final ExcelScanSpec spec;
    private List<SchemaPath> columns;
    private final ExcelStoragePlugin storagePlugin;
    private final ExcelStoragePluginConfig storagePluginConfig;

    @JsonCreator
    private ExcelGroupScan(@JsonProperty("userName") String userName,
                          @JsonProperty("spec") ExcelScanSpec ExcelScanSpec,
                          @JsonProperty("storagePluginConfig") ExcelStoragePluginConfig storagePluginConfig,
                          @JsonProperty("columns") List<SchemaPath> columns,
                          @JacksonInject StoragePluginRegistry pluginRegistry) throws IOException, ExecutionSetupException {
        this (userName, (ExcelStoragePlugin) pluginRegistry.getPlugin(storagePluginConfig), ExcelScanSpec, columns);
    }

    @JsonProperty
    public ExcelScanSpec getSpec() {
        return spec;
    }

    @JsonProperty
    public List<SchemaPath> getColumns() {
        return columns;
    }

    @JsonProperty
    public ExcelStoragePluginConfig getStoragePluginConfig() {
        return storagePluginConfig;
    }

    @JsonIgnore
    public ExcelStoragePlugin getStoragePlugin() {
        return storagePlugin;
    }

    public ExcelGroupScan(String userName, ExcelStoragePlugin plugin, ExcelScanSpec spec, List<SchemaPath> columns) {
        super(userName);
        this.storagePlugin = plugin;
        this.storagePluginConfig = storagePlugin.getConfig();
        this.spec = spec;
        this.columns = columns == null ? ALL_COLUMNS : columns;
    }

    public ExcelGroupScan(ExcelGroupScan that) {
        super(that);
        this.storagePlugin = that.storagePlugin;
        this.storagePluginConfig = that.storagePluginConfig;
        this.spec = that.spec;
        this.columns = that.columns;
    }

    @Override
    public ExcelGroupScan clone(List<SchemaPath> columns) {
        ExcelGroupScan scan = new ExcelGroupScan(this);
        scan.columns = columns == null ? ALL_COLUMNS : columns;
        return scan;
    }

    @Override
    public void applyAssignments(List<CoordinationProtos.DrillbitEndpoint> list) throws PhysicalOperatorSetupException {
        //Ассаймент к нодам дрила не поддерживается
    }

    @Override
    public SubScan getSpecificScan(int i) throws ExecutionSetupException {
        return new ExcelSubScan(getUserName(), spec, storagePlugin, columns);
    }

    @Override
    public int getMaxParallelizationWidth() {
        return 1;
    }

    @Override
    public String getDigest() {
        return toString();
    }

    @Override
    public String toString() {
        return "ExcelGroupScan [ExcelScanSpec="
                + spec
                + ", columns="
                + columns + "]";
    }

    @Override
    public PhysicalOperator getNewWithChildren(List<PhysicalOperator> list) throws ExecutionSetupException {
        Preconditions.checkArgument(list.isEmpty());
        return new ExcelGroupScan(this);
    }

    @Override
    public boolean supportsPartitionFilterPushdown() {
        return true;
    }

    @Override
    public boolean canPushdownProjects(List<SchemaPath> columns) {
        return true;
    }

    @Override
    public ScanStats getScanStats() {
        return ScanStats.TRIVIAL_TABLE;
    }

}
