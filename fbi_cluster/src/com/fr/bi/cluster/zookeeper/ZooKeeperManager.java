package com.fr.bi.cluster.zookeeper;import com.finebi.cube.common.log.BILoggerFactory;import com.fr.base.FRContext;import com.fr.bi.cluster.retry.RetryNTimes;import com.fr.bi.cluster.utils.ClusterEnv;import com.fr.bi.cluster.utils.PropertiesUtils;import com.fr.bi.cluster.wrapper.ZooKeeperConfig;import com.fr.bi.cluster.wrapper.ZooKeeperHandler;import com.fr.bi.cluster.wrapper.ZooKeeperWrapper;import com.fr.bi.cluster.zookeeper.watcher.BICubeStatusWatcher;import com.fr.general.FRLogger;import org.apache.zookeeper.Watcher;import org.apache.zookeeper.ZooKeeper;import java.io.File;import java.io.IOException;import java.util.Iterator;import java.util.Map;import java.util.Properties;import java.util.concurrent.ConcurrentHashMap;/** * Created by Hiram on 2015/2/26. */public class ZooKeeperManager implements ZooKeeperManagerInterface {    public static final String CREATE_ANALYSE_WATCHER = "analyseOperationWatcher";    public static final String CUBE_WATCHER = "cubeWatcher";    public static final String MASTER = "biMaster";    public static final String WORKER = "biWorker";    public static final String CUBE_STATUS = "cubeStatus";    private static final FRLogger LOG = FRContext.getLogger();    private static final int TICK_TIME = 60000;    private static ZooKeeperManager ourInstance;    private Map<String, BIWatcher> watcherContainer;    private ZooKeeperWrapper zk;    private String ip;    private String port;    private ZooKeeperManager() {        File zookeeperInfo = ClusterEnv.getZookeeperInfoFile();        Properties properties = PropertiesUtils.load(zookeeperInfo);        watcherContainer = new ConcurrentHashMap<String, BIWatcher>();        ip = properties.getProperty("ip");        port = properties.getProperty("port");        ZooKeeperConfig config = generateConfig();        ZooKeeperHandler handler = new ZooKeeperHandler(config, new BIDefaultWatcher());        ZooKeeperWrapper wrapper = new ZooKeeperWrapper(handler);        wrapper.initial(new RetryNTimes(10, 6000));        zk = wrapper;        if (ClusterEnv.isCluster()) {            initialWatchers();        }    }    public static synchronized ZooKeeperManager getInstance() {        if (ourInstance != null) {            return ourInstance;        } else {            synchronized (ZooKeeperManager.class) {                if (ourInstance == null) {                    ourInstance = new ZooKeeperManager();                }                return ourInstance;            }        }    }    private ZooKeeperConfig generateConfig() {        ZooKeeperConfig config = new ZooKeeperConfig();        config.setConnectString(ip + ":" + port);        config.setTickTime(TICK_TIME);        return config;    }    public BIWatcher getWatcher(String watcherName) {        if (watcherContainer.containsKey(watcherName)) {            return watcherContainer.get(watcherName);        }        return null;    }    public void addWatchers(String key, BIWatcher watcher) {        watcherContainer.put(key, watcher);    }    private void initialWatchers() {        watcherContainer.put(CUBE_STATUS, new BICubeStatusWatcher());    }    private void registerWatchers() {        Iterator<BIWatcher> it = watcherContainer.values().iterator();        while (it.hasNext()) {            registerWatcher(it.next());        }    }    public String getPort() {        return port;    }    public String getIp() {        return ip;    }    private void init() throws ZooKeeperException {    }    @Override    public ZooKeeperWrapper getZooKeeper() {        return zk;    }    private ZooKeeperException castException(Exception e) {        return castException(e.getMessage(), e);    }    private ZooKeeperException castException(String msg, Exception e) {        return new ZooKeeperException(e.getMessage(), e);    }    private void registerWatcher(BIWatcher watcher) {        try {            watcher.init(zk);        } catch (Exception ex) {            BILoggerFactory.getLogger().error(ex.getMessage(), ex);        }        watcher.setWatcherRegistered(true);    }    @Override    public void notifyCubeUpdateConfig() {        try {            watcherContainer.get(CUBE_WATCHER).rewriteData(String.valueOf(System.currentTimeMillis()).getBytes(), -1);        } catch (Exception ex) {            BILoggerFactory.getLogger().error(ex.getMessage(), ex);        }    }    @Override    public void notifyUpdateAnalyse() {        try {            watcherContainer.get(CREATE_ANALYSE_WATCHER).rewriteData(String.valueOf(System.currentTimeMillis()).getBytes(), -1);        } catch (Exception ex) {            BILoggerFactory.getLogger().error(ex.getMessage(), ex);        }    }    /**     * 生成一个zookeeper。     *     * @return     */    public ZooKeeper generateZookeeper(Watcher defaultWatcher) {        try {            return new ZooKeeper(getIp(), TICK_TIME, defaultWatcher);        } catch (IOException ex) {            BILoggerFactory.getLogger().error(ex.getMessage(), ex);        }        return null;    }    @Override    public void startListen() throws ZooKeeperException {        registerWatchers();    }}//class MyThread extends Thread {//    ZooKeeperManager manager;////    MyThread(ZooKeeperManager manager) {//        this.manager = manager;//    }////    public void run() {//        try {//            String s = manager.getZooKeeper().create("/watch", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);//            System.err.println("s: " + s);//        } catch (KeeperException e) {//            BILoggerFactory.getLogger().error(e.getMessage(), e);//        } catch (InterruptedException e) {//            BILoggerFactory.getLogger().error(e.getMessage(), e);//        } catch (Exception e) {//            e.printStackTrace();//        }//    }//}