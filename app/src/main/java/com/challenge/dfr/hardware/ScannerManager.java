package com.challenge.dfr.hardware;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.brother.sdk.BrotherAndroidLib;
import com.brother.sdk.common.ConnectorDescriptor;
import com.brother.sdk.common.ConnectorManager;
import com.brother.sdk.common.IConnector;
import com.brother.sdk.common.Job;
import com.brother.sdk.common.device.CountrySpec;
import com.brother.sdk.common.device.scanner.ScanPaperSource;
import com.brother.sdk.common.device.scanner.ScanSpecialMode;
import com.brother.sdk.common.socket.scan.scancommand.ScanCommandContext;
import com.brother.sdk.network.NetworkControllerManager;
import com.brother.sdk.network.discovery.mfc.BrotherMFCNetworkConnectorDiscovery;
import com.brother.sdk.network.wifi.WifiNetworkController;
import com.brother.sdk.scan.ScanJob;
import com.brother.sdk.scan.ScanJobController;
import com.brother.sdk.scan.ScanParameters;
import com.brother.sdk.usb.discovery.UsbConnectorDiscovery;
import com.challenge.dfr.logical.CaseManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScannerManager {
    private static List<ConnectorDescriptor> mDescriptors;
    private static UsbConnectorDiscovery mUsbDiscovery;
    private static BrotherMFCNetworkConnectorDiscovery mNetworkDiscovery;
    private static ConnectorManager.OnDiscoverConnectorListener mDiscoverConnectorListener;
    public enum CONNECTION {  WIFI, USB };

    public static void connect (CONNECTION conn, Context ctx) {
        if (conn == CONNECTION.WIFI) {
            discover();

            ArrayList<String> addresses = new ArrayList<>();
            //WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            //DhcpInfo info = wifi.getDhcpInfo();
            //addresses.add(intToIp(info.gateway)); //TODO Look into WiFi Direct.

            BrotherAndroidLib.initialize(ctx);
            WifiNetworkController controller = NetworkControllerManager.getWifiNetworkController();
            addresses.add(controller.getBroadcastAddress().getHostAddress());

            executeNetworkDeviceDiscovery(addresses);
        } else if (conn == CONNECTION.USB) {
            discover();
            executeUsbDeviceDiscovery();
        }
    }

    public static boolean isConnected() {
        boolean connected = mDescriptors != null && mDescriptors.size() > 0;
        System.out.println("isConnected: " + connected);
        return connected;
    }

    public static IConnector createIConnector(Context context)
    {
        if (isConnected())
        {
            IConnector connector = mDescriptors.get(0)
                    .createConnector(
                            CountrySpec
                                    .fromISO_3166_1_Alpha2(
                                            context
                                                    .getResources()
                                                    .getConfiguration()
                                                    .locale
                                                    .getCountry()
                                    )
                    );
            System.out.println("createIConnector: success");
            return connector;
        }
        System.out.println("createIConnector: failure");
        return null;
    }

    public static void executeScan(IConnector connector, Activity activity) {
        Looper.prepare();
        Handler mHandler = new Handler();
        Job.JobState jobState = null;
        ScanJob mScanJob = null;
        try {
            //TODO Fix dual sided input.
            ScanParameters mScanParameters = new ScanParameters();
            mScanParameters.paperSource = ScanPaperSource.ADF;
            mScanParameters.specialScanMode = ScanSpecialMode.EDGE_SCAN;
            mScanJob = new ScanJob(mScanParameters, activity,
                    new ScanJobController(activity.getApplicationContext().getFilesDir()) {
                        // The "value" is progress value in scan processing which is between 0 to 100 per page.
                        public void onUpdateProcessProgress(int value) {
                        }

                        // This callback would not be called if any response has not come from our device.
                        public void onNotifyProcessAlive() {
                        }

                        // This callback would be called when scanned image has been retained.
                        public void onImageReadToFile(String scannedImagePath, int pageIndex) {
                            Bitmap in = BitmapFactory.decodeFile(scannedImagePath);
                            CaseManager.addEvidence(in, pageIndex);
                        }
                    });
            // [Brother Comment]
            // The process has been executed synchronously, so in almost cases you should implement the call of IConnector.submit(ScanJob) in Thread.
            jobState = connector.submit(mScanJob);
        } catch (Exception e) {
            System.out.println("Error: " + e  + " state: " + jobState);
            mScanJob.cancel();
        } finally {
             mHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     CaseManager.completeNewEvidence();
                 }
             });
        }
    }

    private static void discover() {
        if (mDiscoverConnectorListener != null) { return; }
        System.out.println("discover()");
        mDiscoverConnectorListener = new ConnectorManager.OnDiscoverConnectorListener() {
            @Override
            public void onDiscover(ConnectorDescriptor descriptor)  {
                if (descriptor.support(ConnectorDescriptor.Function.Scan))
                {
                    if (mDescriptors == null)
                    {
                        System.out.println("discover() creating descriptors");
                        mDescriptors = new ArrayList<ConnectorDescriptor>();
                    }

                    if (!mDescriptors.contains(descriptor))
                    {
                        System.out.println("discover() adding descriptor");
                        mDescriptors.add(descriptor);
                    }
                }
            }
        };
    }


    private static void executeUsbDeviceDiscovery()
    {
        System.out.println("executeUsbDeviceDiscovery()");
        mUsbDiscovery = new UsbConnectorDiscovery();
        mUsbDiscovery.startDiscover(mDiscoverConnectorListener);
    }

    private static void executeNetworkDeviceDiscovery(List<String> searchAddresses)
    {
        System.out.println("executeNetworkDeviceDiscovery(): " + Arrays.toString(searchAddresses.toArray()));
        mNetworkDiscovery = new BrotherMFCNetworkConnectorDiscovery(searchAddresses);
        mNetworkDiscovery.startDiscover(mDiscoverConnectorListener);
    }

    // Magic from https://stackoverflow.com/a/5391763
    private static String intToIp(int i) {
        return ( i & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ((i >> 24 ) & 0xFF );
    }
}
