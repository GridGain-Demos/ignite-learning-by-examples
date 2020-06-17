# Learning Apache Ignite Through Examples (complete project that is ready for usage)

Follow the steps below to start an Ignite cluster and experiment with various APIs and capabilities.

## Starting Ignite Cluster

* Download [Apache Ignite 2.8.1](https://ignite.apache.org/download.cgi) or later
* Download GridGain Control Center agent and [put it into the Ignite libs folder](https://www.gridgain.com/docs/control-center/latest/connect-ignite-cluster).
* Start a 2-nodes cluster using the configuration from the project `scripts/ignite_world.sql`
* [Connect](https://www.gridgain.com/docs/control-center/latest/connect-ignite-cluster) Control Center with your Ignite cluster.

## Load World Database and Run SQL Queries

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

Update a record using the UPDATE command:

* Query a random record like this: `SELECT name, population FROM City WHERE id = 4000;`
* Update the population: `UPDATE City SET population = 5000 WHERE id = 4000;`
* Check that the change took affect: `SELECT name, population FROM City WHERE id = 4000;`

## Access Records Using Key-Value APIs 

Even though the database was created using DDL syntax, you still can define have POJOs and work with the date using 
key-value, compute and other non-SQL APIs of Ignite. Run `TODO_Name` application that demonstrates how to read and update
records with key-value calls.
