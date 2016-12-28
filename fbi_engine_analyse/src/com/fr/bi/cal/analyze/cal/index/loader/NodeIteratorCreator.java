package com.fr.bi.cal.analyze.cal.index.loader;

import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.cal.analyze.cal.result.NodeExpander;
import com.fr.bi.cal.analyze.cal.sssecret.IRootDimensionGroup;
import com.fr.bi.cal.analyze.cal.sssecret.NoneMetricRootDimensionGroup;
import com.fr.bi.cal.analyze.cal.sssecret.RootDimensionGroup;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.conf.report.widget.field.dimension.BIDimension;
import com.fr.bi.conf.report.widget.field.dimension.filter.DimensionFilter;
import com.fr.bi.conf.report.widget.field.target.filter.TargetFilter;
import com.fr.bi.field.target.key.cal.BICalculatorTargetKey;
import com.fr.bi.field.target.key.cal.configuration.BIConfiguratedCalculatorTargetKey;
import com.fr.bi.field.target.key.cal.configuration.BIPeriodCalTargetKey;
import com.fr.bi.field.target.target.BISummaryTarget;
import com.fr.bi.stable.gvi.GVIFactory;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.general.ComparatorUtils;
import com.fr.general.NameObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by 小灰灰 on 2016/11/17.
 */
public class NodeIteratorCreator {
    private static GroupValueIndex ALL_SHOW = GVIFactory.createAllShowIndexGVI(1);
    private BISession session;
    private List<MetricGroupInfo> metricGroupInfoList = new ArrayList<MetricGroupInfo>();
    private BIDimension[] rowDimension;
    private BISummaryTarget[] usedTargets;
    private Map<String, DimensionFilter> targetFilterMap;
    private NodeExpander expander;
    private boolean isRealData;
    private NameObject targetSort;
    private TargetFilter filter;
    private final boolean showSum;
    private final boolean calAllPage;

    public NodeIteratorCreator(List<MetricGroupInfo> metricGroupInfoList, BIDimension[] rowDimension, BISummaryTarget[] usedTargets, Map<String, DimensionFilter> targetFilterMap, NodeExpander expander, boolean isRealData, BISession session, NameObject targetSort, TargetFilter filter, boolean showSum, boolean setIndex, boolean calAllPage) {
        this.metricGroupInfoList = metricGroupInfoList;
        this.rowDimension = rowDimension;
        this.usedTargets = usedTargets;
        this.targetFilterMap = targetFilterMap;
        this.expander = expander;
        this.isRealData = isRealData;
        this.session = session;
        this.targetSort = targetSort;
        this.filter = filter;
        this.showSum = showSum;
        this.calAllPage = calAllPage;
    }

    private CalLevel getConfiguredCalculatorTargetLevel() {
        CalLevel level = CalLevel.PART_NODE;
        for (BICalculatorTargetKey key : LoaderUtils.getCalculatorTargets(usedTargets)) {
            if (key instanceof BIPeriodCalTargetKey) {
                return CalLevel.ALL_NODE;
            }
            if (key instanceof BIConfiguratedCalculatorTargetKey) {
                level = CalLevel.SINGLE_NODE;
            }
        }
        return level;
    }

    public CalLevel getCalLevel() {
        if (calAllPage){
            return CalLevel.ALL_NODE;
        }
        CalLevel level = getConfiguredCalculatorTargetLevel();
        if (level == CalLevel.ALL_NODE){
            return level;
        }
        if (hasDimensionInDirectFilter() && showSum){
            return CalLevel.ALL_NODE;
        }
        return hasTargetSort() ? CalLevel.SINGLE_NODE : level;
    }

    public IRootDimensionGroup createRoot() {
        switch (getCalLevel()) {
            case SINGLE_NODE:
                return createNormalIteratorRoot();
            case PART_NODE:
                return createNormalIteratorRoot();
            default:
                return createNormalIteratorRoot();
        }
    }

    private IRootDimensionGroup createNormalIteratorRoot() {
        if (usedTargets == null || usedTargets.length == 0){
            return new NoneMetricRootDimensionGroup(metricGroupInfoList, expander, session, isRealData, filter);
        }
        GroupValueIndex[] directFilterIndexes = createDirectFilterIndex();
        for (int i = 0; i < directFilterIndexes.length; i++) {
            if (directFilterIndexes[i] != null) {
                metricGroupInfoList.get(i).getFilterIndex().AND(directFilterIndexes[i]);
            }
        }
        return new RootDimensionGroup(metricGroupInfoList, expander, session, isRealData);
    }

    private GroupValueIndex[] createDirectFilterIndex() {
        GroupValueIndex[] retIndexes = new GroupValueIndex[metricGroupInfoList.size()];
        Arrays.fill(retIndexes, ALL_SHOW);
        for (int i = 0; i < retIndexes.length; i++) {
            for (int deep = 0; deep < rowDimension.length; deep++) {
                DimensionFilter resultFilter = rowDimension[deep].getFilter();
                if (resultFilter != null && resultFilter.canCreateDirectFilter()) {
                    DimensionCalculator c = metricGroupInfoList.get(i).getRows()[deep];
                    BusinessTable t = metricGroupInfoList.get(i).getTarget();
                    GroupValueIndex filterIndex = resultFilter.createFilterIndex(c, t, session.getLoader(), session.getUserId());
                    retIndexes[i] = retIndexes[i].and(filterIndex);
                }
            }
        }
        return retIndexes;
    }


    private boolean hasDimensionInDirectFilter() {
        for (BIDimension dimension : rowDimension){
            DimensionFilter filter = dimension.getFilter();
            if (filter != null && !filter.canCreateDirectFilter()){
                return true;
            }
        }
        return false;
    }

    private boolean hasTargetSort() {
        if (targetSort == null){
            return false;
        }
        for (BISummaryTarget t : usedTargets){
            if (ComparatorUtils.equals(t.getValue(), targetSort.getName())){
                return true;
            }
        }
        return false;
    }
}
