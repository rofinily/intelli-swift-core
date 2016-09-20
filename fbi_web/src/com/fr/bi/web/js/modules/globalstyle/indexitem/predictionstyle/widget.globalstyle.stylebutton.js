/**
 * Created by zcf on 2016/9/8.
 */
BI.GlobalStyleStyleButton = BI.inherit(BI.BasicButton, {

    _defaultConfig: function () {
        var conf = BI.GlobalStyleStyleButton.superclass._defaultConfig.apply(this, arguments);
        return BI.extend(conf, {
            baseCls: (conf.baseCls || ""),
            value: {},
            height: 70,
            width: 110
        })
    },

    _init: function () {
        BI.GlobalStyleStyleButton.superclass._init.apply(this, arguments);
        var o = this.options;
        this.value = o.value;
        var canvas = BI.createWidget({
            type: "bi.canvas",
            element: this.element,
            height: 70,
            width: 110
        });
        var mainBackground = (o.value.mainBackground.type === 1) ? o.value.mainBackground.value : "#f3f3f3";
        var widgetBackground = (o.value.widgetBackground.type === 1) ? o.value.widgetBackground.value : "#ffffff";
        var titleBackground = (o.value.titleBackground.type === 1) ? o.value.titleBackground.value : "#ffffff";
        canvas.rect(0, 0, 110, 70, mainBackground);
        canvas.rect(10, 10, 90, 50, widgetBackground);
        canvas.rect(10, 10, 90, 10, titleBackground);
        canvas.rect(20, 27, 40, 6, o.value.chartColor[0]);
        canvas.rect(20, 37, 70, 6, o.value.chartColor[1]);
        canvas.rect(20, 47, 50, 6, o.value.chartColor[2]);
        canvas.stroke();
    },

    getValue: function () {
        return this.value;
    },

    setValue: function (v) {
        this.value = v;
    },

    doClick: function () {
        BI.GlobalStyleStyleButton.superclass.doClick.apply(this, arguments);
        if (this.isValid()) {
            this.fireEvent(BI.GlobalStyleStyleButton.EVENT_CHANGE, this);
        }
    }
});
BI.GlobalStyleStyleButton.EVENT_CHANGE = "BI.GlobalStyleStyleButton.EVENT_CHANGE";
$.shortcut("bi.global_style_style_button", BI.GlobalStyleStyleButton);