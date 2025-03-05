#!/bin/bash
# Fetch project name and current version from pom.xml
projectName="zigad/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)"
currentVersion=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "Current project version: $currentVersion. Enter the new release version:"
read version

# Ensure version format is valid
if [[ ! "$version" =~ ^v?[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: Version must be in the format vX.Y.Z or X.Y.Z (e.g., v1.2.3 or 1.2.3)"
    exit 1
fi

# Ensure Docker is running
if ! docker info &>/dev/null; then
    echo "Error: Docker is not running! Start Docker and retry."
    exit 1
fi

# Ensure the 'v' prefix
version=${version#v}  # Remove 'v' if present
version="v$version"   # Always add 'v' prefix

echo "Setting version to: $version"
./mvnw org.codehaus.mojo:versions-maven-plugin:2.18.0:set -DnewVersion=$version -DgenerateBackupPoms=false

echo "Creating release branch: release/$version"
git checkout -b "release/$version"
git add .
git commit -m "Prepare release $version"
git push origin "release/$version"

# Merge release branch into master
echo "Merging release/$version into master"
git checkout master
git pull --rebase origin master  # Ensure master is up to date
git merge --no-ff "release/$version" -m "Release $version"
git tag -s -a "$version" -m "$projectName $version"
git push origin master
git push origin "$version"

# Build and push Docker image
echo "Cleaning and packaging service"
./mvnw clean package
echo "Building Docker image for version: $version"
docker build -f src/main/docker/Dockerfile.jvm -t "$projectName:$version" --platform=linux/amd64 .
echo "Pushing Docker image for version: $version"
docker push "$projectName:$version"

echo "Do you want to build this version as latest and push it to Docker? (y)"
read pushLatest

if [[ "$pushLatest" =~ ^[Yy]$ ]]; then
  echo "Building latest Docker image"
  docker build -f src/main/docker/Dockerfile.jvm -t "$projectName:latest" --platform=linux/amd64 .
  echo "Pushing latest Docker image"
  docker push "$projectName:latest"
fi

# Set next development version
nextDevVersion=$(echo $version | awk -F. '{print $1"."$2"."$3+1"-SNAPSHOT"}')
echo "Setting next development version: $nextDevVersion"
./mvnw org.codehaus.mojo:versions-maven-plugin:2.18.0:set -DnewVersion=$nextDevVersion -DgenerateBackupPoms=false
git commit -a -m "Set next development version: $nextDevVersion"
git push origin master

echo "✅ Release $version completed and pushed to Docker repo."
