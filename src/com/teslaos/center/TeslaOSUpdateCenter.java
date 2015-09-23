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
 *  DESCRIPTION: OTACenter: manage your ROM
 *
 *=========================================================================
 */
package com.teslaos.center;

import java.util.Locale;

import com.teslaos.ota.TeslaOSOTA;
import com.teslaos.ota.R;
import com.teslaos.ota.settings.Settings;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TeslaOSUpdateCenter extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new TeslaOSOTA()).commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.teslaos_center, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int resId = item.getItemId();
        if (resId == android.R.id.home) {
            finish();
        } else if (resId == R.id.btn_setting) {
            Intent intentSettings = new Intent(this, Settings.class);
            startActivity(intentSettings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
