package com.fr.swift.config.entity;

import com.fr.swift.config.TableAllotConf;
import com.fr.swift.config.bean.SwiftTableAllotConfBean;
import com.fr.swift.config.convert.FRAllotRuleConverter;
import com.fr.swift.config.entity.key.TableId;
import com.fr.swift.converter.ObjectConverter;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.alloter.AllotRule;
import com.fr.third.javax.persistence.Column;
import com.fr.third.javax.persistence.Convert;
import com.fr.third.javax.persistence.Entity;
import com.fr.third.javax.persistence.Id;
import com.fr.third.javax.persistence.Table;


/**
 * @author anchore
 * @date 2018/7/2
 */
@Entity
@Table(name = "fine_swift_tab_idx_conf")
public class SwiftTableAllotConf implements TableAllotConf, ObjectConverter<SwiftTableAllotConfBean> {
    @Id
    private TableId tableId;

    @Column(name = "allotRule")
    @Convert(converter = FRAllotRuleConverter.class)
    private AllotRule allotRule;

    public SwiftTableAllotConf() {
    }

    public SwiftTableAllotConf(SourceKey tableKey, AllotRule allotRule) {
        this.tableId = new TableId(tableKey);
        this.allotRule = allotRule;
    }

    public SwiftTableAllotConf(SwiftTableAllotConfBean bean) {
        this(new SourceKey(bean.getTableKey()), bean.getAllotRule());
    }

    @Override
    public SourceKey getTable() {
        return tableId.getTableKey();
    }

    @Override
    public AllotRule getAllotRule() {
        return allotRule;
    }

    @Override
    public SwiftTableAllotConfBean convert() {
        return new SwiftTableAllotConfBean(this.getTable().getId(), this.getAllotRule());
    }
}