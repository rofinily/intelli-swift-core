package com.fr.swift.query.post;

import com.fr.swift.query.filter.match.MatchFilter;
import com.fr.swift.query.filter.match.NodeFilter;
import com.fr.swift.result.GroupNode;
import com.fr.swift.result.NodeMergeResultSet;
import com.fr.swift.result.NodeMergeResultSetImpl;
import com.fr.swift.result.NodeResultSet;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Lyon on 2018/5/31.
 */
public class HavingFilterQuery extends AbstractPostQuery<NodeResultSet> {

    private PostQuery<NodeResultSet> query;
    private List<MatchFilter> matchFilterList;

    public HavingFilterQuery(PostQuery<NodeResultSet> query, List<MatchFilter> matchFilterList) {
        this.query = query;
        this.matchFilterList = matchFilterList;
    }

    @Override
    public NodeResultSet getQueryResult() throws SQLException {
        NodeMergeResultSet<GroupNode> mergeResult = (NodeMergeResultSet<GroupNode>) query.getQueryResult();
        NodeFilter.filter(mergeResult.getNode(), matchFilterList);
        // 过滤了要重新new一个resetSet，因为在构造函数里面初始化了迭代器
        return new NodeMergeResultSetImpl((GroupNode) mergeResult.getNode(),
                mergeResult.getRowGlobalDictionaries(), mergeResult.getAggregators());
    }
}
