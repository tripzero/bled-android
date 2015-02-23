package org.tripzero.kev.bled;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements BleListener {

    private Ble ble;

    private List<Ble.Device> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ble = new Ble(getApplicationContext(), this);
        ble.AddService("5faaf494-d4c6-483e-b592-d1a6ffd436c9", "5faaf495-d4c6-483e-b592-d1a6ffd436c9", "5faaf496-d4c6-483e-b592-d1a6ffd436c9");
        ble.scan(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBleDeviceDiscovered(Ble.Device device) {
        final Ble.Device dev = device;
        device.listener = new BleDeviceListener() {
            @Override
            public void onBleMessage(String message) {
                System.out.println("new message: " + message);
            }

            @Override
            public void onReady() {
                /// turn led to green:
                System.out.println("We are ready!");
                setColor(dev, (byte)0, (byte)100, (byte)0);
            }

            @Override
            public void onBleConnect() {
                System.out.println("connected!");
            }

            @Override
            public void onBleDisconnect() {
                System.out.println("disconnected!");
            }
        };

        devices.add(device);
        device.connect();
    }

    private void setColor(Ble.Device device, byte r, byte g, byte b)
    {
        byte[] msg = new byte[3];
        msg[0] = r;
        msg[1] = g;
        msg[2] = b;

        device.sendMessage(msg);
    }
}
