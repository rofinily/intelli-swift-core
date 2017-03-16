/**
 * @class BI.DetailDateDimensionCombo
 * @extend BI.Widget
 * 明细表日期维度的combo
 */
BI.DetailDateDimensionComboShow = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.DetailDateDimensionComboShow.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-detail-date-dimension-combo"
        })
    },

    _init: function () {
        BI.DetailDateDimensionComboShow.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.combo = BI.createWidget({
            type: "bi.down_list_combo",
            element: this.element,
            height: 25,
            iconCls: "detail-dimension-set-font"
        });
        this.combo.on(BI.DownListCombo.EVENT_CHANGE, function (v) {
            self.fireEvent(BI.DetailDateDimensionComboShow.EVENT_CHANGE, v);
        });
        this.combo.on(BI.DownListCombo.EVENT_BEFORE_POPUPVIEW, function () {
            this.populate(self._rebuildItems());
        })
    },

    _rebuildItems: function () {
        var self = this, o = this.options;
        var fieldId = BI.Utils.getFieldIDByDimensionID(o.dId);
        var fieldName = BI.Utils.getFieldNameByID(fieldId);
        var tableName = BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(fieldId));
        return [
            [{
                text: BI.i18nText("BI-Basic_Date"),
                value: BICst.DETAIL_DATE_COMBO.YMD,
                cls: "dot-e-font"
            }, {
                text: BI.i18nText("BI-Year_Fen"),
                value: BICst.DETAIL_DATE_COMBO.YEAR,
                cls: "dot-e-font"
            }, {
                text: BI.i18nText("BI-Multi_Date_Quarter"),
                value: BICst.DETAIL_DATE_COMBO.SEASON,
                cls: "dot-e-font"
            }, {
                text: BI.i18nText("BI-Multi_Date_Month"),
                value: BICst.DETAIL_DATE_COMBO.MONTH,
                cls: "dot-e-font"
            }, {
                text: BI.i18nText("BI-Week_XingQi"),
                value: BICst.DETAIL_DATE_COMBO.WEEK,
                cls: "dot-e-font"
            }, {
                text: BI.i18nText("BI-Time_ShiKe"),
                value: BICst.DETAIL_DATE_COMBO.YMD_HMS,
                cls: "dot-e-font"
            }],
            [{
                text: BI.i18nText("BI-Show_Field"),
                value: BICst.DETAIL_STRING_COMBO.SHOW_FIELD,
                cls: BI.Utils.isDimensionUsable(this.options.dId) ? "widget-combo-show-title-font" : ""
            }],
            [{
                text: BI.i18nText("BI-This_Target_From") + ":" + tableName + "." + fieldName,
                title: BI.i18nText("BI-This_Target_From") + ":" + tableName + "." + fieldName,
                tipType: "warning",
                value: BICst.DETAIL_DATE_COMBO.INFO,
                disabled: true
            }]
        ]
    },

    getValue: function () {
        return this.combo.getValue();
    }

});
BI.DetailDateDimensionComboShow.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.detail_date_dimension_combo_show", BI.DetailDateDimensionComboShow);