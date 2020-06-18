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
 * This application demonstrates several key-value techniques. Search for `DEMO_TODO`
 * tags throughout the source code to finish building the application and resolve all possible exceptions the application can generate when
 * you start it incomplete.
 *
 * 1) Enable peer-class-loading and restart the cluster.
 * 2) Update the logic of the CountryEntryProcessor to use BinaryObject interfaces instead of POJO classes.
 * This will help to eliminate ClassNotFoundException on the server side.
 */
public class AppTemplate1KeyValue {

    /**
     *
     * @param args
     */
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
     * DEMO_TODO #1: perform the steps below to resolve <code>Caused by: class org.apache.ignite.IgniteCheckedException: SQL_PUBLIC_COUNTRY...</code>
     * exception:
     *
     * a) Add the <code>VALUE_TYPE=org.gridgain.examples.model.Country</code> parameter to <code>CREATE TABLE Country</code>
     * command of the `template/scripts/ignite_world.sql` initialization script.
     *
     * b) Add the <code>VALUE_TYPE=org.gridgain.examples.model.City</code> and
     * <code>KEY_TYPE=org.gridgain.examples.model.CityKey</code> parameters to the <code>CREATE TABLE City</code> command.
     *
     * c) Reload the database schema with SQLline: <code>`!run {root_of_this_project}/template/scripts/ignite_world.sql`</code>
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
        Country unitedKingdom = countryCache.get("GBR");

        System.out.println("Current Prime Minister: " + unitedKingdom.getHeadOfState());

        /**
         * DEMO_TODO #2: uncomment two lines below to be able to update an object field with the EntryProcessor
         * that changes the record right on a server node. Restart the application to see
         * the `Caused by: java.lang.ClassNotFoundException: org.gridgain.examples.AppTemplate1KeyValue$CountryEntryProcessor`
         * exception being reported. Do this to make this logic functional:
         *
         * a) Enable the peer-class-loading feature by adding <code><property name="peerClassLoadingEnabled" value="true"/></code>
         * parameter to the `template/cfg/ignite-config.xml`. Restart your 2-nodes cluster with the updated configuration settings.
         * Launch the application again. The previously reported exception won't longer appear as long as the class of the
         * `CountryEntryProcessor` will be sent to the servers over the network automatically. However, you'll see another exception...
         *
         * b) `Caused by: java.lang.ClassNotFoundException: org.gridgain.examples.model.Country` will be the next exception
         * generated by the application. Presently, the peer-class-loading feature cannot transfer over the wire classes of the objects
         * you read from Ignite caches and attempt to deserialize. Update the `CountryEntryProcessor` implementation by using
         * `BinaryObjects` API for records access on the server nodes. Consult with GridGain/Ignite documentation on how
         * to leverage those APIs or check {@see App1KeyValue} application of the `complete` project.
         */

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
