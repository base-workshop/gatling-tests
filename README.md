# gatling-tests

## Requirements
1. Java SDK 1.7 or later
2. Maven

## Install deps
```
export JAVA_HOME=[PATH TO YOUR JAVA BIN]
mvn clean install
```

## Run
```
mvn clean test -Dgatling.simulation=simulations.workshop.WorkshopSimulation
```
