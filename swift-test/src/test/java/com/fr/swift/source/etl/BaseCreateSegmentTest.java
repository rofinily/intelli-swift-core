package com.fr.swift.source.etl;

import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.bitmap.MutableBitMap;
import com.fr.swift.bitmap.impl.BitSetMutableBitMap;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.column.Column;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.segment.relation.CubeMultiRelation;
import com.fr.swift.segment.relation.CubeMultiRelationPath;
import com.fr.swift.segment.relation.RelationIndex;
import com.fr.swift.source.SwiftMetaData;

/**
 * Created by Handsome on 2017/12/26 0026 09:24
 */
public class BaseCreateSegmentTest {

    public Segment getSegment() {
        return getSegment(null);
    }

    public Segment getSegment(SwiftMetaData metaData) {
        return new Segment() {
            @Override
            public void flush() {

            }

            @Override
            public int getRowCount() {
                return 9;
            }

            @Override
            public void putRowCount(int rowCount) {

            }

            @Override
            public RelationIndex getRelation(CubeMultiRelation f) {
                return null;
            }

            @Override
            public RelationIndex getRelation(CubeMultiRelationPath f) {
                return null;
            }

            @Override
            public RelationIndex getRelation(ColumnKey f, CubeMultiRelationPath relationPath) {
                return null;
            }


            @Override
            public IResourceLocation getLocation() {
                return null;
            }

            @Override
            public boolean isReadable() {
                return false;
            }

            @Override
            public Column getColumn(ColumnKey key) {
                if (key.getName().equals("column1")) {
                    return new BaseCreateColumnTest().getColumn();
                } else if (key.getName().equals("column2")) {
                    return new BaseCreateColumnTest().getColumn();
                } else if (key.getName().equals("column3")) {
                    return new BaseCreateColumn2Test().getColumn();
                } else {
                    return new BaseCreateColumn2Test().getColumn();
                }
            }

            @Override
            public ImmutableBitMap getAllShowIndex() {
                MutableBitMap bitMap = BitSetMutableBitMap.newInstance();
                for (int i = 0; i < getRowCount(); i++) {
                    bitMap.add(i);
                }
                return bitMap;
            }

            @Override
            public void putAllShowIndex(ImmutableBitMap bitMap) {

            }

            @Override
            public SwiftMetaData getMetaData() {
                return metaData;
            }

            @Override
            public void release() {

            }

            @Override
            public boolean isHistory() {
                return false;
            }
        };
    }
}
