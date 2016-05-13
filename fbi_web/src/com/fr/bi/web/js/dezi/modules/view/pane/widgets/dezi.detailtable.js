/**
 * @class BIDezi.DetailTableView
 * @extends BI.View
 * @type {*|void|Object}
 */
BIDezi.DetailTableView = BI.inherit(BI.View, {

    _constants: {
        SHOW_CHART: 1,
        SHOW_FILTER: 2
    },

    _defaultConfig: function () {
        return BI.extend(BIDezi.DetailTableView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-dashboard-widget"
        })
    },

    _init: function () {
        BIDezi.DetailTableView.superclass._init.apply(this, arguments);
        var self = this, wId = this.model.get("id");
        BI.Broadcasts.on(BICst.BROADCAST.REFRESH_PREFIX + wId, function () {
            self._refreshTableAndFilter();
        });
        BI.Broadcasts.on(BICst.BROADCAST.LINKAGE_PREFIX + wId, function (dId, v) {
            var clicked = self.model.get("clicked") || {};
            var allFromIds = BI.Utils.getAllLinkageFromIdsByID(BI.Utils.getWidgetIDByDimensionID(dId));
            //这条链上所有的其他clicked都应当被清掉
            BI.each(clicked, function (cid, click) {
                if (allFromIds.contains(cid)) {
                    delete clicked[cid];
                }
            });
            if (BI.isNull(v)) {
                delete clicked[dId];
            } else {
                clicked[dId] = v;
            }
            self.model.set("clicked", clicked);
        })
    },


    _render: function (vessel) {
        var self = this;
        this.title = this._buildWidgetTitle();

        this.table = BI.createWidget({
            type: "bi.detail_table",
            wId: this.model.get("id")
        });
        this.table.on(BI.DetailTable.EVENT_CHANGE, function (ob) {
            self.model.set(ob);
        });
        this.widget = BI.createWidget({
            type: "bi.vtape",
            element: vessel,
            items: [{
                el: this.title,
                height: 32
            }, this.table]
        })
    },

    _buildWidgetTitle: function () {
        var self = this, o = this.options;
        if (!this.title) {
            var text = this.title = BI.createWidget({
                type: "bi.label",
                cls: "dashboard-title-text",
                text: BI.Utils.getWidgetNameByID(this.model.get("id")),
                textAlign: "left",
                height: 32
            });

            var expand = BI.createWidget({
                type: "bi.icon_button",
                width: 32,
                height: 32,
                cls: "widget-combo-detail-font dashboard-title-detail"
            });
            expand.on(BI.IconButton.EVENT_CHANGE, function () {
                self._expandWidget();
            });

            var combo = BI.createWidget({
                type: "bi.widget_combo",
                wId: this.model.get("id")
            });
            combo.on(BI.WidgetCombo.EVENT_CHANGE, function (type) {
                switch (type) {
                    case BICst.DASHBOARD_WIDGET_SHOW_NAME:
                        var settings = self.model.get("settings");
                        settings.show_name = !settings.show_name;
                        self.model.set("settings", settings);
                        break;
                    case BICst.DASHBOARD_WIDGET_NAME_POS_LEFT:
                        var settings = self.model.get("settings");
                        settings.name_pos = BICst.DASHBOARD_WIDGET_NAME_POS_LEFT;
                        self.model.set("settings", settings);
                        break;
                    case BICst.DASHBOARD_WIDGET_NAME_POS_CENTER:
                        var settings = self.model.get("settings");
                        settings.name_pos = BICst.DASHBOARD_WIDGET_NAME_POS_CENTER;
                        self.model.set("settings", settings);
                        break;
                    case BICst.DASHBOARD_WIDGET_FILTER:
                        if (BI.isNull(self.filterPane)) {
                            self.filterPane = BI.createWidget({
                                type: "bi.widget_filter",
                                wId: self.model.get("id")
                            });
                            self.filterPane.on(BI.WidgetFilter.EVENT_REMOVE_FILTER, function (widget) {
                                self.model.set(widget);
                            });
                            BI.createWidget({
                                type: "bi.absolute",
                                element: self.element,
                                items: [{
                                    el: self.filterPane,
                                    top: 32,
                                    left: 0,
                                    right: 0,
                                    bottom: 0
                                }]
                            });
                            return;
                        }
                        self.filterPane.setVisible(!self.filterPane.isVisible());
                        break;
                    case BICst.DASHBOARD_WIDGET_EXCEL:
                        break;
                    case BICst.DASHBOARD_WIDGET_COPY :
                        self.model.copy();
                        break;
                    case BICst.DASHBOARD_WIDGET_DELETE :
                        self.model.destroy();
                        break;
                }
            });

            return BI.createWidget({
                type: "bi.border",
                cls: "dashboard-title",
                items: {
                    center: text,
                    east: {
                        el: BI.createWidget({
                            type: "bi.center_adapt",
                            cls: "operator-region",
                            items: [expand, combo]
                        }),
                        width: 64
                    }
                }
            });
        } else {
            this.title.setValue(BI.Utils.getWidgetNameByID(this.model.get("id")));
        }
    },

    _refreshTableAndFilter: function () {
        BI.isNotNull(this.filterPane) && this.filterPane.populate();
        this.table.populate();
    },

    _expandWidget: function () {
        var wId = this.model.get("id");
        var type = this.model.get("type");
        this.addSubVessel("detail", "body", {
            isLayer: true
        }).skipTo("detail", "detail", "detail", {}, {
            id: wId
        })
    },


    listenEnd: function () {

    },

    change: function (changed) {
        if (BI.has(changed, "clicked") || BI.has(changed, "filter_value")) {
            this._refreshTableAndFilter();
        }
        if (BI.has(changed, "dimensions") ||
            BI.has(changed, "sort_sequence")) {
            this.table.populate();
        }
    },

    local: function () {
        return false;
    },

    refresh: function () {
        this._buildWidgetTitle();
        this.table.populate();
    }
});