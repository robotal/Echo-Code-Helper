# Amazon-Echo
Repo for sharing work on Amazon Echo

I. Project Components

To package and run the project, run

mvn package

This will create a zip and a jar of the package. The zip file is to be uploaded to the elastic beanstalk environment from where it will unpack the jar
and run it using the procfile. Any environmental variable changes should be placed in the Procfile.
