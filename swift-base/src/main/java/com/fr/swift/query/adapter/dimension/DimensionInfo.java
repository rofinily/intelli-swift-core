package com.fr.swift.query.adapter.dimension;

import com.fr.swift.query.filter.info.FilterInfo;

/**
 * 主要是GroupBy的维度的相关的属性
 *
 * Created by Lyon on 2018/4/23.
 */
public interface DimensionInfo {

    /**
     * 用于groupBy维度
     *
     * @return
     */
    Dimension[] getDimensions();

    /**
     * 和维度相关的过滤器，包含明细过滤和结果过滤，但是调用buildDetailFilter的时候只取明细过滤
     *
     * @return
     */
    FilterInfo getFilterInfo();

    /**
     * 分页用到的游标
     *
     * @return
     */
    Cursor getCursor();

    /**
     * 节点展开用的
     *
     * @return
     */
    Expander getExpander();
}
