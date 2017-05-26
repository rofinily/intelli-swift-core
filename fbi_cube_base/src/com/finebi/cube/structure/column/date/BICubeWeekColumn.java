package com.finebi.cube.structure.column.date;

import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.structure.column.BICubeIntegerColumn;
import com.fr.bi.base.ValueConverterFactory;
import com.fr.bi.stable.constant.DateConstant;
import com.fr.bi.stable.io.newio.NIOConstant;

/**
 * This class created on 2016/3/30.
 * 星期几1-7
 * @author Connery
 * @since 4.0
 */
public class BICubeWeekColumn extends BICubeDateSubColumn<Integer> {
    public BICubeWeekColumn(ICubeResourceDiscovery discovery, ICubeResourceLocation currentLocation, BICubeDateColumn hostDataColumn) {
        super(discovery, currentLocation, hostDataColumn);
    }

    @Override
    protected Integer convertDate(Long date) {
        return date != NIOConstant.LONG.NULL_VALUE? (Integer) ValueConverterFactory.createDateValueConverter(DateConstant.DATE.SEASON).result2Value(date) : NIOConstant.INTEGER.NULL_VALUE;

    }

    @Override
    protected void initialColumnEntity(ICubeResourceLocation currentLocation) {
        selfColumnEntity = new BICubeIntegerColumn(discovery, currentLocation);

    }

    public int getGroupValue(int position) {
        return ((BICubeIntegerColumn)selfColumnEntity).getGroupValue(position);
    }

    /**
     * 获取空值表示对象
     *
     * @return
     */
    @Override
    public Integer getCubeNullValue() {
        return NIOConstant.INTEGER.NULL_VALUE;
    }
}
