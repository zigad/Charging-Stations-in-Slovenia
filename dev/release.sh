#!/bin/bash
projectName="zigad/charging-stations-in-slovenia"
currentVersion=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Current version of project is $currentVersion. Enter version you want to release:"
read version
echo "Setting version to: $version"
./mvnw org.codehaus.mojo:versions-maven-plugin:2.10.0:set -DnewVersion=$version -DgenerateBackupPoms=false
echo "Building for version: $version"
./mvnw clean package
echo "Releasing for version: $version"
git commit -a -m"Prepare release $version"
git tag -a $version -m "$projectName $version"
git push
git push origin $version
echo "Building docker image for version: $version"
docker build -f src/main/docker/Dockerfile.jvm -t $projectName":"$version --platform=linux/amd64 .
echo "Pushing docker image for version $version"
docker push $projectName":"$version
echo "Released and pushed to docker repo."