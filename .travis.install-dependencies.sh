#!/bin/bash
set -e

MAVEN_VERSION=3.3.9
MAVEN_EXECUTOR="./.travis.maven.sh"

wget http://apache.cs.utah.edu/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz

tar -zxf apache-maven-${MAVEN_VERSION}-bin.tar.gz
mv apache-maven-${MAVEN_VERSION} /usr/local
ln -s /usr/local/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/bin/mvn

echo "#!/bin/bash" > ${MAVEN_EXECUTOR}
echo "set -ev" >> ${MAVEN_EXECUTOR}
echo "export M2_HOME=/usr/local/apache-maven-${MAVEN_VERSION}" >> ${MAVEN_EXECUTOR}
echo "mvn" "\"\$@\"" >> ${MAVEN_EXECUTOR}

chmod +x ${MAVEN_EXECUTOR}

echo "Dumping script content:"
echo "---------------------------------------"
cat ${MAVEN_EXECUTOR}
echo "---------------------------------------"

echo "Checking Maven version..."
${MAVEN_EXECUTOR} -v
