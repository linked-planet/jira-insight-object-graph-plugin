#!/bin/sh

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <mavenSettingsFile>"
    exit 1
fi

MAVEN_SETTINGS_FILE=$1

if ! grep -q "<servers>" "$MAVEN_SETTINGS_FILE"; then
    sed -i~ "/<settings>/ a<servers>\n</servers>" "$MAVEN_SETTINGS_FILE"
fi

SERVER_LINK_TIME_ARTIFACTORY="<server><id>link-time.artifactory</id><username>$LINK_TIME_ARTIFACTORY_USERNAME</username><password>$LINK_TIME_ARTIFACTORY_PASSWORD</password></server>"

sed -i~ "/<servers>/ a$SERVER_LINK_TIME_ARTIFACTORY" "$MAVEN_SETTINGS_FILE"
