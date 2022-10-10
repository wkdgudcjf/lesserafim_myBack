#!/bin/sh

./gradlew clean bootJar

export BUILD_PATH_FOR_API=./build/libs
export SNAPSHOT_NM_FOR_API=*.jar

export NOW="release-"$(date +'%Y%m%d%H%M')
export GIT_COMMIT=$(git rev-parse --short=8 HEAD)
export SERVICE_NAME="lesserafimapi"

mv $BUILD_PATH_FOR_API/$SNAPSHOT_NM_FOR_API $BUILD_PATH_FOR_API/$SERVICE_NAME-$NOW-$GIT_COMMIT.jar
aws s3 cp $BUILD_PATH_FOR_API/${SERVICE_NAME}-$NOW-$GIT_COMMIT.jar s3://weverse-partner-deploy/${SERVICE_NAME}/${BRANCH}/
