# How to Release

release to github and docker-hub

## Prerequisites

1. (user) ssh key with write access to the github repo located in `~/.ssh` (used by `mvn release:prepare` and `mvn release:prepare` directly)
2. Unfortunately, again, github credentials in maven `settings.xml` located in `~/.m2` (used by `github-release-plugin`):
  ```xml
     <servers>
        ...
        <server>
	        <id>github</id>
			<username>GITHUB_USERNAME</username>
        	<password>GITHUB_PASSWORD</password>
        </server>
        ...
     </servers>
  ```
3. Docker Hub credentials in maven `settings.xml` located in `~/.m2` (used by `dockerfile-maven-plugin`):
  ```xml
     <servers>
        ...
        <server>
            <id>docker.io</id>
            <username>DOCKERHUB_USERNAME</username>
            <password>DOCKERHUB_PASSWORD</password>
        </server>
        ...
     </servers>
  ```
  
## Prepare & Release

1. In [pom.xml](/pom.xml) set the properties: `<release.github.description>RELEASE_DESCRIPTION</release.github.description>` 
and `<release.tag>RELEASE_VERSION</release.tag>`.
2. Commit and push to github.
3. From project root, execute: `mvn release:prepare release:perform --batch-mode`


