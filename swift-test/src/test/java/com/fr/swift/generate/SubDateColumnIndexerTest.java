package com.fr.swift.generate;

import com.fr.swift.context.SwiftContext;
import com.fr.swift.generate.history.index.SubDateColumnIndexer;
import com.fr.swift.generate.history.transport.TableTransporter;
import com.fr.swift.manager.LocalSegmentProvider;
import com.fr.swift.query.group.GroupType;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.column.Column;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.segment.column.DictionaryEncodedColumn;
import com.fr.swift.segment.column.impl.DateDerivers;
import com.fr.swift.segment.column.impl.SubDateColumn;
import com.fr.swift.service.LocalSwiftServerService;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.db.QueryDBSource;
import com.fr.swift.test.Preparer;
import com.fr.swift.util.function.Function;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author anchore
 * @date 2018/3/24
 */
public class SubDateColumnIndexerTest {
    private LocalSegmentProvider segmentProvider = SwiftContext.get().getBean(LocalSegmentProvider.class);
    private String columnName = "注册时间";
    private DataSource dataSource = new QueryDBSource("select " + columnName + " from DEMO_CONTRACT", getClass().getName());

    @BeforeClass
    public static void beforeClass() throws Exception {
        new LocalSwiftServerService().start();
        Preparer.prepareCubeBuild();
    }

    @Before
    public void before() throws Exception {
        segmentProvider = SwiftContext.get().getBean(LocalSegmentProvider.class);
        new TableTransporter(dataSource).transport();
        List<Segment> segments = segmentProvider.getSegment(dataSource.getSourceKey());
        new ColumnIndexer(dataSource, new ColumnKey(columnName), segments).buildIndex();
    }

    @Test
    public void testIndexSubDate() {
        for (GroupType type : SubDateColumn.TYPES_TO_GENERATE) {
            indexSubDate(type);
        }
    }

    private void indexSubDate(GroupType type) {
        List<Segment> segments = segmentProvider.getSegment(dataSource.getSourceKey());
        new SubDateColumnIndexer(dataSource, new ColumnKey(columnName), type, segments).work();

        assertTrue(!segments.isEmpty());

        Column origin = segments.get(0).getColumn(new ColumnKey(columnName));
        Column derive = new SubDateColumn(origin, type);

        DictionaryEncodedColumn originDict = origin.getDictionaryEncodedColumn();
        DictionaryEncodedColumn deriveDict = derive.getDictionaryEncodedColumn();

        Function dateDeriver = DateDerivers.newDeriver(type);
        for (int i = 1; i < originDict.size(); i++) {
            Object deriveVal = dateDeriver.apply(originDict.getValue(i));
            assertTrue(deriveDict.getIndex(deriveVal) != -1);
        }
    }
}