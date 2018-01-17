/*
    Alarm Button
    Aubort Jean-Baptiste <aubort.jeanbaptiste@gmail.com> 2009 

    AlarmButton is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.lamatricexiste.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class Alarm extends Activity {
    private MediaPlayer mp;
    private Float mp_vol;
    private String mp_rng;
    private SharedPreferences prefs;
    private AnimationDrawable anim = null;
    private final String DEFAULT_RNG = "rng_default";
    private final String DEFAULT_VOL = "0.5";
    private LayoutInflater inflater = null;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPrefs,
                String key) {
            if (key.equals("volume")) {
                setup();
                if (mp.isPlaying()) {
                    mp.setVolume(mp_vol, mp_vol);
                } else {
                    mp.release();
                    loadClip();
                }
            } else if (key.equals("ring")) {
                setup();
                if (!mp.isPlaying()) {
                    mp.release();
                    loadClip();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View btn = findViewById(R.id.start_alarm);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StartAlarm();
            }
        });
        btn.setBackgroundResource(R.drawable.animation);
        anim = (AnimationDrawable) btn.getBackground();

        setup();
        loadClip();

        prefs.registerOnSharedPreferenceChangeListener(prefListener);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(getApplication()).inflate(R.menu.options, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, Prefs.class));
            return true;
        } else if (item.getItemId() == R.id.credits) {
            View alert_view = inflater.inflate(R.layout.credits, null);
            new AlertDialog.Builder(this).setTitle(R.string.credits_title)
                    .setView(alert_view).setNeutralButton("Close",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dlg,
                                        int sumthin) {
                                }
                            }).show();
            return true;
        }
        return (super.onOptionsItemSelected(item));
    }

    private void StartAlarm() {
        if (mp.isPlaying()) {
            stop();
        } else {
            play();
        }
    }

    private void play() {
        anim.start();
        mp.start();
    }

    private void stop() {
        anim.stop();
        anim.selectDrawable(0);
        mp.stop();
        mp.release();
        loadClip();
    }

    private void setup() {
        try {
            mp_vol = Float.parseFloat(prefs.getString("volume", DEFAULT_VOL));
            mp_rng = prefs.getString("ring", DEFAULT_RNG);
        } catch (ClassCastException e) {
        }
    }

    private void loadClip() {
        try {
            mp = MediaPlayer.create(this, getResources().getIdentifier(mp_rng,
                    "raw", this.getPackageName()));
            mp.setVolume(mp_vol, mp_vol);
            mp.setLooping(true);
        } catch (Throwable t) {
        }
    }
}
