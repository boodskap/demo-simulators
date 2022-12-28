FROM ubuntu:20.04
LABEL maintainer="Boodskap <platform@boodskap.io>"

ARG VERSION

RUN apt-get update && apt-get -y install openjdk-11-jdk-headless && rm -rf /var/lib/apt

ENV APPVER=${VERSION}

add demo-simulators-${APPVER}.jar ${HOME}/

CMD exec /usr/bin/java -jar demo-simulators-${APPVER}.jar
