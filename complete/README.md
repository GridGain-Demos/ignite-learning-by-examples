# Finished Application: Learning Apache Ignite Through Examples

Follow the steps below to start an Ignite cluster and experiment with various APIs and capabilities. This application
uses the World database schema to demonstrate how to access and process data with SQL, compute, key-value APIs. It also
shows how to update records of cities with Ignite transactions and subscribe for data changes that happen on server nodes.

If you want to gain more practical experience then use this 
[project with source code templates](https://github.com/GridGain-Demos/ignite-learning-by-examples/tree/master/template)
to finish building the application on your own.

## Starting Ignite Cluster

* Download [Apache Ignite 2.8.1](https://ignite.apache.org/download.cgi) or later
* Download GridGain Control Center agent and [put it into the Ignite libs folder](https://www.gridgain.com/docs/control-center/latest/connect-ignite-cluster).
* Start a 2-nodes cluster using the configuration from the project `scripts/ignite_world.sql`
* [Connect](https://www.gridgain.com/docs/control-center/latest/connect-ignite-cluster) Control Center with your Ignite cluster.

## Example #1: Load World Database and Run SQL Queries

Load the data of the World database in the cluster:

* Connect with SQLline: `{ignite}/bin/sqlline.[sh|bat] --verbose=true -u jdbc:ignite:thin://127.0.0.1/`
* Load the database: `!run {root_of_this_project}/complete/scripts/ignite_world.sql`

Execute several SQL commands to query the cluster data:

* Get the most populated countries:

```
SELECT name, MAX(population) as max_pop FROM country
 GROUP BY name, population ORDER BY max_pop DESC LIMIT 3
```

* Get the most populated cities of the provided countries' list joining records from several tables (the joins are co-located
because `affinityKey` was set in `CREATE TABLE City` command): 

```
SELECT country.name, city.name, MAX(city.population) as max_pop FROM country
     JOIN city ON city.countrycode = country.code
     WHERE country.code IN ('USA','RUS','CHN','KOR','MEX','AUT','BRA','ESP','JPN')
     GROUP BY country.name, city.name 
     ORDER BY max_pop DESC LIMIT 3;
```

### Update Ignite Records with SQL Commands

* Query a random record like this: `SELECT name, population FROM City WHERE id = 4000;`
* Update the population: `UPDATE City SET population = 5000 WHERE id = 4000;`
* Check that the change took affect: `SELECT name, population FROM City WHERE id = 4000;`

## Example 2: Using Key-Value APIs for CRUD operations

Even though the database was created using DDL syntax, you still can create  POJOs and work with the clustered data using 
key-value, compute and other non-SQL APIs of Ignite.

Open `App1KeyValue` application that demonstrates several key-value techniques. The application reads and updates several
records using basic `cache.get/put` commands as well as more advanced techniques such as `EntryProcessors`.

Once you execute the application, switch back to the GridGain Control Center SQL
screen and run the following SQL queries to get the updated records but via SQL:

* `SELECT name, continent, headofstate FROM Country WHERE code = 'GBR';` - must return Boris Johnson
* `SELECT name, continent, headofstate FROM Country WHERE code = 'USA';` - must return Donald Trump

## Example 3: Updating Records with Distributed ACID Transactions

With Apache Ignite distributed transactions you can modify records of different caches/tables and cluster nodes atomically.

Open `App2Transactions` application that demonstrates how to use the transactional APIs if you need to 
update a population of two or more Cities atomically. 

## Example 4: Calculating Average Population with Compute APIs

Ignite compute APIs allow us creating custom Java tasks and execute them on server nodes. Depending on the application logic
complexity, this minimizes or fully eliminates data movement over the network and, thus, can improve performance greatly.

Open `App3Compute` source code template that calculates an average population across all the cities of a specific country.
The application does this by running a compute task on a server node that keeps all the cities of a country. 

## Example 5: Receiving Notifications on Data Changes with Continuous Queries APIs

An application can subscribe to receive updates from server nodes whenever any application record gets changed. Ignite 
supports Continuous Queries APIs for that purpose.

Open `App4ContinousQueries` that starts an application that subscribes for notifications about cities' population
changes. The application does this by running a special continuous query. 


## Example 6: No Data Loss On Cluster Crashes or Restarts

If you check [ignite-config.xml](https://github.com/GridGain-Demos/ignite-learning-by-examples/blob/master/complete/cfg/ignite-config.xml) file,
you'll notice that the cluster not only cached the application records in memory but also persisted them in Ignite native persistence.
It means that the cluster can tolerate crashes and restarts not losing a bit of data.

Do the following:

* Stop the 2-nodes cluster
* * Stop `App4ContinousQueries` application
* Bring the cluster back
* Go to the GridGain Control Center SQL screen and execute any previous query such as the one below:

```
SELECT country.name, city.name, MAX(city.population) as max_pop FROM country
     JOIN city ON city.countrycode = country.code
     WHERE country.code IN ('USA','RUS','CHN','KOR','MEX','AUT','BRA','ESP','JPN')
     GROUP BY country.name, city.name 
     ORDER BY max_pop DESC LIMIT 3;
``` 

The cluster will execute the query successfully by serving all the records from disk! You don't need to warm-up the main
memory on restarts. The cluster becomes operation as soon as all the nodes are inter-connected.

