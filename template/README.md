# Source Code Templates: Learning Apache Ignite Through Examples

Follow the steps below to start an Ignite cluster and experiment with various APIs and capabilities.

## Starting Ignite Cluster and Connect to GridGain Control Center

* Download [Apache Ignite 2.8.1](https://ignite.apache.org/download.cgi) or later
* Download GridGain Control Center agent and [put its libs into the Ignite libs folder](https://www.gridgain.com/docs/control-center/latest/connect-ignite-cluster).
* [Start a 2-nodes cluster](https://www.gridgain.com/docs/latest/getting-started/quick-start/java#starting-a-gridgain-node)
 using `{root_of_this_project}/template/cfg/ignite-config.xml`. 
* [Connect](https://www.gridgain.com/docs/control-center/latest/connect-ignite-cluster) Control Center with your Ignite cluster.

## Example 1: Using SQL for Schema Creation and CRUD operations

### Loading World Database Schema 

Load the data of the World database in the cluster:

* Connect with SQLline: `{ignite}/bin/sqlline.[sh|bat] --verbose=true -u jdbc:ignite:thin://127.0.0.1/`
* Load the database: `!run {root_of_this_project}/template/scripts/ignite_world.sql`

Open [GridGain Control Center SQL screen](https://www.gridgain.com/docs/control-center/latest/querying) and 
execute several SQL commands:

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

This query returns an incorrect result because both `City` and `Country` tables are not co-located. As a temporary workaround,
[enable non-colocated joins](https://www.gridgain.com/docs/control-center/latest/querying#non-colocated-joins) for the query to
see that Seoul, Sao Pauolo and Shangai are, in fact, the most inhabited cities.

### Configure Affinity Co-Location and Reload Database Schema

Non-colocated joins are not efficient and can impact performance greatly. We need to co-located `City` and `Country` tables
to support co-located joins for data stored in those tables:

* Add the `AFFINITY_KEY=CountryCode` parameter to the `CREATE TABLE City` command of the 
`{root_of_this_project}/template/scripts/ignite_world.sql` initialization script.
* Reload the database with SQLline: `!run {root_of_this_project}/template/scripts/ignite_world.sql`
* Disable [non-colocated joins flag in Control Center SQL screen](https://www.gridgain.com/docs/control-center/latest/querying#non-colocated-joins)
* Execute the same query that joins data properly and will return a correct result (Seoul, Sao Pauolo and Shangai will be reported as
the most populated cities):

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

## Access Records Using Key-Value APIs 

Even though the database was created using DDL syntax, you still can define have POJOs and work with the date using 
key-value, compute and other non-SQL APIs of Ignite. Run `TODO_Name` application that demonstrates how to read and update
records with key-value calls.
