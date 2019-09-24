/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.extension.execution.list;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.event.Event;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;
import io.siddhi.core.util.EventPrinter;
import io.siddhi.core.util.SiddhiTestHelper;
import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test case for list:get.
 */
public class GetFunctionExtensionExtensionTestCase {
    private static Logger log = Logger.getLogger(GetFunctionExtensionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testGetFunctionExtension() throws InterruptedException {
        log.info("GetFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price double, volume long);";

        String query = ("@info(name = 'query1') from inputStream " +
                "select symbol,price,list:create() as tmpList" +
                " insert into tmpStream;" +
                "@info(name = 'query2') " +
                "from tmpStream  " +
                "select symbol,price,tmpList,list:add(tmpList, price) as list1" +
                " insert into outputStream;" +
                "@info(name = 'query3') from outputStream  select list1, list:get(list1, 0) as price" +
                " insert into outputStream2;"
        );

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                inStreamDefinition + query);

        siddhiAppRuntime.addCallback("outputStream2", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();

                    if (count.get() == 1) {
                        AssertJUnit.assertEquals(100d, event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals(200d, event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals(300d, event.getData(1));
                        eventArrived = true;
                    }
                }
            }
        });
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 100d, 100L});
        inputHandler.send(new Object[]{"WSO2", 200d, 200L});
        inputHandler.send(new Object[]{"XYZ", 300d, 200L});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }


    @Test
    public void testGetFunctionExtension2() throws InterruptedException {
        log.info("GetFunctionExtension TestCase 2");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price long, volume long);";
        String query = ("@info(name = 'query1') from inputStream" +
                " select symbol,price,list:create() as tmpList " +
                "insert into tmpStream;" +
                "@info(name = 'query2') from tmpStream  " +
                "select symbol,price,tmpList, list:add(tmpList,price) as list1" +
                " insert into outputStream;" +
                "@info(name = 'query3') from outputStream  select list1," +
                " convert(cast(list:get(list1, 0), 'int'), 'string') as priceInString" +
                " insert into outputStream2;"
        );

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(
                inStreamDefinition + query);

        siddhiAppRuntime.addCallback("outputStream2", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event : events) {
                    count.incrementAndGet();

                    if (count.get() == 1) {
                        AssertJUnit.assertEquals("100", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        AssertJUnit.assertEquals("200", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        AssertJUnit.assertEquals("300", event.getData(1));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();
        inputHandler.send(new Object[]{"IBM", 100, 100L});
        inputHandler.send(new Object[]{"WSO2", 200, 200L});
        inputHandler.send(new Object[]{"XYZ", 300, 200L});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        AssertJUnit.assertEquals(3, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void testGetFunctionExtension3() {
        log.info("GetFunctionExtension TestCase with test attributeExpressionExecutors length");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "\ndefine stream inputStream (symbol string, price double, volume long);";

        String query = ("@info(name = 'query1') from inputStream " +
                "select symbol,price,list:create() as tmpList" +
                " insert into tmpStream;" +
                "@info(name = 'query2') " +
                "from tmpStream  " +
                "select symbol,price,tmpList,list:put(tmpList,symbol,price) as list1" +
                " insert into outputStream;" +
                "@info(name = 'query3') from outputStream  select list1, list:get(list1,symbol,price) as price" +
                " insert into outputStream2;"
        );
        siddhiManager.createSiddhiAppRuntime(inStreamDefinition + query);
    }

}
