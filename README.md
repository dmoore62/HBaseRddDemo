# HBaseRddDemo

## Overview

The purpose of this repo is to experiment with reading out of HBase table and into a Spark RDD in Java

## Setup

In order to test this code, follow these steps:
1. Download and run the [Hortonworks Sandbox](https://hortonworks.com/downloads/#sandbox)
..* Either the Virtualbox or the Docker setups will work
2. Launch and wait for all services to start
..* HBase may need to be manually started
..* Test the HBase is running by running `$~ hbase shell`
3. Create and populate a HBase table named 'iemployee' with a single column qualifer 'personal'
..* The Docker image may already have this table
4. Run the main class of this repo from your host machine and the rows will be printed from the RDD to the console
..* Also, the JAR can be built and uploaded to the Sandbox and run there

## Notes

The following features need to be addressed in this POC:
1. Mapping column qualifiers and types to an RDD column
2. Outputing an RDD's contents back to HBase
