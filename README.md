# Twitter Sentiment Analysis

An [application continuum](https://www.appcontinuum.io/) style example using Kotlin and Ktor
that includes a single web application with two background workers.

* Basic web application
* Data analyzer
* Data collector

### Technology stack

This codebase is written in a language called [Kotlin](https://kotlinlang.org) that is able to run on the JVM with full
Java compatibility.
It uses the [Ktor](https://ktor.io) web framework, and runs on the [Netty](https://netty.io/) web server.
HTML templates are written using [Freemarker](https://freemarker.apache.org).
The codebase is tested with [JUnit](https://junit.org/) and uses [Gradle](https://gradle.org) to build a jarfile.
The [pack cli](https://buildpacks.io/docs/tools/pack/) is used to build a [Docker](https://www.docker.com/) container
which is deployed to
[Google Cloud](https://cloud.google.com/) on Google's Cloud Platform.

## Getting Started

### Install dependencies

1. Install and start [Docker](https://docs.docker.com/desktop/#download-and-install).
1. Run Docker Compose.

   ```bash
   docker-compose up
   ```

### Run migrations

1. Migrate the test database
   with [Flyway](https://flywaydb.org/documentation/usage/commandline/#download-and-installation).

   ```bash
   FLYWAY_CLEAN_DISABLED=false flyway -user=tweet -password=tweet -url="jdbc:postgresql://localhost:5432/tweet_test" -locations=filesystem:databases/tweet clean migrate
   ```

1. Migrate the development database with Flyway.

   ```bash
   FLYWAY_CLEAN_DISABLED=false flyway -user=tweet -password=tweet -url="jdbc:postgresql://localhost:5432/tweet_development" -locations=filesystem:databases/tweet clean migrate
   ```

1. Populate test and development data with a product scenario.

   ```bash
   PGPASSWORD=tweet psql -h'127.0.0.1' -p 5432 -Utweet -f applications/basic-server/src/test/resources/scenarios/endpoints.sql tweet_test
   PGPASSWORD=tweet psql -h'127.0.0.1' -p 5432 -Utweet -f applications/basic-server/src/test/resources/scenarios/endpoints.sql tweet_development
   ```

## Development

1. Build a Java Archive (jar) file.
   ```bash
   ./gradlew clean build
   ```

1. Configure the port that each server runs on.
   ```bash
   export PORT=8881
   ```

Run the servers locally using the below examples.

### Web application

```bash
java -jar applications/basic-server/build/libs/basic-server-1.0-SNAPSHOT.jar
```

### Data collector

```bash
java -jar applications/data-collector-server/build/libs/data-collector-server-1.0-SNAPSHOT.jar
```

### Data analyzer

```bash
java -jar applications/data-analyzer-server/build/libs/data-analyzer-server-1.0-SNAPSHOT.jar
```

## Production

Building a Docker container and running with Docker.

## Buildpacks

1. Install the [pack](https://buildpacks.io/docs/tools/pack/) CLI.
   ```bash
   brew install buildpacks/tap/pack
   ```

1. Build using pack.
   ```bash
   pack build kotlin-ktor-starter --builder heroku/buildpacks:20
   ```

1. Run with docker.
   ```bash
   docker run  -e "PORT=8882" -e "APP=applications/basic-server/build/libs/basic-server-1.0-SNAPSHOT.jar" kotlin-ktor-starter
   ```

That's a wrap for now.
