# transactionstats

Project to expose an API for creating transactions and providing windowed statistics over it in constant time.

You will need `gradle` to run it. If you don't have it installed you can use `gradlew` provided on the root directory of this project.

# Building and testing
With a working copy of gradle you can use the following command:
```
    gradle clean build
```

## Running
With a working gradle copy you can use the command:
```
    gradle bootRun
```

This is going to expose the application on `localhost:8080`.

Alternatively if you want to run from the final artifact you can use:
```
    gradle clean build
    java -jar build/libs/transactionstats.jar
```
