1. Edit your .bashrc and change MAVEN_OPTS to -Xmx3G
2. Execute this (or run runme.sh)
mvn clean verify -Declipse-ip-site=file:./../../../org.eclipse.ip/org.eclipse.ip.p2/target/repository

