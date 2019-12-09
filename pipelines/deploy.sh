#!/bin/sh

set -e

VERSION=$1

get_groupId_from_maven() {
  mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout | tail -n 1
}

get_artifactId_from_maven() {
  mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout | tail -n 1
}

get_version_from_maven() {
  mvn help:evaluate -Dexpression=project.version -q -DforceStdout | tail -n 1
}

echo "Fetching groupId from Maven"
groupId=$(get_groupId_from_maven)
echo "Fetching artifactId from Maven"
artifactId=$(get_artifactId_from_maven)

if [ "${VERSION}" = "" ]; then
  echo "Fetching version from Maven"
  VERSION=$(get_version_from_maven)
fi

# download artifact from artifactory
gavcUrl="$ARTIFACTORY_CONTEXT_URL/api/search/gavc?g=${groupId}&a=${artifactId}&v=${VERSION}"
echo "GAVC URL = ${gavcUrl}"

descriptorUrl=$(curl -s -X GET -u "${ARTIFACTORY_USERNAME}":"${ARTIFACTORY_PASSWORD}" "${gavcUrl}" | grep "uri" | cut -d '"' -f4 | sort -r | grep ".*[0-9]\.jar" | head -n 1)
echo "Descriptor URL = ${descriptorUrl}"

downloadUrl=$(curl -s -X GET -u "${ARTIFACTORY_USERNAME}":"${ARTIFACTORY_PASSWORD}" "${descriptorUrl}" | grep downloadUri | cut -d '"' -f4)
echo "Download URL = ${downloadUrl}"

echo "Downloading JAR file from Artifactory ..."
curl -X GET -u "${ARTIFACTORY_USERNAME}":"${ARTIFACTORY_PASSWORD}" "${downloadUrl}" -O

# install plugin via upm
fileName=${downloadUrl#*${VERSION}/}
echo "File Name = ${fileName}"
echo "Installing plugin ..."
mvn -Ddeploy.url="$DEPLOY_URL" -Ddeploy.username="$DEPLOY_USERNAME" -Ddeploy.password="$DEPLOY_PASSWORD" \
  upm:uploadPluginFile -DpluginKey="$groupId.$artifactId" -DpluginFile="$fileName" \
  upm:reindex