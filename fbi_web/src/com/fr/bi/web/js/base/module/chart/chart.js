/**
 * 图表控件
 * @class BI.Chart
 * @extends BI.Widget
 */
BI.Chart = BI.inherit(BI.Pane, {

    _defaultConfig: function () {
        return BI.extend(BI.Chart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-chart"
        })
    },

    _init: function () {
        BI.Chart.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.isInit = false;
        this.isSetOptions = false;
        this.wants2SetData = false;
        var width = 0;
        var height = 0;

        BI.Resizers.add(this.getName(), function () {
            if (self.element.is(":visible")) {
                var newW = self.element.width(), newH = self.element.height();
                if (width > 0 && height > 0 && (width !== newW || height !== newH)) {
                    self.vanCharts.resize();
                    width = newW;
                    height = newH;
                }
            }
        });
    },

    _setData: function () {
        this.vanCharts.setData(this.config);
    },

    resize: function () {
        if (this.element.is(":visible") && this.isSetOptions === true) {
            this.vanCharts && this.vanCharts.resize();
        }
    },

    populate: function (items, options) {
        var self = this, o = this.options;
        o.items = items;
        this.config = options;
        this.config.series = o.items;

        var setOptions = function () {
            self.vanCharts.setOptions(self.config);
            self.isSetOptions = true;
            if (self.wants2SetData === true) {
                self._setData();
            }
        };
        var init = function () {
            if (self.element.is(":visible")) {
                self.vanCharts = VanCharts.init(self.element[0]);
                BI.delay(setOptions, 1);
                self.isInit = true;
            }
        };

        if (this.isInit === false) {
            BI.nextTick(init, 1);
        }

        if (this.element.is(":visible") && this.isSetOptions === true) {
            this._setData();
            this.wants2SetData = null;
        } else {
            this.wants2SetData = true;
        }
    }
});
BI.Chart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.chart', BI.Chart);