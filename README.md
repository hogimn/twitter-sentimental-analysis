# Kotlin ktor example

A [application continuum](https://www.appcontinuum.io/) style example application using Kotlin and Ktor that includes a single
 web application with 2 background workers. Deployed via [Fresh Cloud](https://www.freshcloud.com/).

* Basic web application
* Data analyzer
* Data collector

The example depends on the below technologies -

* Language [Kotlin](https://kotlinlang.org)
* Web Framework [Ktor](https://ktor.io) with [Netty](https://netty.io/) and [Freemarker](https://freemarker.apache.org)
* Build tool [Gradle](https://gradle.org)
* Testing tools [JUnit](https://junit.org/)
* Production [Heroku](https://www.heroku.com)

(May-2021) We've been using the kotlin-ktor-starter to explore identity-aware proxies and kubernetes -
and have included a few kubernetes yaml files that describe ingress, service, and backend
objects. We're also using [buildpacks](https://buildpacks.io/docs/tools/pack/cli/pack_build/) to
build and then run the application on our kubernetes cluster.

## Configuration

### Server

Configure the port that each server runs on.

```bash
export PORT=8881
```

Run servers locally using the below example -

```bash
java -jar applications/basic-server/build/libs/basic-server-1.0-SNAPSHOT.jar
```

## Docker

Building with docker.

```bash
docker build -t kotlin-ktor-starter .
```

Running with docker.

```bash
docker run  -e "PORT=8882" -e "APP=applications/basic-server/build/libs/basic-server-1.0-SNAPSHOT.jar" kotlin-ktor-starter
```

## Buildpacks

Installing the [pack](https://buildpacks.io/docs/tools/pack/) CLI

```bash
brew install buildpacks/tap/pack
```

Build the application using pack.

```bash
pack build kotlin-ktor-starter --builder heroku/buildpacks:20
```

Fresh cloud deployment and pipline files are located in `deployment`.
Google cloud kubernetes deployment and service files located in `applications/basic-server`.