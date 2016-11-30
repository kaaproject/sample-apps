#!/bin/bash
set -ev

MAVEN_VERSION=3.3.9

wget http://apache.cs.utah.edu/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz

tar -zxf apache-maven-${MAVEN_VERSION}-bin.tar.gz
mv apache-maven-${MAVEN_VERSION} /usr/local
ln -s /usr/local/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn

echo "export M2_HOME=/usr/local/apache-maven-${MAVEN_VERSION}" >> ~/.profile
source ~/.profile

echo "Maven is on version `mvn -v`"
