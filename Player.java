package com.ncsu.zookeeper.submit;

import java.io.IOException;
import java.util.Scanner;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;

public class Player {
	
	private static ZooKeeper zk;
	
	public static ZooKeeper connect(String host) throws IOException, InterruptedException, IllegalStateException {
		zk = new ZooKeeper(host, 5000, new Watcher() {
			
			public void process(WatchedEvent event) {
				if(event.getState() == KeeperState.SyncConnected) {
					System.out.println(event.getType());					
				}
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
	
	public static void scoreAutomation(String count,String delay,String score) {
		
	}
	
	public static void main(String[] args) throws IllegalStateException, IOException, InterruptedException, KeeperException {
		
		Scanner input = new Scanner(System.in);

		String host ="", pName = "", count = "",delay = "", score = "";
		
		boolean isUserInput = false;
		
		if(args.length<2) {
			System.err.println("Enter valid number of arguments:");
		}else if(args.length == 2) {
			 host = args[0];
			 pName = args[1];
			 isUserInput = true;
		}else {
			 host = args[1];
			 pName = args[2];
			 count = args[3];
			 delay = args[4];
			 score = args[5];
			 
		}
		
		zk = connect(host);
		
		String path = "/players";
		
		if(!isValidZnode(path)) {
			create(path,"".getBytes(),true);
		}
		path+="/"+pName;
		if(!isValidZnode(path)) {
			create(path,"".getBytes(),true);
		}
		create(path+"/online","".getBytes(),false);
		if(isUserInput) {
			while(true){
				System.out.println("Please enter a score: ");
				score = input.next();
				create(path+"/"+System.currentTimeMillis(),score.getBytes(),true);
			}
		}else {
			scoreAutomation(count,delay,score);
		}
		input.close();
	}
}
