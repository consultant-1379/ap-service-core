#! /bin/bash

source docker-env-functions.sh

print_title() {
   echo "`date` @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
   echo "`date` $1"
   echo "`date` @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
}


wait_postgres
wait_container "neo4j"

print_title "RUNNING JBOSS PRE-START *******"
