echo 'Packaging SecurityController local version...'
mvn package
echo 'Packaging SecurityController storm (all in one jar file) version...'
mvn -f storm-jar-pom.xml package
