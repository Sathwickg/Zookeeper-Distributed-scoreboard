package com.ncsu.zookeeper.submit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;



class Score{
	boolean isOnline;
	String name;
	long timeStamp;
	int score;

	Score(boolean online, String name, long timeStamp, int score) {
		this.isOnline = online;
		this.name = name;
		this.timeStamp = timeStamp;
		this.score = score;
	}
}

public class ScoreWatcher {
	private static ZooKeeper zk;
	private static String path = "/players";

	private static int maxSize;
	private static boolean isRootWatched;
	private static List<Score> highestScores = new ArrayList<Score>();
	private static List<Score> mostRecentScores= new ArrayList<Score>();
	private static Set<String> watcherSet = new HashSet<String>();;
	private static Set<String> oldPlayers = new HashSet<String>();;


	public static ZooKeeper connect(String host) throws IOException, InterruptedException, IllegalStateException {
		zk = new ZooKeeper(host, 5000, new Watcher() {			
			public void process(WatchedEvent event) {

			}
		});
		return zk;
	}

	public static void close() throws InterruptedException{
		zk.close();
	}

	public static void create(String path, byte[] data,boolean isPersist) throws KeeperException,InterruptedException {
		if(isPersist){
			zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}else {
			zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		}
	}

	public static boolean isValidZnode(String path) throws KeeperException, InterruptedException {
		return zk.exists(path, true)==null?false:true;
	}	


	public static void chainedWatcher(String root, final boolean isRoot) throws InterruptedException,KeeperException {
		zk.getChildren(root, new Watcher() {
			public void process(WatchedEvent we) {	                
				try {
					displayScoreboard();
					if(isRoot) {
						isRootWatched = false;
						String newPlayerName="";
						List<String> players = zk.getChildren(path, false);
						for(int i = 0; i < players.size(); i++) {
							String playerName = players.get(i);
							if(!oldPlayers.contains(playerName)) {
								newPlayerName= playerName;
								break;
							}
						}
						String newPlayer = path + "/" + newPlayerName;
						watcherSet.add(newPlayer);
						System.out.println("CHILD ADDED " + newPlayer);
					}else {
						watcherSet.add(we.getPath());
					}
					if(watcherSet.size() > 0) {
						for(String name: watcherSet)
							chainedWatcher(name, false);
						watcherSet.clear();
					}
					if(!isRootWatched) {
						chainedWatcher(path, true);
						isRootWatched = true;
					}
				} catch(Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		}, null);
	}

	public static void displayScoreboard() throws KeeperException, InterruptedException, NumberFormatException, UnsupportedEncodingException {
		
		List<Score> scores = new ArrayList<Score>();
		List<String> players = zk.getChildren(path, false); 
		for(String player:players) {
			boolean isPlayerOnline = zk.exists(path+"/"+player+"/online", true)==null?false:true;
			List<String> playerChilds = zk.getChildren(path+"/"+player, false);
			System.out.println("size"+playerChilds.size());
			for(String child:playerChilds) {								
				if(child.equalsIgnoreCase("online"))continue;				
				scores.add(new Score(isPlayerOnline,player,Long.parseLong(child),Integer.parseInt(new String(zk.getData(path + "/" + player + "/" + child, false, null), "UTF-8"))));
			}
		}
		List<Score> mostRecentScores = sortList(scores,true);
		List<Score> maxMostRecentScores = (mostRecentScores.size()>maxSize)?mostRecentScores.subList(0, maxSize):new ArrayList<Score>(mostRecentScores);

		System.out.println("Most recent scores");
		System.out.println("------------------");
		for(int i = 0; i < maxMostRecentScores.size(); i++) {
			System.out.println(maxMostRecentScores.get(i).name + "\t" + maxMostRecentScores.get(i).score + " " + (maxMostRecentScores.get(i).isOnline ? "**" : ""));
		}
		
		List<Score> highestScores = sortList(scores,false);
		List<Score> maxHighestScores = (highestScores.size()>maxSize)?highestScores.subList(0, maxSize):new ArrayList<Score>(highestScores);

		
		System.out.println();
		System.out.println("Highest scores");
		System.out.println("--------------");
		for(int i = 0; i < maxHighestScores.size(); i++) {
			System.out.println(maxHighestScores.get(i).name + "\t" + maxHighestScores.get(i).score + " " + (maxHighestScores.get(i).isOnline ? "**" : ""));
		}

	}
	
	public static List<Score> sortList(List<Score> scores,final boolean isTimestamp) {
		List<Score> tempList = new ArrayList<Score>(scores);
        Collections.sort(scores, new Comparator<Score>(){
            public int compare(Score p1, Score p2) {
            	if(isTimestamp) {
            		 return (int) (p2.timeStamp - p1.timeStamp);
            	}else {
            		return (int) (p2.score - p1.score);
            	}               
            }
        });		
		return tempList;
	}
	
	public static void main(String[] args) {
		if(args.length!=2) {
			System.out.println("Please enter valid arguments. Exiting...");
			System.exit(-1);
		}
		String host = args[0];
		maxSize = Integer.valueOf(args[1]);

		try {
			zk = connect(host);

			if(isValidZnode(path)) {
				List<String> players = zk.getChildren(path, false);
				chainedWatcher(path, true);
				isRootWatched = true;
				for(int i=0;i<players.size();i++) {
					chainedWatcher(path + "/" + players.get(i), false);
					oldPlayers.add(players.get(i));
				}
				
				displayScoreboard();
			}		
			Scanner sc = new Scanner(System.in);
            while(true) {
                sc.next();
            }
		}catch(Exception e) {

		}
	}
}
