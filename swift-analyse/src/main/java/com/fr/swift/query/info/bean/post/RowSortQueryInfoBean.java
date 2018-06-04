package com.fr.swift.query.info.bean.post;

import com.fr.swift.query.info.bean.SortBean;
import com.fr.swift.query.post.PostQueryType;
import com.fr.third.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by Lyon on 2018/6/3.
 */
public class RowSortQueryInfoBean extends AbstractPostQueryInfoBean {

    @JsonProperty
    private Map<String, SortBean> sortMap;

    public Map<String, SortBean> getSortMap() {
        return sortMap;
    }

    public void setSortMap(Map<String, SortBean> sortMap) {
        this.sortMap = sortMap;
    }

    @Override
    public PostQueryType getType() {
        return PostQueryType.ROW_SORT;
    }
}
