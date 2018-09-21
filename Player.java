
import java.io.IOException;
import java.util.Random;
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
	
	public static void main(String[] args) throws IllegalStateException, IOException, InterruptedException, KeeperException {
		
		Scanner input = new Scanner(System.in);

		String host ="", pName = "",scoreString = "";
		int count = 0,score = 0;
		long delay = 0;
		boolean isUserInput = false;
		
		if(args.length<2) {
			System.err.println("Enter valid number of arguments:");
		}else if(args.length == 2) {
			 host = args[0];
			 pName = args[1];
			 isUserInput = true;
		}else {
			 host = args[0];
			 pName = args[1];
			 count = Integer.parseInt(args[2]);
			 delay = Long.parseLong(args[3]);
			 scoreString = args[4];
			 score =  Integer.parseInt(scoreString);
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
				scoreString = input.next();
				create(path+"/"+System.currentTimeMillis(),scoreString.getBytes(),true);
			}
		}else {
			
			Random rnd = new Random();
			
			int rdelay = 0,rscore;
			while(count>0) {
				
				Thread.sleep(rdelay);
				
					do {
					  double val = rnd.nextGaussian() * 500/3 + delay;
					  rdelay = (int) Math.round(val);
					} while (rdelay <= 0);
					
					
					do {
					  double val = rnd.nextGaussian() * 500/3 + score;
					  rscore = (int) Math.round(val);
					} while (rscore <= 0);
					
					System.out.println("Created score :"+rscore+" delay :"+ rdelay);
					create(path+"/"+System.currentTimeMillis(),(rscore+"").getBytes(),true);
					
					count--;
			}			
		}
		input.close();
	}
}
