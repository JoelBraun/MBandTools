package me.joelbraun.bandtools;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.SampleRate;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener{

    RadioGroup SampleSelect;
    RadioButton SampleSelect16;
    RadioButton SampleSelect32;
    RadioButton SampleSelect128;
    RadioGroup TempSelect;
    RadioButton TempSelectC;
    RadioButton TempSelectF;

    SampleRate sampleRate;
    DCSpeed DCspeed;
    TempMode tempMode;

    private String sRate;
    private String tMode;


    private OnFragmentInteractionListener mListener;

    public static SettingsFragment newInstance(SampleRate sRate, TempMode tMode) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putSerializable("SampleRate", new Gson().toJson(sRate));
        args.putString("TempMode", new Gson().toJson(tMode));
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            sRate = getArguments().getString("SampleRate");
            tMode = getArguments().getString("TempMode");
        }

        sampleRate = new Gson().fromJson(sRate, SampleRate.class);
        tempMode = new Gson().fromJson(tMode, TempMode.class);
        Log.w("test", sampleRate.toString());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        SampleSelect = (RadioGroup) view.findViewById(R.id.SampleRateSelect);
        SampleSelect16 = (RadioButton) view.findViewById(R.id.sample16);
        SampleSelect32 = (RadioButton) view.findViewById(R.id.sample32);
        SampleSelect128 = (RadioButton) view.findViewById(R.id.sample128);
        TempSelect = (RadioGroup) view.findViewById(R.id.TempSelect);
        TempSelectC = (RadioButton) view.findViewById(R.id.FahrSelect);
        TempSelectF = (RadioButton) view.findViewById(R.id.CelsSelect);

        //Select the existing sample rate
        switch (sampleRate) {
            case MS16:
                SampleSelect.check(R.id.sample16);
                break;
            case MS32:
                SampleSelect.check(R.id.sample32);
                break;
            default:
                SampleSelect.check(R.id.sample128);
        }

        if (tempMode == TempMode.Fahrenheit)
            TempSelect.check(R.id.FahrSelect);
        else
            TempSelect.check(R.id.CelsSelect);

        mListener.onFragmentInteraction(sampleRate, DCspeed, tempMode);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            Log.w("masdf","asdf");
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
    public void onClick(View view)
    {
        mListener.onFragmentInteraction(sampleRate, DCspeed, tempMode);
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
        void onFragmentInteraction(SampleRate sRate, DCSpeed dCspeed, TempMode tMode);
    }

}
