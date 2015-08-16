package me.joelbraun.bandtools;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.SampleRate;

enum TempMode{
    Fahrenheit,
    Celsius
}

enum DCSpeed{
    FiveHundred,
    OneThousand
}


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, SettingsFragment.OnFragmentInteractionListener, SensorFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    //Preferences Data
    private TempMode tempMode = TempMode.Fahrenheit;
    private SampleRate sampleRate = SampleRate.MS128;
    private String sensorID;
    private DCSpeed DCspeed = DCSpeed.FiveHundred;
    boolean SaveData;

    //Client-Communication Data
    static BandClient client = null;
    BandInfo[] devices;
    BandSensorManager sensorManager;
    BandSkinTemperatureEventListener tempListener;
    BandPedometerEventListener pedListener;
    BandDistanceEventListener distListener;
    BandUVEventListener uvListener;
    BandHeartRateEventListener hrListener;
    BandAccelerometerEventListener accListener;
    BandGyroscopeEventListener gyroListener;
    HeartRateConsentListener hrConsent;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.ic_drawer);

        try {
            getConnectedBandClient();
        } catch (Exception e){
            Log.w("Error", "e");
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;

        switch(position) {
            case 0:
                fragment = SensorFragment.newInstance(sampleRate, tempMode, client);
                break;
            case 1:
                fragment = SettingsFragment.newInstance(sampleRate, tempMode);
                break;
            default:
                fragment = new SensorFragment();
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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


/*Fragment Interaction items as defined.*/
    public void onFragmentInteraction(String SensorID)
    {
        sensorID = SensorID;
    }

    public void onFragmentInteraction(SampleRate sRate, DCSpeed dCspeed, TempMode tMode)
    {
        sampleRate = sRate;
        DCspeed = dCspeed;
        tempMode = tMode;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (client != null){
            try {
                client.getSensorManager().unregisterAllListeners();
                SaveData = false;
            } catch (BandIOException e){
                Log.w("Error Unregistering", e);
            } } }

    @Override
    public void onResume(){
        super.onResume();
        try {
            client.getSensorManager().registerSkinTemperatureEventListener(tempListener);
            client.getSensorManager().registerPedometerEventListener(pedListener);
            client.getSensorManager().registerDistanceEventListener(distListener);
            client.getSensorManager().registerUVEventListener(uvListener);
            client.getSensorManager().registerAccelerometerEventListener(accListener, sampleRate);
            client.getSensorManager().registerGyroscopeEventListener(gyroListener, sampleRate);
            client.getSensorManager().registerHeartRateEventListener(hrListener);
        } catch (Exception e) {
            Log.w("Error registering", e);
        }}

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                Log.w("ERROR","Band isn't paired with your phone...");
                return false;
            }
            client = BandClientManager.getInstance().create(this, devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }
        Log.w("CONNECTING", "Band is connecting...");
        return ConnectionState.CONNECTED == client.connect().await();
    }
}
