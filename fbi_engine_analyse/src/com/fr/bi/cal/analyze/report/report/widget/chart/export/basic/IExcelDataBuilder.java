package com.fr.bi.cal.analyze.report.report.widget.chart.export.basic;

import com.fr.json.JSONException;

/**
 * Created by Kary on 2017/2/26.
 */
public interface IExcelDataBuilder {
    void initAttrs() throws Exception;

    void amendment() throws Exception;

    void createTargetStyles();

    void createHeaders() throws Exception;

    void createItems() throws Exception;

    BIExcelTableData createTableData() throws JSONException;
}
