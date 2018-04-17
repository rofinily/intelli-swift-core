package com.fr.swift.adaptor.transformer;

import com.finebi.conf.constant.BICommonConstants;
import com.finebi.conf.internalimp.bean.filter.AbstractFilterBean;
import com.finebi.conf.internalimp.bean.filter.FormulaFilterBean;
import com.finebi.conf.internalimp.bean.filter.GeneraAndFilterBean;
import com.finebi.conf.internalimp.bean.filter.GeneraOrFilterBean;
import com.finebi.conf.internalimp.bean.filter.date.DateAfterFilterBean;
import com.finebi.conf.internalimp.bean.filter.date.DateBeforeFilterBean;
import com.finebi.conf.internalimp.bean.filter.date.DateBelongFilterBean;
import com.finebi.conf.internalimp.bean.filter.date.DateBottomNFilterBean;
import com.finebi.conf.internalimp.bean.filter.date.DateEqualFilterBean;
import com.finebi.conf.internalimp.bean.filter.date.DateNoBelongFilterBean;
import com.finebi.conf.internalimp.bean.filter.date.DateNoEqualFilterBean;
import com.finebi.conf.internalimp.bean.filter.date.DateTopNFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberBelongFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberBottomNFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberEqualFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberLargeFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberLargeOrEqualFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberNoBelongFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberNoEqualFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberSmallFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberSmallOrEqualFilterBean;
import com.finebi.conf.internalimp.bean.filter.number.NumberTopNFilterBean;
import com.finebi.conf.internalimp.bean.filter.string.StringBeginWithFilterBean;
import com.finebi.conf.internalimp.bean.filter.string.StringBelongFilterBean;
import com.finebi.conf.internalimp.bean.filter.string.StringContainFilterBean;
import com.finebi.conf.internalimp.bean.filter.string.StringEndWithFilterBean;
import com.finebi.conf.internalimp.bean.filter.string.StringNoBeginWithFilterBean;
import com.finebi.conf.internalimp.bean.filter.string.StringNoBelongFilterBean;
import com.finebi.conf.internalimp.bean.filter.string.StringNoContainFilterBean;
import com.finebi.conf.internalimp.bean.filter.string.StringNoEndWithFilterBean;
import com.finebi.conf.internalimp.bean.filtervalue.date.DateRangeValueBean;
import com.finebi.conf.internalimp.bean.filtervalue.number.NumberSelectedFilterValueBean;
import com.finebi.conf.internalimp.bean.filtervalue.number.NumberValue;
import com.finebi.conf.structure.bean.filter.DateFilterBean;
import com.finebi.conf.structure.bean.filter.FilterBean;
import com.finebi.conf.structure.dashboard.widget.target.FineTarget;
import com.finebi.conf.structure.filter.FineFilter;
import com.fr.general.ComparatorUtils;
import com.fr.stable.StringUtils;
import com.fr.swift.adaptor.encrypt.SwiftEncryption;
import com.fr.swift.adaptor.transformer.cal.AvgUtils;
import com.fr.swift.adaptor.transformer.date.DateUtils;
import com.fr.swift.query.filter.SwiftDetailFilterType;
import com.fr.swift.query.filter.info.FilterInfo;
import com.fr.swift.query.filter.info.GeneralFilterInfo;
import com.fr.swift.query.filter.info.MatchFilterInfo;
import com.fr.swift.query.filter.info.SwiftDetailFilterInfo;
import com.fr.swift.query.filter.info.value.SwiftDateInRangeFilterValue;
import com.fr.swift.query.filter.info.value.SwiftNumberInRangeFilterValue;
import com.fr.swift.segment.Segment;
import com.fr.swift.util.Crasher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pony on 2017/12/21.
 */
public class FilterInfoFactory {

    public static FilterInfo transformFineFilter(List<FineFilter> filters) {
        List<FilterBean> beans = new ArrayList<FilterBean>();
        for (FineFilter filter : filters) {
            if (filter.getValue() != null) {
                beans.add((FilterBean) filter.getValue());
            }
        }
        return transformFilterBean(beans, new ArrayList<Segment>());
    }

