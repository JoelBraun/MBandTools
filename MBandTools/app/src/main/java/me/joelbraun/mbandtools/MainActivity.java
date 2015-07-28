package me.joelbraun.mbandtools;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;

import com.microsoft.band.UserConsent;
import com.microsoft.band.internal.device.subscription.HeartRateData;
import com.microsoft.band.personalization.BandPersonalizationManager;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.MotionType;
import com.microsoft.band.sensors.SampleRate;
import com.microsoft.band.sensors.UVIndexLevel;

import com.opencsv.*;

import org.w3c.dom.Text;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

enum TempMode {
    Fahrenheit,
    Celsius
}

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks{
    private static BandClient client = null;
    public static BandSensorManager sensorManager;
    public static BandSkinTemperatureEventListener tempListener;
    public static BandPedometerEventListener pedListener;
    public static BandDistanceEventListener distListener;
    public static BandUVEventListener uvListener;
    public static BandHeartRateEventListener hrListener;
    public static BandAccelerometerEventListener accelListener;
    public static BandGyroscopeEventListener gyroListener;
    public static HeartRateConsentListener hrConsent;
    public static TextView ConnStatus;
    public static TextView SkinTempText;
    public static TextView pedCount;
    public static TextView totDistance;
    public static TextView UVValue;
    public static TextView HRValue;
    public static TextView AccelValue;
    public static TextView GyroValue;
    public static TextView PedSpeed;
    public static TextView PedMode;
    public static Button DCButton;
    public static CSVWriter mWriter;
    public static BandInfo[] devices;
    public static SampleRate sRate;
    public static TempMode tempMode;
    public static AsyncTask csvTask;
    public static boolean gconsent = false;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static boolean SaveData = false;
    public static List<String[]> data = new ArrayList<String[]>();

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
        bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(64, 0, 128)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        try {
            mWriter = new CSVWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() + "/BandData.csv"));
        } catch (Exception e){
            Log.w("app", e);
        }
       // new appTask().execute();
    }
    @Override
    protected void onResume(){
        super.onResume();
      //  new appTask().execute();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, new SensorListFragment())
                        .commit();
                break;
// Left unused because there's only two fragments, and the default case does settings.
//            case 1:
//                fragmentManager.beginTransaction()
//                        .replace(R.id.container, new CustomizeFragment())
//                        .commit();
//                break;
            default:
                try {
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, new SettingsFragment())
                            .commit();
                }
                catch (Exception e)
                {
                    Log.w("Exception!",e);
                }
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
            getMenuInflater().inflate(R.menu.menu_main, menu);
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
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }

        if (item.getItemId() == R.id.action_HR) {
            Log.w("here", "got here!");
            try {

                hrConsent = new HeartRateConsentListener() {
                    @Override
                    public void userAccepted(boolean b) {
                        Log.w("got here", "here is here");
                        if (b == true)
                        {
                            try {
                                hrListener = new BandHeartRateEventListener() {
                                    @Override
                                    public void onBandHeartRateChanged(final BandHeartRateEvent bandHeartRateEvent) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (bandHeartRateEvent != null)
                                                {
                                                    Integer heartRate = bandHeartRateEvent.getHeartRate();
                                                    HRValue.setText(heartRate.toString());
                                                }
                                            }
                                        });
                                    }
                                };
                                client.getSensorManager().registerHeartRateEventListener(hrListener);

                            } catch (Exception e) { Log.w("appfail",e);}
                        }
                    }
                };
            } catch (Exception e) { Log.w("failure!", e);}

            if(client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {

            } else {
                // user has not consented yet, request it
                client.getSensorManager().requestHeartRateConsent(this.getParent(), hrConsent);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Fragment contains the customization options for the band.
     */
    public static class SettingsFragment extends Fragment {
        RadioGroup SampleSelect;
        RadioButton SampleSelect16;
        RadioButton SampleSelect32;
        RadioButton SampleSelect128;
        RadioGroup TempSelect;
        RadioButton TempSelectC;
        RadioButton TempSelectF;

        public SettingsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
            SampleSelect = (RadioGroup) rootView.findViewById(R.id.SampleRateSelect);
            SampleSelect16 = (RadioButton) rootView.findViewById(R.id.sample16);
            SampleSelect32 = (RadioButton) rootView.findViewById(R.id.sample32);
            SampleSelect128 = (RadioButton) rootView.findViewById(R.id.sample128);
            TempSelect = (RadioGroup) rootView.findViewById(R.id.TempSelect);
            TempSelectC = (RadioButton) rootView.findViewById(R.id.FahrSelect);
            TempSelectF = (RadioButton) rootView.findViewById(R.id.CelsSelect);

            if (sRate == SampleRate.MS32)
                SampleSelect.check(R.id.sample32);
            else if (sRate == SampleRate.MS16)
                SampleSelect.check(R.id.sample16);
            else
                SampleSelect.check(R.id.sample128);

            TempSelectF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tempMode = TempMode.Celsius;
                }
            });

            TempSelectC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tempMode = TempMode.Fahrenheit;
                }
            });

            SampleSelect16.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sRate = SampleRate.MS16;
                    Log.w("Sample rate is now", sRate.toString());
                }
            });

            SampleSelect32.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sRate = SampleRate.MS32;
                    Log.w("Sample rate is now", sRate.toString());
                }
            });

            SampleSelect128.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sRate = SampleRate.MS128;
                    Log.w("Sample rate is now", sRate.toString());
                }
            });
            return rootView;
        }
    }

    /*
    public static class CustomizeFragment extends Fragment {
        public Button SendCustomization;
        public BandPersonalizationManager PersonalizationManager;
        public Bitmap toPost;
        public Button PickImage;
        public ImageView imageView;
        public CustomizeFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_customize, container, false);
            SendCustomization = (Button) rootView.findViewById(R.id.SendCustomization);
            PersonalizationManager = client.getPersonalizationManager();
            PickImage = (Button) rootView.findViewById(R.id.PickImage);
            imageView = (ImageView) rootView.findViewById(R.id.imageView);
            PickImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto,1);
                    Log.w("got here", "got here");
                }
            });
            SendCustomization.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        PersonalizationManager.setMeTileImage(toPost);
                    } catch (Exception e)
                    { Log.w("app",e);}
                }
            });
         return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent IntentReturned) {
            super.onActivityResult(requestCode, resultCode, IntentReturned);
            Integer n = Integer.valueOf(resultCode);
            Log.w("INFORMATION", n.toString());

            if (resultCode == -1) {
                Log.w("SUCCESS", "GOT HERE!");
                Uri img = IntentReturned.getData();
                try {
                    toPost = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), img);
                    imageView.setImageBitmap(toPost);
                    if (toPost.getHeight() == 102 && toPost.getWidth() == 310) {

                    } else {
                        Intent intent = new Intent("com.android.camera.action.CROP");
// this will open all images in the Galery
                        intent.setDataAndType(img, "image/*");
                        intent.putExtra("crop", "true");
                        intent.putExtra("outputX", 310);
                        intent.putExtra("outputY", 102);

                        intent.putExtra("return-data", true);
                        startActivityForResult(intent, 2);
                    }

                } catch (Exception e) {
                    Log.w("Error!", e);
                }
            }
            if (resultCode == 2) {
                Uri img = IntentReturned.getData();
                try {
                    toPost = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), img);
                    imageView.setImageBitmap(toPost);
                }
                catch (Exception e){
                        Log.w("Error!",e);
                }
            }

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }
    */

    /**
     * Fragment contains the sensor readouts.
     */
