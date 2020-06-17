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
import org.gridgain.examples.model.Country;

/**
 * TODO:
 * 1) Enable peer-class-loading and restart the cluster.
 * 2) Update the logic of the CountryEntryProcessor to use BinaryObject interfaces instead of POJO classes.
 * This will help to eliminate ClassNotFoundException on the server side.
 */
public class AppTemplate1KeyValue {

    public static void main(String args[]) {
        Ignition.setClientMode(true);

        Ignite client = Ignition.start("template/cfg/ignite-config.xml");

        IgniteCache<String, Country> countryCache = client.cache("Country");


        getPutCountry(countryCache);

        updateSingleField(countryCache);


        client.close();
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

    private static void updateSingleField(IgniteCache<String, Country> countryCache) {
        // TODO:
        // 1) Update the logic of the CountryEntryProcessor to use BinaryObject interfaces instead of POJO classes.
        // This will help to eliminate ClassNotFoundException on the server side.

        Country unitedKingdom = countryCache.get("GBR");

        System.out.println("Current Prime Minister: " + unitedKingdom.getHeadOfState());

        String newPrimeMinister = countryCache.invoke("GBR", new CountryEntryProcessor());

        System.out.println("New Prime Minister: " + newPrimeMinister);
    }

    private static class CountryEntryProcessor implements EntryProcessor<String, Country, String> {

        @Override public String process(MutableEntry<String, Country> entry,
            Object... arguments) throws EntryProcessorException {

            entry.getValue().setHeadOfState("Boris Johnson");

            return entry.getValue().getHeadOfState();
        }
    }
}
