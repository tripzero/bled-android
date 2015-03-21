package org.tripzero.kev.bled;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gc.materialdesign.widgets.ColorSelector;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.tripzero.kev.bled.adapters.LEDAdapter;
import org.tripzero.kev.bled.utils.BaseActivity;
import org.tripzero.kev.bled.utils.DimDialog;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements ColorSelector.OnColorSelectedListener,BleListener,LEDAdapter.OnFeedItemClickListener {

    private Ble ble;
    public int backgroundColor = Color.parseColor("#039BE5");
    private List<Ble.Device> devices = new ArrayList<>();
    LEDAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private List<ParseObject> mItems;
    private boolean pendingIntroAnimation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarIcon(R.drawable.ic_bulb);
        mItems = new ArrayList<ParseObject>();

        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        } else {

            mAdapter.updateItems(false, 0);

        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        getSampleData();
        mAdapter = new LEDAdapter(this, mItems);
        mAdapter.setOnFeedItemClickListener(MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


//        ble = new Ble(getApplicationContext(), this);
//        ble.AddService("5faaf494-d4c6-483e-b592-d1a6ffd436c9", "5faaf495-d4c6-483e-b592-d1a6ffd436c9", "5faaf496-d4c6-483e-b592-d1a6ffd436c9");
//        ble.scan(true);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }


    public List<ParseObject> getSampleData() {


        // Query to see if user exists
        ParseQuery<ParseObject> query = ParseQuery.getQuery("LEDtest");
        query.findInBackground( new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> data, ParseException e) {

                if(data != null){

                    mItems.addAll(data);
                   // mRecyclerView.setItemAnimator(new FadeInAnimator());

                    mAdapter.notifyDataSetChanged();

                    if(mItems != null){

                        mAdapter.updateItems(true,0);
                    }
                }

                else {


                }
            }
        });


        return  mItems;
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

    @Override
    public void onColorClick(View v, int position) {

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
    public void onColorSelected(int color) {
        backgroundColor = color;

    }
}
