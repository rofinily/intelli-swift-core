package com.fr.bi.manager;import com.finebi.cube.common.log.BILoggerFactory;import com.fr.base.FRContext;import com.fr.stable.project.ProjectConstants;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.io.InputStream;import java.util.ArrayList;import java.util.List;import java.util.Map;import java.util.Properties;import java.util.concurrent.ConcurrentHashMap;/** * Created by Hiram on 2015/3/18. */public class PerformancePlugManager implements PerformancePlugManagerInterface {    private static final Logger logger = LoggerFactory.getLogger(PerformancePlugManager.class);    private final static String PERFORMANCE = "performance";    public static int DEFAULT_DEPLOY_MODE_ON = 4096;    public static int DEFAULT_DEPLOY_MODE_OFF = -1;    private static PerformancePlugManager ourInstance = new PerformancePlugManager();    private boolean isInit;    private Properties properties = null;    private boolean isControl = false;    private long timeout = Long.MAX_VALUE;    private String message = "The server is busy,Please try again later. ";    private ThreadLocal<LocalAdrrBean> localAdrrThreadLocal = new ThreadLocal<LocalAdrrBean>();    private Map<String, List<Long>> calculateMap = new ConcurrentHashMap<String, List<Long>>();    private boolean uniqueThread = false;    private boolean returnEmptyIndex = false;    private boolean isSearchPinYin = true;    private boolean isGetTemplateScreenCapture = true;    private boolean isControlMaxMemory = false;    private int maxNodeCount = Integer.MAX_VALUE;    private boolean useMultiThreadCal = false;    //	private String message = "当前模板计算量大或服务器繁忙，请点击上面清除按钮清除条件或稍后再试";    private boolean diskSort = false;    private long diskSortDumpThreshold = 1l << 15;    private int biThreadPoolSize = 1;    private boolean useStandardOutError = false;    private boolean verboseLog = true;    private boolean useLog4JPropertiesFile = false;    private String serverJarLocation = "";    private int deployModeSelectSize = DEFAULT_DEPLOY_MODE_OFF;    private PerformancePlugManager() {        init();    }    public static PerformancePlugManager getInstance() {        return ourInstance;    }    private void init() {        try {            InputStream in = FRContext.getCurrentEnv().readBean("plugs.properties", ProjectConstants.RESOURCES_NAME);            if (in == null) {                return;            }            properties = new Properties();            properties.load(in);            setTimeoutConfig(properties);            returnEmptyIndex = getBoolean(PERFORMANCE + ".emptyWhenNotSelect", false);            isSearchPinYin = getBoolean(PERFORMANCE + ".isSearchPinYin", true);            isGetTemplateScreenCapture = getBoolean(PERFORMANCE + ".isGetTemplateScreenCapture", true);            isControlMaxMemory = getBoolean(PERFORMANCE + ".isControlMaxMemory", isControlMaxMemory);            useMultiThreadCal = getBoolean(PERFORMANCE + ".useMultiThreadCal", useMultiThreadCal);            maxNodeCount = getInt(PERFORMANCE + ".maxNodeCount", maxNodeCount);            diskSortDumpThreshold = getLong(PERFORMANCE + ".diskSortDumpThreshold", diskSortDumpThreshold);            diskSort = getBoolean(PERFORMANCE + ".useDiskSort", false);            biThreadPoolSize = getInt(PERFORMANCE + ".biThreadPoolSize", biThreadPoolSize);            useStandardOutError = getBoolean(PERFORMANCE + ".useStandardOutError", useStandardOutError);            verboseLog = getBoolean(PERFORMANCE + ".verboseLog", verboseLog);            useLog4JPropertiesFile = getBoolean(PERFORMANCE + ".useLog4JPropertiesFile", useLog4JPropertiesFile);            serverJarLocation = getString(PERFORMANCE + ".serverJarLocation", serverJarLocation);            deployModeSelectSize = getInt(PERFORMANCE + ".deployModeSelectSize", deployModeSelectSize);//            logConfiguration();        } catch (Exception e) {            BILoggerFactory.getLogger().error(e.getMessage(), e);        }    }    private void logConfiguration() {        logger.info("");        logger.info("");        logger.info("Properties of FineIndex system can be set through editing the plugs.properties file in the resource folder. " +                "The current value is displayed  below ");        logger.info("The value of {}.returnEmptyIndex is {}", PERFORMANCE, returnEmptyIndex);        logger.info("The value of {}.isSearchPinYin is {}", PERFORMANCE, isSearchPinYin);        logger.info("The value of {}.isGetTemplateScreenCapture is {}", PERFORMANCE, isGetTemplateScreenCapture);        logger.info("The value of {}.isControlMaxMemory is {}", PERFORMANCE, isControlMaxMemory);        logger.info("The value of {}.useMultiThreadCal is {}", PERFORMANCE, useMultiThreadCal);        logger.info("The value of {}.maxNodeCount is {}", PERFORMANCE, maxNodeCount);        logger.info("The value of {}.diskSortDumpThreshold is {}", PERFORMANCE, diskSortDumpThreshold);        logger.info("The value of {}.useDiskSort is {}", PERFORMANCE, diskSort);        logger.info("The value of {}.biThreadPoolSize is {}", PERFORMANCE, biThreadPoolSize);        logger.info("The value of {}.useStandardOutError is {}", PERFORMANCE, useStandardOutError);        logger.info("The value of {}.verboseLog is {}", PERFORMANCE, verboseLog);        logger.info("The value of {}.useLog4JPropertiesFile is {}", PERFORMANCE, useLog4JPropertiesFile);        logger.info("The value of {}.serverJarLocation is {}", PERFORMANCE, serverJarLocation);        logger.info("The value of {}.deployModeSelectSize is {}", PERFORMANCE, deployModeSelectSize);        logger.info("");        logger.info("");    }    @Override    public void printSystemParameters() {        logConfiguration();    }    @Override    public String BIServerJarLocation() {        return null;    }    @Override    public boolean useStandardOutError() {        return useStandardOutError;    }    @Override    public int getDeployModeSelectSize() {        return deployModeSelectSize;    }    @Override    public void setDeployModeSelectSize(int size) {        deployModeSelectSize = size;    }    @Override    public void setThreadPoolSize(int size) {        biThreadPoolSize = size;    }    @Override    public int getThreadPoolSize() {        return biThreadPoolSize;    }    @Override    public boolean verboseLog() {        return verboseLog;    }    @Override    public boolean useLog4JPropertiesFile() {        return false;    }    private boolean getBoolean(String name, boolean defaultValue) {        try {            String property = properties.getProperty(name);            if (property != null) {                return Boolean.valueOf(property);            }            return defaultValue;        } catch (Exception e) {            return defaultValue;        }    }    private int getInt(String name, int defaultValue) {        try {            String property = properties.getProperty(name);            if (property != null) {                return Integer.valueOf(property);            }            return defaultValue;        } catch (Exception e) {            return defaultValue;        }    }    private long getLong(String name, long defaultValue) {        try {            String property = properties.getProperty(name);            if (property != null) {                return Long.valueOf(property);            }            return defaultValue;        } catch (Exception e) {            return defaultValue;        }    }    private String getString(String name, String defaultValue) {        try {            String property = properties.getProperty(name);            if (property != null) {                return property;            }            return defaultValue;        } catch (Exception e) {            return defaultValue;        }    }    private void setTimeoutConfig(Properties properties) {        try {            String controlTimeout = properties.getProperty(PERFORMANCE + ".controlTimeout");            isControl = Boolean.parseBoolean(controlTimeout);            timeout = Long.parseLong(properties.getProperty(PERFORMANCE + ".timeout"));            String message = properties.getProperty(PERFORMANCE + ".message");            if (message != null) {                this.message = message;            }        } catch (NumberFormatException e) {            isControl = false;            timeout = Long.MAX_VALUE;        }    }    @Override    public boolean controlTimeout() {        return isControl;    }    @Override    public boolean isTimeout(long time) {        return System.currentTimeMillis() - time > timeout;    }    @Override    public String getTimeoutMessage() {        return message;    }    private void put(String localAddr, long startTime) {        List<Long> startTimeList = calculateMap.get(localAddr);        if (startTimeList == null) {            List<Long> list = new ArrayList<Long>();            list.add(startTime);            calculateMap.put(localAddr, list);        } else {            synchronized (startTimeList) {                startTimeList.add(startTime);            }        }    }    @Override    public void mark(String localAddr, long startTime) {        localAdrrThreadLocal.set(new LocalAdrrBean(localAddr, startTime));        put(localAddr, startTime);    }    @Override    public boolean controlUniqueThread() {        return true;    }    public boolean shouldExit() {        LocalAdrrBean localAdrr = localAdrrThreadLocal.get();        if (localAdrr == null) {            return false;        }        List<Long> startTimeList = calculateMap.get(localAdrr.getLocalAddr());        long startTime = localAdrr.getStartTime();        synchronized (startTimeList) {            for (long time : startTimeList) {                if (time > startTime) {                    return true;                }            }        }        return false;    }    @Override    public void checkExit() {        if (shouldExit()) {            removeStartTime();            exit();        }    }    private void removeStartTime() {        LocalAdrrBean localAdrr = localAdrrThreadLocal.get();        if (localAdrr == null) {            return;        }        List<Long> startTimeList = calculateMap.get(localAdrr.getLocalAddr());        startTimeList.remove(localAdrr.getStartTime());    }    private void exit() {        throw new RuntimeException("Duplicate calculation.");//		throw new RuntimeException("同时只能进行一次分析计算");    }    @Override    public boolean isReturnEmptyIndex() {        return returnEmptyIndex;    }    @Override    public boolean isSearchPinYin() {        return isSearchPinYin;    }    @Override    public boolean controlMaxMemory() {        return isControlMaxMemory;    }    @Override    public int getMaxNodeCount() {        return maxNodeCount;    }    @Override    public boolean isDiskSort() {        return diskSort;    }    @Override    public long getDiskSortDumpThreshold() {        return diskSortDumpThreshold;    }    @Override    public boolean isGetTemplateScreenCapture() {        return isGetTemplateScreenCapture;    }    private boolean forceWrite = false;    private boolean useDereplication = true;    public boolean isForceMapBufferWrite() {        return forceWrite;    }    public boolean useDereplication() {        return useDereplication;    }    public boolean isUseMultiThreadCal() {        return useMultiThreadCal;    }    public int getBiThreadPoolSize() {        return biThreadPoolSize;    }}