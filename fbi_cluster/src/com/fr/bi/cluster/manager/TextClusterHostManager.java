package com.fr.bi.cluster.manager;

import com.fr.bi.cluster.ClusterHostManagerInterface;
import com.fr.bi.cluster.utils.ClusterEnv;
import com.fr.bi.cluster.utils.PropertiesUtils;
import com.fr.bi.stable.utils.program.BIStringUtils;
import com.fr.file.ClusterService;

import java.io.File;
import java.util.Properties;

/**
 * 从配置文件中读取主机信息
 * Created by Hiram on 2015/2/27.
 */
public class TextClusterHostManager implements ClusterHostManagerInterface {

    private static TextClusterHostManager ourInstance = new TextClusterHostManager();
    private boolean isSelf;
    private ClusterService hostClusterService;
    private boolean isInit;
    private boolean isAutoVote;
    private String localRpcPort;
    private Boolean isBuildCube;
    private String buildCubePort;
    private String buildCubeIp;
    private TextClusterHostManager() {
        ensureInit();
    }

    public static TextClusterHostManager getInstance() {
        return ourInstance;
    }

    @Override
    public String getIp() {
        return getHostClusterService().getIp();
    }

    @Override
    public int getPort() {
        return Integer.parseInt(getHostClusterService().getPort());
    }

    public void setPort(int port) {
        getHostClusterService().setPort(String.valueOf(port));
    }

    @Override
    public String getWebAppName() {
        return null;
    }

    @Override
    public String getServiceName() {
        return null;
    }

    @Override
    public boolean isSelf() {
        ensureInit();
        return isSelf;
    }

    @Override
    public ClusterService getHostClusterService() {
        ensureInit();
        return hostClusterService;
    }

    @Override
    public String getLocalRpcPort() {
        return localRpcPort;
    }

    @Override
    public String getBuildCubeIp() {
        return buildCubeIp;
    }

    @Override
    public int getBuildCubePort() {
        return Integer.parseInt(buildCubePort);
    }

    @Override
    public boolean isBuildCube() {
        return isBuildCube;
    }

    public boolean isAutoVote() {
        return isAutoVote;
    }

    private void ensureInit() {
        if (!isInit) {
            isInit = true;
            init();
        }
    }

    private void init() {
        File infoFile = ClusterEnv.getRedirectInfoFile();
        Properties properties = PropertiesUtils.load(infoFile);
        if (properties == null) {
            return;
        }
        hostClusterService = new ClusterService();
        hostClusterService.setIp(properties.getProperty("ip"));
        hostClusterService.setPort(properties.getProperty("port"));
        hostClusterService.setWebAppName(properties.getProperty("webAppName"));
        hostClusterService.setServiceName(properties.getProperty("serviceName"));
        this.isSelf = Boolean.valueOf(properties.getProperty("isSelf"));
        this.isBuildCube = Boolean.valueOf(properties.getProperty("isBuildCube"));
        this.buildCubeIp = properties.getProperty("buildCubeIp");
        this.buildCubePort = properties.getProperty("buildCubePort");

        if(BIStringUtils.isEmptyString(this.buildCubeIp)|| BIStringUtils.isEmptyString(this.buildCubeIp)){
            this.isBuildCube = this.isSelf;
            this.buildCubeIp = hostClusterService.getIp();
            this.buildCubePort = hostClusterService.getPort();
        }

        if (properties.getProperty("isAutoVote") == null) {
            this.isAutoVote = false;
        } else {
            this.isAutoVote = Boolean.valueOf(properties.getProperty("isAutoVote"));
        }
        if (properties.getProperty("localRpcPort") == null) {
            this.localRpcPort = hostClusterService.getPort();
        } else {
            this.localRpcPort = properties.getProperty("localRpcPort");
        }

    }


}