    public static FilterInfo transformDimensionFineFilter(List<FineFilter> filters, String dimId, List<FineTarget> targets) {
        if (filters == null) {
            return null;
        }
        List<FilterBean> beans = new ArrayList<FilterBean>();
        for (FineFilter filter : filters) {
            if (filter.getValue() != null) {
                beans.add((FilterBean) filter.getValue());
            }
        }
        List<FilterInfo> filterInfoList = new ArrayList<FilterInfo>();
        for (FilterBean bean : beans) {
            AbstractFilterBean filterBean = (AbstractFilterBean) bean;
            FilterInfo info = createFilterInfo(filterBean, new ArrayList<Segment>());
            if (!ComparatorUtils.equals(filterBean.getTargetId(), dimId)) {
                filterInfoList.add(new MatchFilterInfo(info, getIndex(filterBean.getTargetId(), targets)));
            } else {
                filterInfoList.add(info);
            }
        }
        return new GeneralFilterInfo(filterInfoList, GeneralFilterInfo.AND);
    }

    private static int getIndex(String targetId, List<FineTarget> targets) {
        for (int i = 0; i < targets.size(); i++) {
            if (ComparatorUtils.equals(targetId, targets.get(i).getId())) {
                return i;
            }
        }
        return Crasher.crash("invalid target filter id :" + targetId);
    }

    public static FilterInfo transformFilterBean(List<FilterBean> beans, List<Segment> segments) {
        List<FilterInfo> filterInfoList = new ArrayList<FilterInfo>();
        for (FilterBean bean : beans) {
            filterInfoList.add(createFilterInfo(bean, segments));
        }
        return new GeneralFilterInfo(filterInfoList, GeneralFilterInfo.AND);
    }

