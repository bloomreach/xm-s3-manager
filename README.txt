Local development
=================

You can easily run the provided demo with a local development version of the plugin.
First from the project directory execute the below command to install a development version (SNAPSHOT) of the plugin in your local maven repo.
    mvn clean install

Second execute the following command to start the demo utilising the local plugin.
    cd demo && mvn clean verify && mvn -Pcargo.run

Release
=======

The release process is very easy and straight forward. By executing
    mvn release:prepare

you will be prompted to provide the release version (eg. 0.4.0), scm tag (eg. brxm-s3-manager-0.4.0) and next development version (eg. 0.5.0-SNAPSHOT).
Once this process finishes, you should verify there are 2 new commits in your git history like the following
    [maven-release-plugin] prepare release brxm-s3-manager-0.4.0
    [maven-release-plugin] prepare for next development iteration

Finish the release process by executing the below command without removing the release.properties file that was generated from the previous command.
    mvn release:perform

After this command both the release.properties file but also pom backup files will be automatically removed.

Finally, don't forget to update the README.md with the newly released version (eg. 0.4.0) and update in the root pom.xml of the demo project the property <brxm.s3.manager.version> with the next development version (eg. 0.5.0-SNAPSHOT).