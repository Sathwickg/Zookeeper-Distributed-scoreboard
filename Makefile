all: player.class watcher.class

player.class: Player.java
	javac -cp zookeeper-3.4.12.jar Player.java

watcher.class: ScoreWatcher.java
	javac -cp zookeeper-3.4.12.jar ScoreWatcher.java