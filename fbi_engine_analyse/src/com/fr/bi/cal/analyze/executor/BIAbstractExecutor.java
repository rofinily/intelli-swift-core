package com.fr.bi.cal.analyze.executor;

import com.finebi.cube.api.ICubeDataLoader;
import com.fr.bi.cal.analyze.cal.index.loader.MetricGroupInfo;
import com.fr.bi.cal.analyze.executor.paging.Paging;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.conf.report.BIWidget;
import com.fr.bi.conf.session.BISessionProvider;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by GUY on 2015/4/16.
 */
public abstract class BIAbstractExecutor<T> implements BIEngineExecutor<T> {

    protected Paging paging; //分页信息
    protected transient BISession session;
    private BIWidget widget;

    public BIAbstractExecutor(BIWidget widget, Paging paging, BISession session) {
        this.paging = paging;
        this.widget = widget;
        this.session = session;
    }

    public BISessionProvider getSession() {
        return session;
    }

    public ICubeDataLoader getLoader() {
        return session.getLoader();
    }

    /**
     * 获取点击节点代表的gvi
     *
     * @param click
     * @return
     */
    public GroupValueIndex getClickGvi(Map<String, JSONArray> click) {

        return null;
    }
}