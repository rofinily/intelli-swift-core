package com.fr.bi.cal.analyze.report.report.widget.chart.types;

import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

/**
 * Created by eason on 2017/3/22.
 */
public class VanMultiPieWidget extends VanPieWidget{

    private static final int LIGHTER = 1;
    private static final int DARKER = 2;

    protected JSONObject populateDefaultSettings() throws JSONException {
        JSONObject settings = super.populateDefaultSettings();

        settings.put("gradientType", LIGHTER);

        return settings;
    }

    public JSONObject createPlotOptions(JSONObject globalStyle, JSONObject settings) throws Exception{
        JSONObject plotOptions = super.createPlotOptions(globalStyle, settings);

        plotOptions.put("gradual", settings.optInt("gradientType") == LIGHTER ? "lighter" : "darker");

        plotOptions.put("drilldown", true);

        return plotOptions;
    }

    public JSONArray createSeries(JSONObject originData) throws Exception{

        JSONArray series = JSONArray.create();
        String[] targetIDs = this.getUsedTargetID();

        if(!originData.has("c") || targetIDs.length < 1){
            return series;
        }

        JSONArray data = this.createChildren(originData);

        series.put(JSONObject.create().put("data", data).put("name", this.getDimensionNameByID(targetIDs[0])).put("dimensionID", targetIDs[0]));

        return series;
    }

    private JSONArray createChildren(JSONObject originData) throws JSONException {
        JSONArray children = JSONArray.create();

        if(!originData.has("c")){
            return children;
        }

        JSONArray rawChildren = originData.optJSONArray("c");

        for(int i = 0, dataCount = rawChildren.length(); i < dataCount; i++){
            JSONObject item = rawChildren.getJSONObject(i);
            double value = item.getJSONArray("s").getDouble(0);
            String name =  item.optString("n");
            JSONObject datum = JSONObject.create().put("name", name).put("value", value);
            if(item.has("c")){
                datum.put("children", this.createChildren(item));
            }
            children.put(datum);
        }

        return children;
    }

    protected int numberLevel(String dimensionID){
        return BIReportConstant.TARGET_STYLE.NUM_LEVEL.NORMAL;
    }

    protected void formatSeriesTooltipFormat(JSONObject options) throws Exception{

    }

    protected void formatSeriesDataLabelFormat(JSONObject options) throws Exception{

    }

    public String getSeriesType(String dimensionID){
        return "multiPie";
    }

    protected String getTooltipIdentifier(){
        return NAME + SERIES + PERCENT;
    }

}
