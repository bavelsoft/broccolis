package com.bavelsoft.broccolies.reg;

import static com.bavelsoft.broccolies.util.RegressionUtil.ru;
import org.junit.rules.TestName;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

public class J4RegBase {
	@Rule public TestName testName = new TestName();
	@Before public final void startTest() { ru.startTest(getClass().getName()+"."+testName.getMethodName()); }
	@After public final void stopTest() { ru.stopTest(); }
}

