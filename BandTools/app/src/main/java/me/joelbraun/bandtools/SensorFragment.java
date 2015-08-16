package me.joelbraun.bandtools;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.band.BandClient;
import com.microsoft.band.sensors.SampleRate;

import me.joelbraun.bandtools.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SensorFragment extends Fragment implements AbsListView.OnItemClickListener {
    TempMode tempMode;
    SampleRate sampleRate;
    BandClient Client;




    // TODO: Rename and change types of parameters
    private String tMode;
    private String sRate;
    private static String client;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static SensorFragment newInstance(SampleRate sRate, TempMode tMode, BandClient client) {
        SensorFragment fragment = new SensorFragment();
        Bundle args = new Bundle();
        args.putSerializable("SampleRate", new Gson().toJson(sRate));
        args.putSerializable("TempMode", new Gson().toJson(tMode));
        args.putSerializable("Client", new Gson().toJson(client));
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SensorFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            tMode = getArguments().getString("SampleRate");
            sRate = getArguments().getString("TempMode");
            client = getArguments().getString("Client");
        }

        sampleRate = new Gson().fromJson(tMode, SampleRate.class);
        tempMode = new Gson().fromJson(sRate, TempMode.class);
        Client = new Gson().fromJson(client, BandClient.class);


        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item, R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Log.w("Verify", "Got here!");
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String SensorID);
    }


    /* This goes in here, but needs to be drastically improved.
    public class appTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    Log.w("CONNECTED", "SYSTEM IS CONNECTED");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ConnStatus.setText("Connected!");
                        }
                    });
                    final BandSensorManager sensorManager = client.getSensorManager();

                    tempListener = new BandSkinTemperatureEventListener() {
                        @Override
                        public void onBandSkinTemperatureChanged(final BandSkinTemperatureEvent bandSkinTemperatureEvent) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (bandSkinTemperatureEvent != null) {
                                        Float f = new Float(bandSkinTemperatureEvent.getTemperature());
                                        if (tempMode == TempMode.Fahrenheit)
                                        {
                                            f = (f * (9/5)) + 32;
                                            SkinTempText.setText((f.toString().substring(0,4) + " F"));
                                        }
                                        else
                                        {
                                            SkinTempText.setText((f.toString().substring(0,4) + " C"));
                                        }
                                    }
                                }
                            });
                        }
                    };
                }

                pedListener = new BandPedometerEventListener() {
                    @Override
                    public void onBandPedometerChanged(final BandPedometerEvent bandPedometerEvent) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bandPedometerEvent != null) {
                                    Long l = bandPedometerEvent.getTotalSteps();
                                    pedCount.setText(l.toString());

                                }
                            }
                        });
                    }
                };

                distListener = new BandDistanceEventListener() {
                    @Override
                    public void onBandDistanceChanged(final BandDistanceEvent bandDistanceEvent) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bandDistanceEvent != null) {
                                    Long l = bandDistanceEvent.getTotalDistance();
                                    totDistance.setText(l.toString() + " cm");
                                    Float f = bandDistanceEvent.getSpeed();
                                    PedSpeed.setText(f.toString() + " cm/s");
                                    MotionType mtype =bandDistanceEvent.getMotionType();
                                    PedMode.setText(mtype.toString());
                                }
                            }
                        });
                    }
                };

                accelListener = new BandAccelerometerEventListener() {
                    @Override
                    public void onBandAccelerometerChanged(final BandAccelerometerEvent bandAccelerometerEvent) {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bandAccelerometerEvent != null) {
                                            AccelValue.setText(String.format(" X = %.3f \n Y = %.3f\n Z = %.3f", bandAccelerometerEvent.getAccelerationX(),
                                                    bandAccelerometerEvent.getAccelerationY(), bandAccelerometerEvent.getAccelerationZ()));
                                        }
                                    }
                                });
                    }
                };

                gyroListener = new BandGyroscopeEventListener() {
                    @Override
                    public void onBandGyroscopeChanged(final BandGyroscopeEvent bandGyroscopeEvent) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bandGyroscopeEvent != null) {
                                    GyroValue.setText(String.format(" X = %.3f \n Y = %.3f\n Z = %.3f", bandGyroscopeEvent.getAngularVelocityX(),
                                            bandGyroscopeEvent.getAngularVelocityY(), bandGyroscopeEvent.getAngularVelocityZ()));
                                }
                            }
                        });
                    }
                };

                uvListener = new BandUVEventListener() {

                    @Override
                    public void onBandUVChanged(final BandUVEvent bandUVEvent) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (bandUVEvent != null) {
                                    UVIndexLevel l = bandUVEvent.getUVIndexLevel();
                                    UVValue.setText(l.toString());
                                }
                            }
                        });
                    }
                };

                client.getSensorManager().registerSkinTemperatureEventListener(tempListener);
                client.getSensorManager().registerPedometerEventListener(pedListener);
                client.getSensorManager().registerDistanceEventListener(distListener);
                client.getSensorManager().registerUVEventListener(uvListener);
          client.getSensorManager()sensorManager.registerHeartRateEventListener(hrListener);
                if (sRate == null)
                { sRate = SampleRate.MS128;}
                client.getSensorManager().registerAccelerometerEventListener(accelListener, sRate);
                client.getSensorManager().registerGyroscopeEventListener(gyroListener, sRate);
            }
        catch (Exception e){
                Log.w("Not connected.", "Please make sure bluetooth is on and the band is in range.\n");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {ConnStatus.setText("Not Connected");
                    }
                });
        }
            return null;
        }
    }
     */

}
