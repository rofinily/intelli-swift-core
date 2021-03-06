package com.fr.swift.config.convert;

import com.fr.swift.SwiftContext;
import com.fr.swift.config.SegmentDestSelectRule;
import com.fr.swift.config.bean.SwiftConfigBean;
import com.fr.swift.config.dao.SwiftConfigDao;
import com.fr.swift.config.oper.ConfigSession;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author yee
 * @date 2018-11-29
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SwiftContext.class)
public class SegDestSelectRuleConvertTest {
    private SegDestSelectRuleConvert convert = new SegDestSelectRuleConvert();

    @Test
    public void toBean() throws SQLException {
        SwiftConfigDao mockSwiftConfigDao = PowerMock.createMock(SwiftConfigDao.class);
        EasyMock.expect(mockSwiftConfigDao.select(EasyMock.anyObject(ConfigSession.class),
                EasyMock.eq(convert.getNameSpace() + ".class"))).andReturn(new SwiftConfigBean(convert.getNameSpace() + ".class", TestSegDestSelectRule.class.getName())).once();
        EasyMock.expect(mockSwiftConfigDao.select(EasyMock.anyObject(ConfigSession.class),
                EasyMock.eq(convert.getNameSpace() + ".class"))).andReturn(new SwiftConfigBean(convert.getNameSpace() + ".class", SegDestSelectRuleConvertTest.class.getName())).once();
        ConfigSession mockConfigSession = PowerMock.createMock(ConfigSession.class);
        PowerMock.mockStatic(SwiftContext.class);
        SwiftContext mockSwiftContext = PowerMock.createMock(SwiftContext.class);
        EasyMock.expect(SwiftContext.get()).andReturn(mockSwiftContext).anyTimes();
        PowerMock.replay(SwiftContext.class);
        EasyMock.expect(SwiftContext.get().getBean(EasyMock.eq("defaultSegmentDestSelectRule"), EasyMock.eq(SegmentDestSelectRule.class))).andReturn(new TestSegDestSelectRule("defaultSegmentDestSelectRule"));
        PowerMock.replayAll();
        TestSegDestSelectRule rule = (TestSegDestSelectRule) convert.toBean(mockSwiftConfigDao, mockConfigSession);
        assertNull(rule.toString());
        TestSegDestSelectRule rule1 = (TestSegDestSelectRule) convert.toBean(mockSwiftConfigDao, mockConfigSession);
        assertEquals(rule1.toString(), "defaultSegmentDestSelectRule");
        PowerMock.verifyAll();
    }
}