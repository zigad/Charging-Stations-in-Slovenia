#!/bin/bash
set -e  # Exit immediately if a command fails
set -o pipefail  # Catch pipeline errors

# Fetch project name and current version from pom.xml
projectName="zigad/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)"
currentVersion=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "Current project version: $currentVersion. Enter the new release version:"
read version

# Ensure version format is valid
if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Error: Version format must be X.Y.Z (e.g., 1.2.3)"
    exit 1
fi

# Ensure Docker is running
if ! docker info &>/dev/null; then
    echo "Error: Docker is not running! Start Docker and retry."
    exit 1
fi

# Rollback on error
trap 'echo "Something went wrong. Rolling back..."; git reset --hard; git checkout develop; exit 1' ERR

# Create a release branch from develop
echo "Creating release branch: release/$version"
git checkout develop
git pull origin develop
git checkout -b "release/$version"

# Set the new release version
echo "Setting project version to: $version"
./mvnw org.codehaus.mojo:versions-maven-plugin:2.10.0:set -DnewVersion=$version -DgenerateBackupPoms=false

# Commit and push the release branch
echo "Committing version change: $projectName"
git commit -a -m "Prepare release v$version"
git push origin "release/$version"

# Create PR to merge release/$version into master
echo "Creating PR: release/$version → master"
gh pr create --base master --head release/$version --title "Release v$version" --body "Merging release v$version into master"

echo "Waiting for PR approval before proceeding..."
read -p "Press Enter when the PR is merged into master..."

# Merge release branch into master
echo "Merging release/$version into master"
git checkout master
git pull origin master
git merge --no-ff "release/$version" -m "Release v$version"
git tag -a "v$version" -m "$projectName v$version"
git push origin master
git push origin "v$version"

# Create PR to merge master back into develop
echo "Creating PR: master → develop"
gh pr create --base develop --head master --title "Sync master to develop (Release v$version)" --body "Merging release v$version back to develop"

echo "Waiting for PR approval before proceeding..."
read -p "Press Enter when the PR is merged into develop..."

# Set next snapshot version
nextDevVersion=$(echo $version | awk -F. '{print $1"."$2"."$3+1"-SNAPSHOT"}')
echo "Setting next development version: $nextDevVersion"
./mvnw org.codehaus.mojo:versions-maven-plugin:2.10.0:set -DnewVersion=$nextDevVersion -DgenerateBackupPoms=false

# Commit and push snapshot version
echo "Committing next development version: $nextDevVersion"
git commit -a -m "Set next development version: $nextDevVersion"
git push origin develop

# Clean up the release branch
echo "Cleaning up release branch: release/$version"
git branch -d "release/$version"
git push origin --delete "release/$version"

# Build Docker image
echo "Building Docker image for version: v$version"
docker build -f src/main/docker/Dockerfile.jvm -t $projectName":v$version" --platform=linux/amd64 .

# Push Docker image to registry
echo "Pushing Docker image for version: v$version"
docker push $projectName":v$version"

echo "🎉 Release process for v$version completed successfully! 🎉"
