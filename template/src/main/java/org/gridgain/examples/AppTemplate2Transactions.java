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

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteTransactions;
import org.apache.ignite.Ignition;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.gridgain.examples.model.City;
import org.gridgain.examples.model.CityKey;

/**
 * TODO:
 * 1) Annotate the affinity key of the CityKey Object.
 * 2) Finish the implementation of the transaction.
 */
public class AppTemplate2Transactions {
    /**
     * Start the application, connect to the cluster and execute the logic.
     * @param args
     */
    public static void main(String args[]) {
        Ignition.setClientMode(true);

        try (Ignite client = Ignition.start("complete/cfg/ignite-config.xml")) {

            IgniteCache<CityKey, City> cityCache = client.cache("City");

            updateCitiesPopulation(client, cityCache);
        }
    }

    private static void updateCitiesPopulation(Ignite client, IgniteCache<CityKey, City> cityCache) {
        IgniteTransactions txs = client.transactions();

        CityKey newYorkPK = new CityKey(3793, "USA");
        CityKey losAngelesPK = new CityKey(3794, "USA");

        int migratedResidentsNumber = 10_000;

        try (Transaction tx = txs.txStart(TransactionConcurrency.PESSIMISTIC, TransactionIsolation.REPEATABLE_READ)) {
            City newYork = cityCache.get(newYorkPK);
            City losAngeles = cityCache.get(losAngelesPK);

            System.out.println("Population Before [NY=" + newYork.getPopulation() + ", LA=" + losAngeles.getPopulation() + "]");

            // TODO

            tx.commit();

            System.out.println("Transaction has been committed");
        }

        City newYork = cityCache.get(newYorkPK);
        City losAngeles = cityCache.get(losAngelesPK);

        System.out.println("Population After [NY=" + newYork.getPopulation() + ", LA=" + losAngeles.getPopulation() + "]");
    }
}
