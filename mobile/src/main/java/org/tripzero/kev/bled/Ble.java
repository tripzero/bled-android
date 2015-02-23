package org.tripzero.kev.bled;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Parcel;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

interface BleListener
{
    void onBleDeviceDiscovered(Ble.Device device);
}


interface BleDeviceListener
{
    void onBleMessage(String message);
    void onReady();
    void onBleConnect();
    void onBleDisconnect();
}


public class Ble {

    private BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private BleListener iface;

    private static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public class Device {

        public String name;

        public String address;
        Service service;
        public BleDeviceListener listener;
        private BluetoothGattCharacteristic rx = null;
        private BluetoothGattCharacteristic tx = null;
        private BluetoothDevice bleDevice;
        private BluetoothGatt deviceGatt;

        public void setBleDevice(BluetoothDevice d)
        {
            bleDevice = d;
        }

        public void connect()
        {
            if(bleDevice == null)
                return;

            bleDevice.connectGatt(context, false, gattCallback);
        }

        public void disconnect()
        {
            deviceGatt.disconnect();
        }

        public boolean isValid() { return deviceGatt != null; }

        public void sendMessage(byte[] message)
        {
            if(deviceGatt == null) {
                System.out.println("deviceGatt is null.  cannot send message");
                return;
            }

            System.out.println("sending msg...");
            tx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            tx.setValue(message);
            deviceGatt.writeCharacteristic(tx);
        }

        public void setMessage(String message) {
            if(listener != null)
                listener.onBleMessage(message);
        }

        public void setConnected(boolean connected) {
            if(listener != null)
                if(connected == true)
                    listener.onBleConnect();
                else
                    listener.onBleDisconnect();
        }

        private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);

                String data = characteristic.getStringValue(0);
                System.out.println("received from device: " + data);
                setMessage(data);
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    deviceGatt = gatt;
                    gatt.discoverServices();
                    setConnected(true);
                }
                else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    setConnected(false);
                }

            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    System.out.println("Service discovery completed!");
                }
                else {
                    System.out.println("Service discovery failed with status: " + status);
                    return;
                }

                BluetoothGattService s = gatt.getService(service.serviceUuid.getUuid());

                if(s == null)
                {
                    System.out.println("Could not get service: " + service.serviceUuid.getUuid());
                    System.out.println("These are the services I see: ");
                    for(BluetoothGattService is : gatt.getServices()) {
                        System.out.println(is.getUuid());
                    }

                    return;
                }

                rx = s.getCharacteristic(service.rxUuid);
                tx = s.getCharacteristic(service.txUuid);

                if (rx != null && !gatt.setCharacteristicNotification(rx, true)) {
                    System.out.println("Couldn't set notifications for RX characteristic!");
                }

                listener.onReady();
            }
        };
    }

    public class Service {
        public ParcelUuid serviceUuid;
        public UUID rxUuid;
        public UUID txUuid;
    }

    private List<Service> services = new ArrayList<Service>();

    private Context context;
    private List<ScanFilter> filters = new ArrayList<ScanFilter>();

    public Ble(Context c, BleListener i)
    {
        context = c;
        iface = i;
        adapter = BluetoothAdapter.getDefaultAdapter();
        scanner = adapter.getBluetoothLeScanner();

        if(scanner == null)
        {
            System.out.println("Failed to get LE scanner");
            return;
        }
    }

    public void AddService(String deviceUuid, String r, String t)
    {
        Service service = new Service();
        service.serviceUuid = ParcelUuid.fromString(deviceUuid);
        service.rxUuid = UUID.fromString(r);
        service.txUuid = UUID.fromString(t);

        services.add(service);

        ScanFilter.Builder filter = new ScanFilter.Builder();
        filter.setServiceUuid(service.serviceUuid);

        filters.add(filter.build());

    }

    public void scan(boolean scan)
    {
        if(!scan)
        {
            scanner.stopScan(scanCallback);
            return;
        }

        ScanSettings.Builder settings = new ScanSettings.Builder();
        settings.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

        scanner.startScan(filters, settings.build(), scanCallback);
    }


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();

            System.out.println("I see a bluetooth device: " + device.getAddress());

            List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();

            Service theService = null;

            if(uuids != null) {
                for (ParcelUuid uuid :uuids) {
                    System.out.println("Service UUID: " + uuid.toString());
                    String foundUuid = uuid.toString();
                    for (Service service : services)
                    {
                        String myServiceUuid = service.serviceUuid.toString();
                        System.out.println("Checking if any of our services matches: " + myServiceUuid);
                        System.out.println(myServiceUuid + " ==? " + foundUuid);
                        System.out.println("lengths: " + myServiceUuid.length() + " vs. " + foundUuid.length());
                        if(myServiceUuid.equals(foundUuid))
                        {
                            System.out.println("Yes, it does!");
                            theService = service;
                        }
                        else
                        {
                            System.out.println("No it doesn't :(");
                        }
                    }
                }
            }

            if(theService == null)
            {
                System.out.println("Big problem: no viable service found!");
                return;
            }

            Device dev = new Device();

            dev.service = theService;
            dev.setBleDevice(device);
            iface.onBleDeviceDiscovered(dev);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for(ScanResult result : results)
            {
                BluetoothDevice device = result.getDevice();

                System.out.println("I see a bluetooth device: " + device.getAddress());
            }
        }
    };
}
