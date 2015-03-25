package org.tripzero.kev.bled;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gc.materialdesign.widgets.ColorSelector;

import org.tripzero.kev.bled.adapters.LEDAdapter;
import org.tripzero.kev.bled.utils.BaseActivity;
import org.tripzero.kev.bled.utils.DimDialog;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements ColorSelector.OnColorSelectedListener, BleListener, LEDAdapter.OnFeedItemClickListener {

    public class LEDDevice {
        public Ble.Device device;
        public int red;
        public int green;
        public int blue;
        public boolean mOn = false;

        public LEDDevice(Ble.Device device)
        {
            this.device = device;
        }

        public void colorTest()
        {
            byte[] msg = new byte[1];
            msg[0] = 'e';
            device.sendMessage(msg);
        }

        public void fromRGB(int r, int g, int b)
        {
            red = r;
            green = g;
            blue = b;
            postColors();
        }

        public int toInt()
        {
            if(mOn)
            {
                return Color.rgb(red, green, blue);
            }
            return Color.rgb(0, 0, 0);
        }

        public void fromInt(int color)
        {
            red = (color >> 16) & 0xFF;
            green = (color >> 8) & 0xFF;
            blue = (color >> 0) & 0xFF;

            postColors();
        }

        public boolean isOn()
        {
            return mOn;
        }

        public void setOn(boolean o)
        {
            mOn = o;
            byte[] msg = new byte[2];
            msg[0] = 's';
            msg[1] = mOn ? (byte)1 : (byte)0;

            device.sendMessage(msg);
        }

        public void queryStatus()
        {
            byte[] msg = new byte[1];
            msg[0] = '?';
            device.sendMessage(msg);
        }


        private void postColors()
        {
            byte[] msg = new byte[4];
            msg[0] = 'c';
            msg[1] = (byte)red ;
            msg[2] = (byte)green;
            msg[3] = (byte)blue;

            device.sendMessage(msg);
        }

    }
    private Ble ble;
    public int backgroundColor = Color.parseColor("#039BE5");
    private List<LEDDevice> devices = new ArrayList<>();
    LEDAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private boolean pendingIntroAnimation;
    private LEDDevice selectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarIcon(R.drawable.ic_bulb);

        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        } else {
//            mAdapter.updateItems(false, 0);
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mAdapter = new LEDAdapter(this, devices);
        mAdapter.setOnFeedItemClickListener(MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        ble = new Ble(getApplicationContext(), this);
        ble.AddService("5faaf494-d4c6-483e-b592-d1a6ffd436c9", "5faaf495-d4c6-483e-b592-d1a6ffd436c9", "5faaf496-d4c6-483e-b592-d1a6ffd436c9");
        ble.scan(true);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
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

        final LEDDevice light = new LEDDevice(dev);
        if(isUniqueDevice(dev))
        {

            devices.add(light);
            mAdapter.updateItems(true, 0);
        }

        light.device.listener = new BleDeviceListener() {
            @Override
            public void onBleMessage(byte[] message) {
                for(int i = 0; i < message.length; i++) {
                    byte c = message[i];
                    if(c == 'c') {
                        light.red = (int)message[++i];
                        light.green = (int)message[++i];
                        light.blue = (int)message[++i];
                        System.out.println("color changed msg received");
                        System.out.println(light.red);
                        System.out.println(light.blue);
                        System.out.println(light.green);
                    }
                    else if(c == 's')
                    {
                        light.mOn = (message[++i] != 0);
                        System.out.println("Light status: " + String.valueOf(light.isOn()));
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateItems(true, 0);
                    }
                });
            }

            @Override
            public void onReady() {
                /// turn led to green:
                System.out.println("We are ready!");
                light.colorTest();
                light.queryStatus();
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

    }

    private boolean isUniqueDevice(Ble.Device device)
    {
        for(LEDDevice dev : devices)
        {
            if(dev.device.address().equals(device.address()))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onColorClick(View v, int position) {
        selectedDevice = devices.get(position);
        ColorSelector colorSelector = new ColorSelector(MainActivity.this, backgroundColor, MainActivity.this);
        colorSelector.show();
    }

    @Override
    public void onSettingsClick(View v, int position) {
        // Also send any additional data to Dialog fragment. ie Device ID
        FragmentActivity activity = (FragmentActivity) (MainActivity.this);
        FragmentManager fm = activity.getSupportFragmentManager();
        DimDialog submitDialog = new DimDialog();
        submitDialog.show(fm, "");
    }

    @Override
    public void onLedClicked(int position) {
        System.out.println("clicked on the led.  connecting...");
        selectedDevice = devices.get(position);
        selectedDevice.device.connect();
    }

    @Override
    public void onToggleOnClicked(int position, boolean on) {
        selectedDevice = devices.get(position);
        selectedDevice.setOn(on);
    }


    @Override
    public void onColorSelected(int color) {
        backgroundColor = color;
        selectedDevice.fromInt(color);
        mAdapter.updateItems(true, 0);
    }
}
