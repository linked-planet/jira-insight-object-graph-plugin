#!/bin/sh

set -e

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <developerConnection>"
    exit 1
fi

DEVELOPER_CONNECTION=$1

get_version_from_maven() {
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout | tail -n 1
}

mvn build-helper:parse-version versions:set "-DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.incrementalVersion}" versions:commit
version=$(get_version_from_maven)
echo "${version}" > release.version
git add pom.xml && git commit -m "[skip ci] release v${version}"
mvn clean deploy scm:tag \
    "-DaltReleaseDeploymentRepository=artifactory.libs-release-local::$ARTIFACTORY_CONTEXT_URL/libs-release-local" \
    "-DaltSnapshotDeploymentRepository=artifactory.libs-snapshot-local::$ARTIFACTORY_CONTEXT_URL/libs-snapshot-local" \
    "-Ddeveloper.connection=scm:git:$DEVELOPER_CONNECTION" \
    -Pci
mvn build-helper:parse-version versions:set "-DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion}-SNAPSHOT" versions:commit
version=$(get_version_from_maven)
git add pom.xml && git commit -m "[skip ci] set development version ${version}" && git push
