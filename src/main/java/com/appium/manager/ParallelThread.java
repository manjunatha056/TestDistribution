package com.appium.manager;

import com.appium.executor.MyTestExecutor;
import com.appium.ios.IOSDeviceConfiguration;
import com.github.lalyos.jfiglet.FigletFont;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/*
 * This class picks the devices connected
 * and distributes across multiple thread.
 */


public class ParallelThread {
    protected int deviceCount = 0;
    Map<String, String> devices = new HashMap<String, String>();
    Map<String, String> iOSdevices = new HashMap<String, String>();
    private AndroidDeviceConfiguration deviceConf = new AndroidDeviceConfiguration();
    private IOSDeviceConfiguration iosDevice = new IOSDeviceConfiguration();
    private MyTestExecutor myTestExecutor = new MyTestExecutor();
    public Properties prop = new Properties();
    public InputStream input = null;
    List<Class> testcases;

    public ParallelThread() throws IOException {
        input = new FileInputStream("config.properties");
        prop.load(input);
    }

    public void runner(String pack, List<String> tests) throws Exception {
        figlet(prop.getProperty("RUNNER"));
        triggerTest(pack, tests);
    }

    public void runner(String pack) throws Exception {
        figlet(prop.getProperty("RUNNER"));
        List<String> test = new ArrayList<>();
        triggerTest(pack, test);
    }

    public void triggerTest(String pack, List<String> tests) throws Exception {
        parallelExecution(pack, tests);
    }

    public void parallelExecution(String pack, List<String> tests) throws Exception {
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

        if (prop.getProperty("ANDROID_APP_PATH") != null && deviceConf.getDevices() != null) {
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
            if (iosDevice.getIOSUDID() != null) {
                iosDevice.checkExecutePermissionForIOSDebugProxyLauncher();
                iOSdevices = iosDevice.getIOSUDIDHash();
                deviceCount += iOSdevices.size();
                createSnapshotFolderiOS(deviceCount, "iPhone");
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

        if (prop.getProperty("FRAMEWORK").equalsIgnoreCase("testng")) {
            // final String pack = "com.paralle.tests"; // Or any other package
            PackageUtil.getClasses(pack).stream().forEach(s -> {
                if (s.toString().contains("Test")) {
                    testcases.add((Class) s);
                }
            });

            if (prop.getProperty("RUNNER").equalsIgnoreCase("distribute")) {
                myTestExecutor
                    .runMethodParallelAppium(tests, pack, deviceCount,
                        "distribute");

            }
            if (prop.getProperty("RUNNER").equalsIgnoreCase("parallel")) {
                myTestExecutor
                    .runMethodParallelAppium(tests, pack, deviceCount,
                        "parallel");
            }
        }

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
