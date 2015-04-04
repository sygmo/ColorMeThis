package aasgmkrm.colormethis;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Kevin on 4/2/2015.
 */
public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("ttt_prefs");
        addPreferencesFromResource(R.xml.preferences);
    }
}