    public static SwiftDetailFilterInfo createFilterInfo(FilterBean bean, List<Segment> segments) {
        String fieldId = ((AbstractFilterBean) bean).getFieldId();
        // 分析表这边的bean暂时没有FieldId，所以还是取FieldName
        String fieldName = StringUtils.isEmpty(fieldId) ?
                ((AbstractFilterBean) bean).getFieldName() : SwiftEncryption.decryptFieldId(fieldId)[1];
        int type = bean.getFilterType();
        switch (type) {
            // string类过滤
            case BICommonConstants.ANALYSIS_FILTER_STRING.BELONG_VALUE:
                List<String> belongValues = ((StringBelongFilterBean) bean).getFilterValue().getValue();
                if (belongValues == null || belongValues.size() == 0) {
                    break;
                }
                return new SwiftDetailFilterInfo<Set<String>>(fieldName,
                        new HashSet<String>(belongValues), SwiftDetailFilterType.STRING_IN);
            case BICommonConstants.ANALYSIS_FILTER_STRING.NOT_BELONG_VALUE:
                List<String> notBelongValues = ((StringNoBelongFilterBean) bean).getFilterValue().getValue();
                if (notBelongValues == null || notBelongValues.size() == 0) {
                    break;
                }
                return new SwiftDetailFilterInfo<Set<String>>(fieldName,
                        new HashSet<String>(notBelongValues), SwiftDetailFilterType.STRING_NOT_IN);
            case BICommonConstants.ANALYSIS_FILTER_STRING.CONTAIN:
                String contain = ((StringContainFilterBean) bean).getFilterValue();
                if (StringUtils.isBlank(contain)) {
                    break;
                }
                return new SwiftDetailFilterInfo<String>(fieldName, contain, SwiftDetailFilterType.STRING_LIKE);
            case BICommonConstants.ANALYSIS_FILTER_STRING.NOT_CONTAIN: {
                String value = ((StringNoContainFilterBean) bean).getFilterValue();
                if (StringUtils.isBlank(value)) {
                    break;
                }
                return new SwiftDetailFilterInfo<String>(fieldName, value, SwiftDetailFilterType.STRING_NOT_LIKE);
            }
            case BICommonConstants.ANALYSIS_FILTER_STRING.BEGIN_WITH: {
                String value = ((StringBeginWithFilterBean) bean).getFilterValue();
                if (StringUtils.isBlank(value)) {
                    break;
                }
                return new SwiftDetailFilterInfo<String>(fieldName, value, SwiftDetailFilterType.STRING_STARTS_WITH);
            }
            case BICommonConstants.ANALYSIS_FILTER_STRING.NOT_BEGIN_WITH: {
                String value = ((StringNoBeginWithFilterBean) bean).getFilterValue();
                if (StringUtils.isBlank(value)) {
                    break;
                }
                return new SwiftDetailFilterInfo<String>(fieldName, value, SwiftDetailFilterType.STRING_NOT_STARTS_WITH);
            }
            case BICommonConstants.ANALYSIS_FILTER_STRING.END_WITH: {
                String value = ((StringEndWithFilterBean) bean).getFilterValue();
                if (StringUtils.isBlank(value)) {
                    break;
                }
                return new SwiftDetailFilterInfo<String>(fieldName, value, SwiftDetailFilterType.STRING_ENDS_WITH);
            }
            case BICommonConstants.ANALYSIS_FILTER_STRING.NOT_END_WITH: {
                String value = ((StringNoEndWithFilterBean) bean).getFilterValue();
                if (StringUtils.isBlank(value)) {
                    break;
                }
                return new SwiftDetailFilterInfo<String>(fieldName, value, SwiftDetailFilterType.STRING_NOT_ENDS_WITH);
            }
            case BICommonConstants.ANALYSIS_FILTER_STRING.IS_NULL:
                return new SwiftDetailFilterInfo<Object>(fieldName, null, SwiftDetailFilterType.NULL);
            case BICommonConstants.ANALYSIS_FILTER_STRING.NOT_NULL:
                return new SwiftDetailFilterInfo<Object>(fieldName, null, SwiftDetailFilterType.NOT_NULL);

            // 数值类过滤
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.BELONG_VALUE: {
                NumberValue nv = ((NumberBelongFilterBean) bean).getFilterValue();
                return new SwiftDetailFilterInfo<SwiftNumberInRangeFilterValue>(fieldName, createValue(nv),
                        SwiftDetailFilterType.NUMBER_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.NOT_BELONG_VALUE: {
                NumberValue nv = ((NumberNoBelongFilterBean) bean).getFilterValue();
                return new SwiftDetailFilterInfo<SwiftNumberInRangeFilterValue>(fieldName, createValue(nv),
                        SwiftDetailFilterType.NUMBER_NOT_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.EQUAL_TO: {
                final Double value = ((NumberEqualFilterBean) bean).getFilterValue();
                return new SwiftDetailFilterInfo<Set<Double>>(fieldName, new HashSet<Double>() {{
                    add(value);
                }},
                        SwiftDetailFilterType.NUMBER_CONTAIN);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.NOT_EQUAL_TO: {
                final Double value = ((NumberNoEqualFilterBean) bean).getFilterValue();
                return new SwiftDetailFilterInfo<Set<Double>>(fieldName, new HashSet<Double>() {{
                    add(value);
                }},
                        SwiftDetailFilterType.NUMBER_NOT_CONTAIN);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.LARGE: {
                NumberSelectedFilterValueBean numberBean = ((NumberLargeFilterBean) bean).getFilterValue();
                SwiftNumberInRangeFilterValue filterValue = new SwiftNumberInRangeFilterValue();
                filterValue.setMin(createValue(numberBean, segments, fieldName));
                return new SwiftDetailFilterInfo<SwiftNumberInRangeFilterValue>(fieldName, filterValue,
                        SwiftDetailFilterType.NUMBER_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.SMALL: {
                NumberSelectedFilterValueBean numberBean = ((NumberSmallFilterBean) bean).getFilterValue();
                SwiftNumberInRangeFilterValue value = new SwiftNumberInRangeFilterValue();
                value.setMax(createValue(numberBean, segments, fieldName));
                return new SwiftDetailFilterInfo<SwiftNumberInRangeFilterValue>(fieldName, value,
                        SwiftDetailFilterType.NUMBER_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.LARGE_OR_EQUAL: {
                NumberSelectedFilterValueBean numberBean = ((NumberLargeOrEqualFilterBean) bean).getFilterValue();
                SwiftNumberInRangeFilterValue value = new SwiftNumberInRangeFilterValue();
                value.setMin(createValue(numberBean, segments, fieldName));
                value.setMinIncluded(true);
                return new SwiftDetailFilterInfo<SwiftNumberInRangeFilterValue>(fieldName, value,
                        SwiftDetailFilterType.NUMBER_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.SMALL_OR_EQUAL: {
                NumberSelectedFilterValueBean numberBean = ((NumberSmallOrEqualFilterBean) bean).getFilterValue();
                SwiftNumberInRangeFilterValue value = new SwiftNumberInRangeFilterValue();
                value.setMax(createValue(numberBean, segments, fieldName));
                value.setMaxIncluded(true);
                return new SwiftDetailFilterInfo<SwiftNumberInRangeFilterValue>(fieldName, value,
                        SwiftDetailFilterType.NUMBER_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.TOP_N: {
                int n = ((NumberTopNFilterBean) bean).getFilterValue().intValue();
                return new SwiftDetailFilterInfo<Integer>(fieldName, n, SwiftDetailFilterType.TOP_N);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.BOTTOM_N: {
                int n = ((NumberBottomNFilterBean) bean).getFilterValue().intValue();
                return new SwiftDetailFilterInfo<Integer>(fieldName, n, SwiftDetailFilterType.BOTTOM_N);
            }
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.IS_NULL:
                return new SwiftDetailFilterInfo<Object>(fieldName, null, SwiftDetailFilterType.NULL);
            case BICommonConstants.ANALYSIS_FILTER_NUMBER.NOT_NULL:
                return new SwiftDetailFilterInfo<Object>(fieldName, null, SwiftDetailFilterType.NOT_NULL);

            // 日期类过滤
            case BICommonConstants.ANALYSIS_FILTER_DATE.BELONG_VALUE: {
                DateRangeValueBean dateValueBean = ((DateBelongFilterBean) bean).getFilterValue();
                return new SwiftDetailFilterInfo<SwiftDateInRangeFilterValue>(fieldName,
                        createValue(dateValueBean), SwiftDetailFilterType.DATE_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.NOT_BELONG_VALUE: {
                DateRangeValueBean dateValueBean = ((DateNoBelongFilterBean) bean).getFilterValue();
                return new SwiftDetailFilterInfo<SwiftDateInRangeFilterValue>(fieldName,
                        createValue(dateValueBean), SwiftDetailFilterType.DATE_NOT_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.LESS_THAN: {
                DateFilterBean dateFilterBean = ((DateBeforeFilterBean) bean).getFilterValue();
                // 在某一时刻之前。-1ms，因为DateInRangeFilter的范围是左右包含的(startTimeIncluded, endTimeIncluded)
                long value = DateUtils.dateFilterBean2Long(dateFilterBean, false) - 1;
                SwiftDateInRangeFilterValue filterValue = new SwiftDateInRangeFilterValue();
                filterValue.setEnd(value);
                return new SwiftDetailFilterInfo<SwiftDateInRangeFilterValue>(fieldName,
                        filterValue, SwiftDetailFilterType.DATE_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.MORE_THAN: {
                DateFilterBean dateFilterBean = ((DateAfterFilterBean) bean).getFilterValue();
                // 在某一时刻之后。+1ms，因为DateInRangeFilter的范围是左右包含的(startTimeIncluded, endTimeIncluded)
                long value = DateUtils.dateFilterBean2Long(dateFilterBean, true) + 1;
                SwiftDateInRangeFilterValue filterValue = new SwiftDateInRangeFilterValue();
                filterValue.setStart(value);
                return new SwiftDetailFilterInfo<SwiftDateInRangeFilterValue>(fieldName,
                        filterValue, SwiftDetailFilterType.DATE_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.EQUAL_TO: {
                // 最小单位是天
                DateFilterBean dateFilterBean = ((DateEqualFilterBean) bean).getFilterValue();
                long value = DateUtils.dateFilterBean2Long(dateFilterBean);
                SwiftDateInRangeFilterValue filterValue = new SwiftDateInRangeFilterValue();
                filterValue.setStart(DateUtils.startOfDay(value));
                filterValue.setEnd(DateUtils.endOfDay(value));
                return new SwiftDetailFilterInfo<SwiftDateInRangeFilterValue>(fieldName,
                        filterValue, SwiftDetailFilterType.DATE_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.NOT_EQUAL_TO: {
                // 最小单位是天
                DateFilterBean dateFilterBean = ((DateNoEqualFilterBean) bean).getFilterValue();
                long value = DateUtils.dateFilterBean2Long(dateFilterBean);
                SwiftDateInRangeFilterValue filterValue = new SwiftDateInRangeFilterValue();
                filterValue.setStart(DateUtils.startOfDay(value));
                filterValue.setEnd(DateUtils.endOfDay(value));
                return new SwiftDetailFilterInfo<SwiftDateInRangeFilterValue>(fieldName,
                        filterValue, SwiftDetailFilterType.DATE_NOT_IN_RANGE);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.TOP_N: {
                int n = ((DateTopNFilterBean) bean).getFilterValue().intValue();
                // 最早的，对应BOTTOM_N
                return new SwiftDetailFilterInfo<Integer>(fieldName, n, SwiftDetailFilterType.BOTTOM_N);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.BOTTOM_N: {
                int n = ((DateBottomNFilterBean) bean).getFilterValue().intValue();
                // 最晚的，对应TOP_N
                return new SwiftDetailFilterInfo<Integer>(fieldName, n, SwiftDetailFilterType.TOP_N);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.IS_NULL: {
                return new SwiftDetailFilterInfo(fieldName, null, SwiftDetailFilterType.NULL);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.NOT_NULL: {
                return new SwiftDetailFilterInfo(fieldName, null, SwiftDetailFilterType.NOT_NULL);
            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.BELONG_DATE_WIDGET_VALUE: {

            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.NOT_BELONG_DATE_WIDGET_VALUE: {

            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.EQUAL_TO_DATE_WIDGET_VALUE: {

            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.NOT_EQUAL_TO_DATE_WIDGET_VALUE: {

            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.LESS_THAN_DATE_WIDGET_VALUE: {

            }
            case BICommonConstants.ANALYSIS_FILTER_DATE.MORE_THAN_DATE_WIDGET_VALUE: {

            }

            case BICommonConstants.ANALYSIS_FILTER_TYPE.FORMULA: {
                String expr = ((FormulaFilterBean) bean).getFilterValue();
                return new SwiftDetailFilterInfo<String>(fieldName, expr, SwiftDetailFilterType.FORMULA);
            }
            case BICommonConstants.ANALYSIS_FILTER_TYPE.AND: {
                List<FilterBean> beans = ((GeneraAndFilterBean) bean).getFilterValue();
                List<SwiftDetailFilterInfo> filterValues = createFilterValueList(beans, segments);
                return new SwiftDetailFilterInfo<List<SwiftDetailFilterInfo>>(fieldName,
                        filterValues, SwiftDetailFilterType.AND);
            }
            case BICommonConstants.ANALYSIS_FILTER_TYPE.OR: {
                List<FilterBean> beans = ((GeneraOrFilterBean) bean).getFilterValue();
                List<SwiftDetailFilterInfo> filterValues = createFilterValueList(beans, segments);
                return new SwiftDetailFilterInfo<List<SwiftDetailFilterInfo>>(fieldName,
                        filterValues, SwiftDetailFilterType.OR);
            }
            case BICommonConstants.ANALYSIS_FILTER_TYPE.EMPTY_FORMULA:
            case BICommonConstants.ANALYSIS_FILTER_TYPE.EMPTY_CONDITION:
            default:
        }
        return new SwiftDetailFilterInfo(fieldName, null, SwiftDetailFilterType.ALL_SHOW);
    }

    private static List<SwiftDetailFilterInfo> createFilterValueList(List<FilterBean> beans, List<Segment> segments) {
        List<SwiftDetailFilterInfo> filterInfoList = new ArrayList<SwiftDetailFilterInfo>();
        for (FilterBean bean : beans) {
            filterInfoList.add(createFilterInfo(bean, segments));
        }
        return filterInfoList;
    }

    private static double createValue(NumberSelectedFilterValueBean bean, List<Segment> segments, String fieldName) {
        int valueType = bean.getType();
        Double min;
        if (valueType == BICommonConstants.ANALYSIS_FILTER_NUMBER_VALUE.AVG) {
            min = AvgUtils.average(segments, fieldName);
        } else {
            min = bean.getValue();
        }
        return min;
    }


    private static SwiftDateInRangeFilterValue createValue(DateRangeValueBean bean) {
        SwiftDateInRangeFilterValue value = new SwiftDateInRangeFilterValue();
        if (bean.getStart() != null) {
            value.setStart(DateUtils.dateFilterBean2Long(bean.getStart(), false));
        }
        if (bean.getEnd() != null) {
            value.setEnd(DateUtils.dateFilterBean2Long(bean.getEnd(), true));
        }
        return value;
    }

    private static SwiftNumberInRangeFilterValue createValue(NumberValue nv) {
        SwiftNumberInRangeFilterValue value = new SwiftNumberInRangeFilterValue();
        value.setMin(nv.getMin());
        value.setMax(nv.getMax());
        value.setMinIncluded(nv.isClosemin());
        value.setMaxIncluded(nv.isClosemax());
        return value;
    }

}