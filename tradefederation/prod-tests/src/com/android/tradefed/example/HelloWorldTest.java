package com.android.tradefed.example;

import com.android.ddmlib.Log;
import com.android.ddmlib.Log.LogLevel;

import com.android.tradefed.config.Option;
import com.android.tradefed.device.DeviceNotAvailableException;
import com.android.tradefed.device.ITestDevice;
import com.android.tradefed.log.LogUtil;
import com.android.tradefed.result.ITestInvocationListener;
import com.android.tradefed.testtype.IDeviceTest;
import com.android.tradefed.testtype.IRemoteTest;

public class HelloWorldTest implements IRemoteTest, IDeviceTest {

    private ITestDevice mDevice;

    @Option(name="my_option",
            shortName='m',
            description="this is the option's help text",
            // always display this option in the default help text
            importance= Option.Importance.ALWAYS)
    private String mMyOption = "thisisthedefault";

    @Override
    public void run(ITestInvocationListener listener) throws DeviceNotAvailableException {
        System.out.println("Hello, TF World! I have device " + getDevice().getSerialNumber());
        LogUtil.CLog.logAndDisplay(LogLevel.INFO, "I received option '%s'", mMyOption);
        Log.d("test", "ddd");
    }

    @Override
    public void setDevice(ITestDevice device) {
        mDevice = device;
    }

    @Override
    public ITestDevice getDevice() {
        return mDevice;
    }
}