/*=========================================================================
 *
 *  PROJECT:  SlimRoms
 *            Team Slimroms (http://www.slimroms.net)
 *
 *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
 *            All rights reserved
 *
 *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
 *
 *  AUTHORS:     fronti90, mnazim, tchaari, kufikugel
 *  DESCRIPTION: SlimOTA keeps our rom up to date
 *
 *=========================================================================
 */

package com.teslaos.ota;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.teslaos.ota.updater.UpdateChecker;
import com.teslaos.ota.updater.UpdateListener;
import com.teslaos.ota.settings.Settings;
import com.commonsware.cwac.wakeful.WakefulIntentService;

public class TeslaOSOTA extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final int ID_DEVICE_NAME = R.id.deviceName;
    private static final int ID_DEVICE_CODE_NAME = R.id.deviceCodename;
    private static final int ID_CURRENT_VERSION = R.id.curVer;
    private static final int ID_CURRENT_FILE = R.id.curFile;
    private static final int ID_UPDATE_FILE = R.id.upToDate;

    private static final String LAST_INTERVAL = "lastInterval";

    private TextView mDeviceOut;
    private TextView mCodenameOut;
    private TextView mCurVerOut;
    private TextView mCurFileOut;
    private TextView mUpdateFile;

    private String mStrDevice;
    private String mStrCodename;
    private String mStrCurVer;
    private String mStrCurFile;
    private String mStrUpToDate;

    SharedPreferences prefs;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.teslaos_ota, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDeviceOut = (TextView) getView().findViewById(ID_DEVICE_NAME);
        mCodenameOut = (TextView) getView().findViewById(ID_DEVICE_CODE_NAME);
        mCurVerOut = (TextView) getView().findViewById(ID_CURRENT_VERSION);
        mCurFileOut = (TextView) getView().findViewById(ID_CURRENT_FILE);
        mUpdateFile = (TextView) getView().findViewById(ID_UPDATE_FILE);
        View actionButton = getView().findViewById(R.id.floating_action_button);
        actionButton.setOnClickListener(mComposeClickHandler);

        prefs = this.getActivity().getSharedPreferences("UpdateChecker", 0);
        prefs.registerOnSharedPreferenceChangeListener(this);

        if (UpdateChecker.connectivityAvailable(getActivity())) {
           doTheUpdateCheck();
        } else {
           Toast.makeText(getView().getContext(), R.string.toast_no_data_text, Toast.LENGTH_LONG).show();
        }

        setDeviceInfoContainer();
        addShortCutFragment();

        setInitialUpdateInterval();
    }

    private View.OnClickListener mComposeClickHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (UpdateChecker.connectivityAvailable(getActivity())) {
                    doTheUpdateCheck();
                }
                setDeviceInfoContainer();
                addShortCutFragment();
            }
        };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("Filename")) {
            setDeviceInfoContainer();
            addShortCutFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (prefs != null) prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs = this.getActivity().getSharedPreferences("UpdateChecker", 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    private void doTheUpdateCheck(){
        UpdateChecker otaChecker = new UpdateChecker();
        otaChecker.execute(getActivity());
    }

    private void setDeviceInfoContainer() {
        try {
            FileInputStream fstream = new FileInputStream("/system/build.prop");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] line = strLine.split("=");
                if (line[0].equalsIgnoreCase("ro.product.device")) {
                    mStrCodename = line[1];
                } else if (line[0].equalsIgnoreCase("tesla.ota.version")) {
                    mStrCurVer = line[1];
                } else if (line[0].equalsIgnoreCase("ro.product.model")) {
                    mStrDevice = line[1];
                } else if (line[0].equalsIgnoreCase("ro.modversion")) {
                    mStrCurFile = line[1];
                }
            }
            in.close();
        } catch (Exception e) {
            Toast.makeText(getView().getContext(), getString(R.string.system_prop_error),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        if(mStrCodename == null){ mStrCodename = Build.DEVICE; }
        if(mStrDevice == null){ mStrDevice = Build.MODEL; }

        mDeviceOut.setText(getString(R.string.device_name_title) + " " + mStrDevice);
        mCodenameOut.setText(getString(R.string.codename_title) + " " + mStrCodename);
        mCurVerOut.setText(getString(R.string.version_title) + " " + mStrCurVer);
        mCurFileOut.setText(getString(R.string.file_name_title) + " " + " " + mStrCurFile);

        SharedPreferences prefs = this.getActivity().getSharedPreferences("UpdateChecker", 0);
        String updateFile = prefs.getString("Filename", "");

        mUpdateFile.setTextColor(Color.RED);

        if (!UpdateChecker.connectivityAvailable(getActivity())) {
            mStrUpToDate = getString(R.string.no_data_title);
        } else if (updateFile.equals("")) {
            mStrUpToDate = getString(R.string.error_reading_title);
        } else if (updateFile.compareToIgnoreCase(mStrCurVer)<=0) {
            mStrUpToDate = getString(R.string.up_to_date_title);
            mUpdateFile.setTextColor(0xff009688);
        } else {
            mStrUpToDate = updateFile;
        }

        mUpdateFile.setText(" " + mStrUpToDate);
    }

    private void addShortCutFragment() {
        FragmentManager fragmentManager = this.getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TeslaOSLinks Links = new TeslaOSLinks();
        fragmentTransaction.replace(R.id.linksFragment, Links);
        fragmentTransaction.commit();
    }

    private void setInitialUpdateInterval() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences(LAST_INTERVAL, 0);
        long value = prefs.getLong(LAST_INTERVAL,0);
        //set interval to 12h if user starts first time OTA and it was not installed by system before
        //yes ask lazy tarak....he has this case ;)
        if (value == 0) {
            UpdateListener.interval = AlarmManager.INTERVAL_HALF_DAY;
            prefs.edit().putLong(LAST_INTERVAL, UpdateListener.interval).apply();
            WakefulIntentService.scheduleAlarms(new UpdateListener(), getActivity(), false);
        }
    }
}
