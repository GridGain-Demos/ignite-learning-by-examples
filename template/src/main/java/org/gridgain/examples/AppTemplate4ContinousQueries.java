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

import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.query.ContinuousQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.gridgain.examples.model.City;
import org.gridgain.examples.model.CityKey;

/**
 * TODO: complete the implementation of the remote filter and local listener. The filter has send notifications only if
 * the population field gets changed. The listener has to deserialize BinaryObjects to POJO.
 */
public class AppTemplate4ContinousQueries {
    /**
     * Start the application, connect to the cluster and execute the logic.
     *
     * @param args
     */
    public static void main(String args[]) {
        Ignition.setClientMode(true);

        Ignite client = Ignition.start("complete/cfg/ignite-config.xml");

        subscribeForDataUpdates(client);

        SqlFieldsQuery query = new SqlFieldsQuery("UPDATE City SET population = population - 10 " +
            "WHERE name = 'Los Angeles'");

        client.cache("City").query(query);

        System.out.println("Updated the city record");
    }

    private static void subscribeForDataUpdates(Ignite client) {
        ContinuousQuery<BinaryObject, BinaryObject> query = new ContinuousQuery<>();

        query.setLocalListener(new ChangesListener());
        query.setRemoteFilterFactory(new PopulationChangesFilter());

        client.cache("City").withKeepBinary().query(query);

        System.out.println("Subscribed for the notifications");
    }

    private static class ChangesListener implements CacheEntryUpdatedListener<BinaryObject, BinaryObject> {
        @Override public void onUpdated(
            Iterable<CacheEntryEvent<? extends BinaryObject, ? extends BinaryObject>> events) throws CacheEntryListenerException {

            for (CacheEntryEvent<? extends BinaryObject, ? extends BinaryObject> event : events) {
                CityKey key = event.getKey().deserialize();
                City value = event.getValue().deserialize();

                System.out.println("City record has been changed [key=" + key + ", value = " + value + ']');
            }
        }
    }

    private static class PopulationChangesFilter implements Factory<CacheEntryEventFilter<BinaryObject, BinaryObject>> {
        @Override public CacheEntryEventFilter<BinaryObject, BinaryObject> create() {
            return new CacheEntryEventFilter<BinaryObject, BinaryObject>() {
                @Override
                public boolean evaluate(CacheEntryEvent<? extends BinaryObject, ? extends BinaryObject> e) {
                    // Notify the application only if the population has been changes.
                    if (!e.getOldValue().<Integer>field("population").equals(
                        e.getValue().<Integer>field("population")))
                        return true;

                    // Don't send a notification in all other cases
                    return false;
                }
            };
        }
    }
}
