/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.gridgain.examples;

import java.util.Collections;
import javax.cache.Cache;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;

/**
 * The application calculates an average population across all the cities of a specific country.
 * The application does this by running a compute task on a server node that keeps all the cities of a country.
 */
public class App3Compute {
    /**
     * Start the application, connect to the cluster and execute the logic.
     * @param args
     */
    public static void main(String args[]) {
        Ignition.setClientMode(true);

        try (Ignite client = Ignition.start("complete/cfg/ignite-config.xml")) {
            calculateAverageCountryPopulation(client, "BRA");
            calculateAverageCountryPopulation(client, "RUS");
        }
    }

    private static void calculateAverageCountryPopulation(Ignite client, String countryCode) {
        //Getting a cluster node and a partition that store a primary copy of all the cities with 'countryCode'.
        Affinity<String> affinity = client.affinity("Country");

        ClusterNode node = affinity.mapKeyToNode(countryCode);

        int partition = affinity.partition(countryCode);

        //Scheduling the task for calculation on that primary node.
        int[] result = client.compute().affinityCall(Collections.singleton("Country"), partition, new AvgPopulationCalculationTask(
            countryCode, partition));

        System.out.println("Finished task execution [country = " + countryCode + ", avgPopulation=" +
            result[0] + ", citiesNumber=" + result[1] + "]");
    }
    /**
     * Compute job that calculates average population across all the cities of a given country.
     * The job iterates only over a single data partition.
     */
    private static class AvgPopulationCalculationTask implements IgniteCallable<int[]> {
        @IgniteInstanceResource
        private Ignite ignite;

        private String countryCode;

        private int partition;

        public AvgPopulationCalculationTask(String countryCode, int partition) {
            this.partition = partition;
            this.countryCode = countryCode;
        }

        @Override public int[] call() throws Exception {
            System.out.println("Calculating average [country=" + countryCode + ", partition=" + partition +
                ", node = " + ignite.cluster().localNode().id() + "]");

            //Accessing object records with BinaryObject interface that avoids a need of deserialization and doesn't
            //require to keep models' classes on the server nodes.
            IgniteCache<BinaryObject, BinaryObject> cities = ignite.cache("City").withKeepBinary();

            ScanQuery<BinaryObject, BinaryObject> scanQuery = new ScanQuery<>(partition,
                new IgniteBiPredicate<BinaryObject, BinaryObject>() {
                    @Override public boolean apply(BinaryObject key, BinaryObject object) {
                        //Filtering out cities of other countries that stored in the same partition.
                        return key.field("CountryCode").equals(countryCode);
                    }
                });

            //Extra hint to Ignite that the data is available locally.
            scanQuery.setLocal(true);

            //Calculation average population across the cities.
            QueryCursor<Cache.Entry<BinaryObject, BinaryObject>> cursor = cities.query(scanQuery);

            long totalPopulation = 0;
            int citiesNumber = 0;

            for (Cache.Entry<BinaryObject, BinaryObject> entry: cursor) {
                totalPopulation += (int)entry.getValue().field("Population");
                citiesNumber++;
            }

            return citiesNumber == 0 ? null : new int[] {(int)(totalPopulation/citiesNumber), citiesNumber};
        }
    }
}
