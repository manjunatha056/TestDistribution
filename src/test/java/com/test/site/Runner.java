package com.test.site;

import com.appium.manager.AppiumParallelTest;
import com.appium.manager.ParallelThread;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Runner {
    @Test public static void testApp() throws Exception {

        ParallelThread parallelThread = new ParallelThread();
        /*List<String> tests = new ArrayList<>();
        tests.add("HomePageTest2");
        parallelThread.runner("com.test.site",tests);*/
        parallelThread.runner("com.test.site");
        Assert.assertFalse(AppiumParallelTest.buildStatus());
    }
}
