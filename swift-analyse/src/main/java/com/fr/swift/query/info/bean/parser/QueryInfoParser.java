package com.fr.swift.query.info.bean.parser;

import com.fr.general.ComparatorUtils;
import com.fr.swift.config.service.SwiftMetaDataService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.query.filter.info.FilterInfo;
import com.fr.swift.query.info.bean.element.SortBean;
import com.fr.swift.query.info.bean.query.DetailQueryInfoBean;
import com.fr.swift.query.info.bean.query.GroupQueryInfoBean;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.query.info.bean.query.ResultJoinQueryInfoBean;
import com.fr.swift.query.info.detail.DetailQueryInfo;
import com.fr.swift.query.info.element.dimension.Dimension;
import com.fr.swift.query.info.element.metric.Metric;
import com.fr.swift.query.info.group.GroupQueryInfo;
import com.fr.swift.query.info.group.GroupQueryInfoImpl;
import com.fr.swift.query.info.group.ResultJoinQueryInfoImpl;
import com.fr.swift.query.info.group.post.PostQueryInfo;
import com.fr.swift.query.query.QueryInfo;
import com.fr.swift.query.query.QueryType;
import com.fr.swift.query.sort.AscSort;
import com.fr.swift.query.sort.DescSort;
import com.fr.swift.query.sort.NoneSort;
import com.fr.swift.query.sort.Sort;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lyon on 2018/6/7.
 */
public class QueryInfoParser {

    public static QueryInfo parse(QueryInfoBean queryInfoBean) {
        QueryType type = queryInfoBean.getQueryType();
        switch (type) {
            case GROUP:
            case LOCAL_GROUP_PART:
            case LOCAL_GROUP_ALL:
                return parseGroupQueryInfo((GroupQueryInfoBean) queryInfoBean);
            case RESULT_JOIN:
                return parseResultJoinQueryInfo((ResultJoinQueryInfoBean) queryInfoBean);
            case LOCAL_DETAIL:
            default:
                return parseDetailQueryInfo((DetailQueryInfoBean) queryInfoBean);
        }
    }

    private static QueryInfo parseGroupQueryInfo(GroupQueryInfoBean bean) {
        String queryId = bean.getQueryId();
        // TODO: 2018/6/7 table2sourceKey
        SourceKey table = new SourceKey(bean.getTableName());
        FilterInfo filterInfo = FilterInfoParser.parse(table, bean.getFilterInfoBean());
        List<Dimension> dimensions = DimensionParser.parse(table, bean.getDimensionBeans(), bean.getSortBeans());
        List<Metric> metrics = MetricParser.parse(table, bean.getMetricBeans());
        List<PostQueryInfo> postQueryInfoList = PostQueryInfoParser.parse(bean.getPostQueryInfoBeans(), dimensions, bean.getMetricBeans());
        GroupQueryInfo groupQueryInfo = new GroupQueryInfoImpl(queryId, bean.getFetchSize(), table, filterInfo, dimensions, metrics, postQueryInfoList);
        groupQueryInfo.setQuerySegment(bean.getQuerySegments());
        return groupQueryInfo;
    }

    private static QueryInfo parseResultJoinQueryInfo(ResultJoinQueryInfoBean bean) {
        String queryId = bean.getQueryId();
        List<QueryInfoBean> queryInfoBeans = bean.getQueryInfoBeans();
        List<QueryInfo> queryInfoList = new ArrayList<QueryInfo>();
        for (QueryInfoBean queryInfoBean : queryInfoBeans) {
            queryInfoList.add(parse(queryInfoBean));
        }
        List<Dimension> dimensions = DimensionParser.parse(bean.getJoinedFields());
        List<PostQueryInfo> postQueryInfoList = PostQueryInfoParser.parsePostQueryInfoOfResultJoinQuery(
                bean.getPostQueryInfoBeans(), dimensions, queryInfoBeans);
        return new ResultJoinQueryInfoImpl(queryId, bean.getFetchSize(), queryInfoList, dimensions, postQueryInfoList);
    }

    private static QueryInfo parseDetailQueryInfo(DetailQueryInfoBean bean) {
        String queryId = bean.getQueryId();
        SourceKey table = new SourceKey(bean.getTableName());
        FilterInfo filterInfo = FilterInfoParser.parse(table, bean.getFilterInfoBean());
        List<Dimension> dimensions = DimensionParser.parse(table, bean.getDimensionBeans(), bean.getSortBeans());
        SwiftMetaData metaData = SwiftContext.get().getBean(SwiftMetaDataService.class).getMetaDataByKey(bean.getTableName());
        List<SwiftMetaDataColumn> columns = new ArrayList<SwiftMetaDataColumn>();
        List<Sort> sorts = null;
        List<SortBean> sortBeans = bean.getSortBeans();
        if (null != sortBeans) {
            sorts = new ArrayList<Sort>();
            for (SortBean sortBean : sortBeans) {
                ColumnKey columnKey = new ColumnKey(sortBean.getColumn());
                columnKey.setRelation(RelationSourceParser.parse(sortBean.getRelation()));
                switch (sortBean.getType()) {
                    case NONE:
                        sorts.add(new NoneSort());
                        break;
                    case DESC:
                        sorts.add(new DescSort(getDimensionIndex(sortBean.getColumn(), dimensions), columnKey));
                        break;
                    case ASC:
                        sorts.add(new AscSort(getDimensionIndex(sortBean.getColumn(), dimensions), columnKey));
                        break;
                }
            }
        }
//        List<String> fieldNames = bean.getColumns();
//        try {
//            for (String fieldName : fieldNames) {
//                columns.add(metaData.getColumn(fieldName));
//            }
//            return new DetailQueryInfo(queryId, table, filterInfo, dimensions, sorts, null, new SwiftMetaDataBean(metaData.getTableName(), metaData.getRemark(), metaData.getSchemaName(), columns));
//        } catch (SwiftMetaDataException e) {
//            SwiftLoggers.getLogger(QueryInfoParser.class).error(e);
//        }
        DetailQueryInfo detailQueryInfo = new DetailQueryInfo(queryId, bean.getFetchSize(), table, filterInfo, dimensions, sorts, null, metaData);
        detailQueryInfo.setQuerySegment(bean.getQuerySegments());
        return detailQueryInfo;
    }

    private static int getDimensionIndex(String columnName, List<Dimension> dimensions) {
        for (int i = 0; i < dimensions.size(); i++) {
            if (ComparatorUtils.equals(columnName, dimensions.get(i).getColumnKey().getName())) {
                return i;
            }
        }
        return -1;
    }
}
