# Zookeeper-Distributed-scoreboard
Homework 1


# Compiling .java
javac -cp zookeeper-3.4.12.jar *.java 

# Player execution
java -cp .:zookeeper-3.4.12.jar:slf4j-simple-1.7.25.jar:slf4j-api-1.7.25.jar Player host name

java -cp .:zookeeper-3.4.12.jar:slf4j-simple-1.7.25.jar:slf4j-api-1.7.25.jar Player host "first last"

java -cp .:zookeeper-3.4.12.jar:slf4j-simple-1.7.25.jar:slf4j-api-1.7.25.jar Player host name count delay score

# Watcher execution
java -cp .:zookeeper-3.4.12.jar:slf4j-simple-1.7.25.jar:slf4j-api-1.7.25.jar ScoreWatcher host size

