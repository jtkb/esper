/*
 * *************************************************************************************
 *  Copyright (C) 2006-2015 EsperTech, Inc. All rights reserved.                       *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 * *************************************************************************************
 */

package com.espertech.esper.epl.core;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.scopetest.EPAssertionUtil;
import com.espertech.esper.core.service.StatementEventTypeRefImpl;
import com.espertech.esper.epl.core.eval.SelectExprStreamDesc;
import com.espertech.esper.epl.spec.InsertIntoDesc;
import com.espertech.esper.epl.spec.SelectClauseExprCompiledSpec;
import com.espertech.esper.epl.spec.SelectClauseStreamSelectorEnum;
import com.espertech.esper.epl.table.mgmt.TableServiceImpl;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.support.bean.SupportBean;
import com.espertech.esper.support.core.SupportEngineImportServiceFactory;
import com.espertech.esper.support.epl.SupportSelectExprFactory;
import com.espertech.esper.support.epl.SupportStreamTypeSvc1Stream;
import com.espertech.esper.support.event.SupportEventAdapterService;
import com.espertech.esper.support.event.SupportEventBeanFactory;
import com.espertech.esper.support.event.SupportValueAddEventService;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;

public class TestSelectExprEvalProcessor extends TestCase
{
    private SelectExprProcessorHelper methodOne;
    private SelectExprProcessorHelper methodTwo;

    public void setUp() throws Exception
    {
        List<SelectClauseExprCompiledSpec> selectList = SupportSelectExprFactory.makeNoAggregateSelectList();
        EventAdapterService eventAdapterService = SupportEventAdapterService.getService();
        SupportValueAddEventService vaeService = new SupportValueAddEventService();
        SelectExprEventTypeRegistry selectExprEventTypeRegistry = new SelectExprEventTypeRegistry("abc", new StatementEventTypeRefImpl());
        MethodResolutionService methodResolutionService = new MethodResolutionServiceImpl(SupportEngineImportServiceFactory.make(), null);

        methodOne = new SelectExprProcessorHelper(Collections.<Integer>emptyList(), selectList, Collections.<SelectExprStreamDesc>emptyList(), null, null, false, new SupportStreamTypeSvc1Stream(), eventAdapterService, vaeService, selectExprEventTypeRegistry, methodResolutionService, null, null, new Configuration(), null, new TableServiceImpl());

        InsertIntoDesc insertIntoDesc = new InsertIntoDesc(SelectClauseStreamSelectorEnum.ISTREAM_ONLY, "Hello");
        insertIntoDesc.add("a");
        insertIntoDesc.add("b");

        methodTwo = new SelectExprProcessorHelper(Collections.<Integer>emptyList(), selectList, Collections.<SelectExprStreamDesc>emptyList(), insertIntoDesc, null, false, new SupportStreamTypeSvc1Stream(), eventAdapterService, vaeService, selectExprEventTypeRegistry, methodResolutionService, null, null, new Configuration(), null, new TableServiceImpl());
    }

    public void testGetResultEventType() throws Exception
    {
        EventType type = methodOne.getEvaluator().getResultEventType();
        EPAssertionUtil.assertEqualsAnyOrder(type.getPropertyNames(), new String[]{"resultOne", "resultTwo"});
        assertEquals(Double.class, type.getPropertyType("resultOne"));
        assertEquals(Integer.class, type.getPropertyType("resultTwo"));

        type = methodTwo.getEvaluator().getResultEventType();
        EPAssertionUtil.assertEqualsAnyOrder(type.getPropertyNames(), new String[]{"a", "b"});
        assertEquals(Double.class, type.getPropertyType("a"));
        assertEquals(Integer.class, type.getPropertyType("b"));
    }

    public void testProcess() throws Exception
    {
        EventBean[] events = new EventBean[] {makeEvent(8.8, 3, 4)};

        EventBean result = methodOne.getEvaluator().process(events, true, false, null);
        assertEquals(8.8d, result.get("resultOne"));
        assertEquals(12, result.get("resultTwo"));

        result = methodTwo.getEvaluator().process(events, true, false, null);
        assertEquals(8.8d, result.get("a"));
        assertEquals(12, result.get("b"));
        assertSame(result.getEventType(), methodTwo.getEvaluator().getResultEventType());
    }

    private EventBean makeEvent(double doubleBoxed, int intPrimitive, int intBoxed)
    {
        SupportBean bean = new SupportBean();
        bean.setDoubleBoxed(doubleBoxed);
        bean.setIntPrimitive(intPrimitive);
        bean.setIntBoxed(intBoxed);
        return SupportEventBeanFactory.createObject(bean);
    }
}
