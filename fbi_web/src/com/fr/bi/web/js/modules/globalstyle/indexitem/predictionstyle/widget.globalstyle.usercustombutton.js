/**
 * Created by zcf on 2016/9/5.
 */
BI.GlobalStyleUserCustomButton = BI.inherit(BI.BasicButton, {
    _defaultConfig: function () {
        return BI.extend(BI.GlobalStyleUserCustomButton.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-global-style-user-custom-button",
            text: "",
            selected: false,
            value: null
        })
    },

    _init: function () {
        BI.GlobalStyleUserCustomButton.superclass._init.apply(this, arguments);
        var o = this.options;
        var self = this;
        this.button = BI.createWidget({
            type: "bi.button",
            level: "ignore",
            text: o.text,
            value: o.value,
            height: 70,
            width: 108
        });
        this.deleteButton = BI.createWidget({
            type: "bi.icon_button",
            cls: "close-red-font",
            height: 20,
            width: 20
        });
        this.button.on(BI.Button.EVENT_CHANGE, function () {
            self.fireEvent(BI.GlobalStyleUserCustomButton.EVENT_SELECT)
        });
        this.deleteButton.on(BI.IconButton.EVENT_CHANGE, function () {
            self.fireEvent(BI.GlobalStyleUserCustomButton.EVENT_DELETE)
        });
        this.widget = BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: this.button
            }, {
                el: this.deleteButton
            }],
            height: 70,
            width: 110,
            rgap: 0,
            tgap: 0
        });
        this.deleteButton.setVisible(false);
        this.widget.element.hover(function () {
            self.deleteButton.setVisible(true);
        }, function () {
            self.deleteButton.setVisible(false);
        })
    },

    getValue: function () {
        return this.button.getValue()
    },

    setValue: function (v) {
        this.button.setValue(v)
    }
});
BI.GlobalStyleUserCustomButton.EVENT_SELECT = "BI.GlobalStyleUserCustomButton.EVENT_SELECT";
BI.GlobalStyleUserCustomButton.EVENT_DELETE = "BI.GlobalStyleUserCustomButton.EVENT_DELETE";
$.shortcut("bi.global_style_user_custom_button", BI.GlobalStyleUserCustomButton);