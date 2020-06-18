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

## Example 2: Using Key-Value APIs for CRUD operations

Even though the database was created using DDL syntax, you still can create  POJOs and work with the clustered data using 
key-value, compute and other non-SQL APIs of Ignite.

Open `AppTemplate1KeyValue` source code template that demonstrates several key-value techniques. Search for `DEMO_TODO`
tags in the source code to finish building the application and resolve all possible exceptions the application can generate when
you start it incomplete.

Once you complete all `DEMO_TODOs` making the application fully functional, switch back to the GridGain Control Center SQL
screen and run the following SQL queries to get the updated records but via SQL

* `SELECT name, continent, headofstate FROM Country WHERE code = 'GBR';` - must return Boris Johnson
* `SELECT name, continent, headofstate FROM Country WHERE code = 'USA';` - must return Donald Trump

## Example 3: Updating Records with Distributed ACID Transactions

With Apache Ignite distributed transactions you can modify records of different caches/tables and cluster nodes atomically.
Open `AppTemplate2Transactions` source code template that demonstrates how to use the transactional APIs if you need to 
update a population of two or more Cities atomically. Search for `DEMO_TODO` tags in the source code to finish building 
the application and resolve all possible exceptions the application can generate when you start it incomplete.
