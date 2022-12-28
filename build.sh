#!/bin/bash

export VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

mvn clean install

docker build --build-arg VERSION=${VERSION} -t boodskapiot/simulators:${VERSION} target -f Dockerfile

if [ "$1" == "push"  ]
then
    docker push boodskapiot/simulators:${VERSION}
else
	echo "Not pushing, use *push* as second argument"
fi
