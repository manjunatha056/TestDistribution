package com.appium.manager;

import com.appium.executor.MyTestExecutor;
import com.appium.ios.IOSDeviceConfiguration;
import com.github.lalyos.jfiglet.FigletFont;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This class picks the devices connected
 * and distributes across multiple thread.
 *
 */


public class ParallelThread {
    private final ConfigurationManager configurationManager;
    protected int deviceCount = 0;
    Map<String, String> devices = new HashMap<String, String>();
    Map<String, String> iOSdevices = new HashMap<String, String>();
    private AndroidDeviceConfiguration deviceConf = new AndroidDeviceConfiguration();
    private IOSDeviceConfiguration iosDevice = new IOSDeviceConfiguration();
    private MyTestExecutor myTestExecutor = new MyTestExecutor();
    List<Class> testcases;

    public ParallelThread() throws IOException {
        configurationManager = ConfigurationManager.getInstance();
    }

    public boolean runner(String pack, List<String> tests) throws Exception {
        figlet(configurationManager.getProperty("RUNNER"));
        return triggerTest(pack, tests);
    }

    public boolean runner(String pack) throws Exception {
        return runner(pack, new ArrayList<String>());
    }

    public boolean triggerTest(String pack, List<String> tests) throws Exception {
        return parallelExecution(pack, tests);
    }

    public boolean parallelExecution(String pack, List<String> tests) throws Exception {
        String operSys = System.getProperty("os.name").toLowerCase();
        File f = new File(System.getProperty("user.dir") + "/target/appiumlogs/");
        if (!f.exists()) {
            System.out.println("creating directory: " + "Logs");
            boolean result = false;
            try {
                f.mkdir();
                result = true;
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }

        if (configurationManager.getProperty("ANDROID_APP_PATH") != null
                && deviceConf.getDevices() != null) {
            devices = deviceConf.getDevices();
            deviceCount = devices.size() / 4;
            File adb_logs = new File(System.getProperty("user.dir") + "/target/adblogs/");
            if (!adb_logs.exists()) {
                System.out.println("creating directory: " + "ADBLogs");
                boolean result = false;
                try {
                    adb_logs.mkdir();
                    result = true;
                } catch (SecurityException se) {
                    se.printStackTrace();
                }
            }
            createSnapshotFolderAndroid(deviceCount, "android");
        }

        if (operSys.contains("mac")) {
            if (configurationManager.getProperty("IOS_APP_PATH") != null ) {
                if (iosDevice.getIOSUDID() != null) {
                    iosDevice.checkExecutePermissionForIOSDebugProxyLauncher();
                    iOSdevices = iosDevice.getIOSUDIDHash();
                    deviceCount += iOSdevices.size();
                    createSnapshotFolderiOS(deviceCount, "iPhone");
                }
            }


        }
        if (deviceCount == 0) {
            figlet("No Devices Connected");
            System.exit(0);
        }
        System.out.println("***************************************************\n");
        System.out.println("Total Number of devices detected::" + deviceCount + "\n");
        System.out.println("***************************************************\n");
        System.out.println("starting running tests in threads");

        testcases = new ArrayList<Class>();

        boolean hasFailures = false;
        if (configurationManager.getProperty("FRAMEWORK").equalsIgnoreCase("testng")) {
            // final String pack = "com.paralle.tests"; // Or any other package
            PackageUtil.getClasses(pack).stream().forEach(s -> {
                if (s.toString().contains("Test")) {
                    testcases.add((Class) s);
                }
            });

            if (configurationManager.getProperty("RUNNER").equalsIgnoreCase("distribute")) {
                hasFailures = myTestExecutor
                        .runMethodParallelAppium(tests, pack, deviceCount,
                                "distribute");

            }
            if (configurationManager.getProperty("RUNNER").equalsIgnoreCase("parallel")) {
                hasFailures = myTestExecutor
                        .runMethodParallelAppium(tests, pack, deviceCount,
                                "parallel");
            }
        }

        if (configurationManager.getProperty("FRAMEWORK").equalsIgnoreCase("cucumber")) {
            if (configurationManager.getProperty("RUNNER").equalsIgnoreCase("distribute")) {
                hasFailures = myTestExecutor.runMethodParallel(myTestExecutor
                        .constructXmlSuiteDistributeCucumber(deviceCount,
                                AppiumParallelTest.devices));
            } else if (configurationManager.getProperty("RUNNER").equalsIgnoreCase("parallel")) {
                hasFailures = myTestExecutor.runMethodParallel(myTestExecutor
                        .constructXmlSuiteForParallelCucumber(deviceCount,
                                AppiumParallelTest.devices));
            }
        }
        return hasFailures;
    }

    public void createSnapshotFolderAndroid(int deviceCount, String platform) throws Exception {
        for (int i = 1; i <= (devices.size() / 4); i++) {
            String deviceSerial = devices.get("deviceID" + i);
            if (deviceSerial != null) {
                createPlatformDirectory(platform);
                File file = new File(
                        System.getProperty("user.dir") + "/target/screenshot/" + platform + "/"
                                + deviceSerial.replaceAll("\\W", "_"));
                if (!file.exists()) {
                    if (file.mkdir()) {
                        System.out.println("Android " + deviceSerial + " Directory is created!");
                    } else {
                        System.out.println("Failed to create directory!");
                    }
                }
            }
        }
    }

    public void createSnapshotFolderiOS(int deviceCount, String platform) {
        for (int i = 0; i < iOSdevices.size(); i++) {
            String deviceSerial = iOSdevices.get("deviceID" + i);
            createPlatformDirectory(platform);
            File file = new File(
                    System.getProperty("user.dir") + "/target/screenshot/" + platform + "/"
                            + deviceSerial);
            if (!file.exists()) {
                if (file.mkdir()) {
                    System.out.println("IOS " + deviceSerial + " Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
        }
    }


    public void createPlatformDirectory(String platform) {
        File file2 = new File(System.getProperty("user.dir") + "/target/screenshot");
        if (!file2.exists()) {
            file2.mkdir();
        }

        File file3 = new File(System.getProperty("user.dir") + "/target/screenshot/" + platform);
        if (!file3.exists()) {
            file3.mkdir();
        }
    }

    public static void figlet(String text) {
        String asciiArt1 = null;
        try {
            asciiArt1 = FigletFont.convertOneLine(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(asciiArt1);
    }
}