//    public static class SensorsFragment extends Fragment {
//
//        public class writeCSVTask extends AsyncTask<Void,Void,Void> {
//            @Override
//            protected Void doInBackground(Void... params) {
//                while (SaveData) {
//                    try {
//                            data.add(new String[]{GyroValue.getText().toString(), AccelValue.getText().toString(), SkinTempText.getText().toString(), pedCount.getText().toString(), UVValue.getText().toString()});
//                            Thread.sleep(1000);
//                            mWriter.writeAll(data);
//                            data.clear();
//                        }
//
//                    catch (InterruptedException e) {
//                        break;
//                    }
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void onProgressUpdate(Void... values) {
//
//            }
//
//            @Override
//            protected void onPreExecute()
//            {
//                data.add(new String[]{"Gyro Values", "Accelerometer Values","Skin Temperature", "Pedometer Count", "UV Level"});
//                mWriter.writeAll(data);
//                data.clear();
//            }
//
//            @Override
//            protected void onCancelled()
//            {
//                try {
//                    mWriter.close();
//                }
//                catch (Exception e)
//                {
//                    Log.w("mWriter", e);
//                }
//            }
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                     Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_sensors, container, false);
//            super.onCreate(savedInstanceState);
//            ConnStatus = (TextView) rootView.findViewById(R.id.ConnectionStatus);
//            SkinTempText = (TextView) rootView.findViewById(R.id.SkinTemp);
//            pedCount = (TextView) rootView.findViewById(R.id.PedCount);
//            totDistance = (TextView) rootView.findViewById(R.id.TotDistance);
//            UVValue = (TextView) rootView.findViewById(R.id.UVValue);
//            HRValue = (TextView) rootView.findViewById(R.id.HRValue);
//            AccelValue =(TextView) rootView.findViewById(R.id.AccelValue);
//            GyroValue = (TextView) rootView.findViewById(R.id.GyroValue);
//            DCButton = (Button) rootView.findViewById(R.id.DCbutton);
//            PedSpeed = (TextView) rootView.findViewById(R.id.PedSpeed);
//            PedMode = (TextView) rootView.findViewById(R.id.PedMode);
//
//                DCButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        if (devices.length > 0){
//                            if (SaveData) {
//                                DCButton.setText("Start Data Collection");
//                                SaveData = false;
//                                csvTask.cancel(true);
//                            } else {
//                                SaveData = true;
//                                DCButton.setText("Stop Data Collection");
//                                csvTask = new writeCSVTask().execute();
//                                Toast.makeText(getActivity(), "Outputting to /sdcard/BandData.csv", Toast.LENGTH_LONG).show();
//                            }
//                        } else
//                        {
//                           // DCButton.setText("Connect a Microsoft Band to Collect Data.");
//                            Toast.makeText(getActivity(), "Connect a Band to collect data.", Toast.LENGTH_LONG).show();
//                        }
//                    }
//            });
//            return rootView;
//        }
//
//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            // Handle action bar item clicks here. The action bar will
//            // automatically handle clicks on the Home/Up button, so long
//            // as you specify a parent activity in AndroidManifest.xml.
//            int id = item.getItemId();
//
//            //noinspection SimplifiableIfStatement
//            if (id == R.id.action_settings) {
//                return true;
//            }
//
//            return super.onOptionsItemSelected(item);
//        }
//
//        @Override
//        public void onResume(){
//            super.onResume();
//            try {
//                if (devices[0] != null){
//                    ConnStatus.setText("Connected!");
//                }
//
//                client.getSensorManager().registerSkinTemperatureEventListener(tempListener);
//                client.getSensorManager().registerPedometerEventListener(pedListener);
//                client.getSensorManager().registerDistanceEventListener(distListener);
//                client.getSensorManager().registerUVEventListener(uvListener);
//
//                if (sRate == null)
//                    sRate = SampleRate.MS128;
//                client.getSensorManager().registerAccelerometerEventListener(accelListener, sRate);
//                client.getSensorManager().registerGyroscopeEventListener(gyroListener, sRate);
//                client.getSensorManager().registerHeartRateEventListener(hrListener);
//            }
//            catch (Exception e)
//            {
//                Log.w("Error!!!", e);
//            }
//        }
//
//        @Override
//        public void onPause() {
//            super.onPause();
//
//            if (client != null) {
//                try {
//
//                    client.getSensorManager().unregisterAllListeners();
//                    SaveData = false;
//
//                } catch (BandIOException e) {
//                    Log.w("ERROR UNREGISTERING", e);
//                }
//            }
//        }
//        /**
//         *
//         *  Checks for band client connection
//         * @return is the band client connected?
//         * @throws InterruptedException
//         * @throws BandException
//         */
//    }
//    private boolean getConnectedBandClient() throws InterruptedException, BandException {
//        if (client == null) {
//           devices = BandClientManager.getInstance().getPairedBands();
//            if (devices.length == 0) {
//                Log.w("ERROR","Band isn't paired with your phone...");
//                return false;
//            }
//            client = BandClientManager.getInstance().create(this, devices[0]);
//        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
//            return true;
//        }
//        Log.w("CONNECTING", "Band is connecting...");
//        return ConnectionState.CONNECTED == client.connect().await();
//    }
//
//    public class appTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                if (getConnectedBandClient()) {
//                    Log.w("CONNECTED", "SYSTEM IS CONNECTED");
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ConnStatus.setText("Connected!");
//                        }
//                    });
//                    final BandSensorManager sensorManager = client.getSensorManager();
//
//                    tempListener = new BandSkinTemperatureEventListener() {
//                        @Override
//                        public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent bandSkinTemperatureEvent) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (bandSkinTemperatureEvent != null) {
//                                        Float f = new Float(bandSkinTemperatureEvent.getTemperature());
//                                        if (tempMode == TempMode.Fahrenheit)
//                                        {
//                                            f = (f * (9/5)) + 32;
//                                            SkinTempText.setText((f.toString().substring(0,4) + " F"));
//                                        }
//                                        else
//                                        {
//                                            SkinTempText.setText((f.toString().substring(0,4) + " C"));
//                                        }
//                                    }
//                                }
//                            });
//                        }
//                    };
//                }
//
//                pedListener = new BandPedometerEventListener() {
//                    @Override
//                    public void onBandPedometerChanged(final BandPedometerEvent bandPedometerEvent) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (bandPedometerEvent != null) {
//                                    Long l = bandPedometerEvent.getTotalSteps();
//                                    pedCount.setText(l.toString());
//
//                                }
//                            }
//                        });
//                    }
//                };
//
//                distListener = new BandDistanceEventListener() {
//                    @Override
//                    public void onBandDistanceChanged(final BandDistanceEvent bandDistanceEvent) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (bandDistanceEvent != null) {
//                                    Long l = bandDistanceEvent.getTotalDistance();
//                                    totDistance.setText(l.toString() + " cm");
//                                    Float f = bandDistanceEvent.getSpeed();
//                                    PedSpeed.setText(f.toString() + " cm/s");
//                                    MotionType mtype =bandDistanceEvent.getMotionType();
//                                    PedMode.setText(mtype.toString());
//                                }
//                            }
//                        });
//                    }
//                };
//
//                accelListener = new BandAccelerometerEventListener() {
//                    @Override
//                    public void onBandAccelerometerChanged(final BandAccelerometerEvent bandAccelerometerEvent) {
//                        runOnUiThread(
//                                new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (bandAccelerometerEvent != null) {
//                                            AccelValue.setText(String.format(" X = %.3f \n Y = %.3f\n Z = %.3f", bandAccelerometerEvent.getAccelerationX(),
//                                                    bandAccelerometerEvent.getAccelerationY(), bandAccelerometerEvent.getAccelerationZ()));
//                                        }
//                                    }
//                                });
//                    }
//                };
//
//                gyroListener = new BandGyroscopeEventListener() {
//                    @Override
//                    public void onBandGyroscopeChanged(final BandGyroscopeEvent bandGyroscopeEvent) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (bandGyroscopeEvent != null) {
//                                    GyroValue.setText(String.format(" X = %.3f \n Y = %.3f\n Z = %.3f", bandGyroscopeEvent.getAngularVelocityX(),
//                                            bandGyroscopeEvent.getAngularVelocityY(), bandGyroscopeEvent.getAngularVelocityZ()));
//                                }
//                            }
//                        });
//                    }
//                };
//
//                uvListener = new BandUVEventListener() {
//
//                    @Override
//                    public void onBandUVChanged(final BandUVEvent bandUVEvent) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (bandUVEvent != null) {
//                                    UVIndexLevel l = bandUVEvent.getUVIndexLevel();
//                                    UVValue.setText(l.toString());
//                                }
//                            }
//                        });
//                    }
//                };
//
//                client.getSensorManager().registerSkinTemperatureEventListener(tempListener);
//                client.getSensorManager().registerPedometerEventListener(pedListener);
//                client.getSensorManager().registerDistanceEventListener(distListener);
//                client.getSensorManager().registerUVEventListener(uvListener);
////          client.getSensorManager()sensorManager.registerHeartRateEventListener(hrListener);
//                if (sRate == null)
//                { sRate = SampleRate.MS128;}
//                client.getSensorManager().registerAccelerometerEventListener(accelListener, sRate);
//                client.getSensorManager().registerGyroscopeEventListener(gyroListener, sRate);
//            }
//        catch (Exception e){
//                Log.w("Not connected.", "Please make sure bluetooth is on and the band is in range.\n");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {ConnStatus.setText("Not Connected");
//                    }
//                });
//        }
//            return null;
//        }
//    }
}
