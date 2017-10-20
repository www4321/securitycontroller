package com.sds.securitycontroller.directory.registry;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sds.securitycontroller.core.internal.GlobalConfig;
import com.sds.securitycontroller.event.manager.EventManager;

public class ZookeeperHelper {

    protected Logger logger = LoggerFactory.getLogger(EventManager.class);
    
	private static ZookeeperHelper instance;
	public static ZookeeperHelper getInstance(){
		if(instance == null)
			instance = new ZookeeperHelper();
		return instance;
	}
	

    // zooKeeper params
    private String zooKeeperHost = "127.0.0.1:2181";
    private ZooKeeper zookeeper;
    private final String rootNode = "/securitycontroller";
    
	
    private ZookeeperHelper(){

        GlobalConfig config = GlobalConfig.getInstance();
        zooKeeperHost = config.zooKeeperHost;
        
        //init zooKeeper nodes
        try {
            zookeeper = new ZooKeeper(zooKeeperHost, 12000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                	logger.debug("receive an event: {}", watchedEvent);
                }
            });
            if (zookeeper.exists(rootNode, true) == null) {
                zookeeper.create(rootNode, rootNode.getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    List<String> getZKChildren(String path) throws KeeperException, InterruptedException {
    	String fpath = String.format("%s%s", rootNode, path);
        return zookeeper.getChildren(fpath, true);
    }
    
    public boolean exist(String path){
    	String fpath = String.format("%s%s", rootNode, path);
    	try {
			return (zookeeper.exists(fpath, true) != null);
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }

    public byte[] readZKNode(String path) throws KeeperException, InterruptedException {
    	String fpath = String.format("%s%s", rootNode, path);
        if (zookeeper.exists(fpath, true) != null) {
            byte[] data = zookeeper.getData(rootNode + path, true, null);
            return data;
        }
        return null;
    }

    public void writeZKNode(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException {
    	String fpath = String.format("%s%s", rootNode, path);
        if (zookeeper.exists(fpath, true) == null) {
            zookeeper.create(fpath, data,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, mode); 
        } else {
            zookeeper.setData(fpath, data, -1);
        }
    }
    

    public void writeZKNode(String path, String data, CreateMode mode) throws KeeperException, InterruptedException {
	    writeZKNode(path, data.getBytes(), mode);
    }


    public void deleteZKNode(String path) throws KeeperException, InterruptedException {
    	String fpath = String.format("%s%s", rootNode, path);
        if (zookeeper.exists(fpath, true) != null) {
            zookeeper.delete(fpath, -1);
        }
    }
}
