/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridgain.examples;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.gridgain.examples.model.Country;

/**
 * The application demonstrates several key-value techniques. It reads and updates several
 * records using basic `cache.get/put` commands as well as more advanced techniques such as `EntryProcessors`.
 */
public class App1KeyValue {

    /**
     * Start the application, connect to the cluster and execute the logic.
     * @param args
     */
    public static void main(String args[]) {
        Ignition.setClientMode(true);

        try (Ignite client = Ignition.start("complete/cfg/ignite-config.xml")) {

            IgniteCache<String, Country> countryCache = client.cache("Country");

            getPutCountry(countryCache);

            updateSingleField(countryCache);
        }
    }

    /**
     * Get and update a record using key-value APIs.
     *
     * @param countryCache cache instance.
     */
    private static void getPutCountry(IgniteCache<String, Country> countryCache) {
        Country usa = countryCache.get("USA");

        System.out.println("Current President: " + usa.getHeadOfState());

        usa.setHeadOfState("Donald Trump");

        countryCache.put("USA", usa);

        System.out.println("New President: " + countryCache.get("USA").getHeadOfState());
    }

    /**
     * Update a record field using EntryProcesser API.
     *
     * @param countryCache cache instance.
     */
    private static void updateSingleField(IgniteCache<String, Country> countryCache) {
        Country unitedKingdom = countryCache.get("GBR");

        System.out.println("Current Prime Minister: " + unitedKingdom.getHeadOfState());

        String newPrimeMinister = countryCache.<String, BinaryObject>withKeepBinary().
            invoke("GBR", new CountryEntryProcessor());

        System.out.println("New Prime Minister: " + newPrimeMinister);
    }

    /**
     * An EntryProcessor implementation that updates the HeadOfState field right on the server node and uses BinaryObject
     * APIs to achieve that.
     */
    private static class CountryEntryProcessor implements EntryProcessor<String, BinaryObject, String> {

        @Override public String process(MutableEntry<String, BinaryObject> entry,
            Object... arguments) throws EntryProcessorException {

            BinaryObjectBuilder builder = entry.getValue().toBuilder();

            builder.setField("headofstate", "Boris Johnson");

            entry.setValue(builder.build());

            return entry.getValue().field("headofstate");
        }
    }
}
