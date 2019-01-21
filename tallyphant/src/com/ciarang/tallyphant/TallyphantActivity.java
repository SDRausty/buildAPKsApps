/*
 * Copyright (C) 2012  Ciaran Gultnieks, ciaran@ciarang.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.ciarang.tallyphant;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TallyphantActivity extends ListActivity implements OnInitListener {

    private static final int REQUEST_EDITITEM = 0;
    private static final int REQUEST_PREFS = 1;

    private class ItemListAdapter extends BaseAdapter {

        private List<DB.Item> items = new ArrayList<DB.Item>();
        private Context mContext;

        public ItemListAdapter(Context context) {
            mContext = context;
        }

        public void addItem(DB.Item item) {
            items.add(item);
        }

        public void clear() {
            items.clear();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.itemlistitem, null);
            }
            DB.Item item = items.get(position);

            final TextView tv = (TextView) v.findViewById(R.id.text);
            tv.setText(item.getFormatted());
            tv.setTag(item);
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DB.Item item = (DB.Item) v.getTag();
                    Intent call = new Intent(TallyphantActivity.this,
                            ItemEdit.class);
                    call.putExtra("itemname", item.name);
                    startActivityForResult(call, REQUEST_EDITITEM);

                }
            });

            Button minus = (Button) v.findViewById(R.id.minus);
            if (item.buttonstyle == DB.ButtonStyle.Plus) {
                minus.setVisibility(View.GONE);
            } else {
                minus.setVisibility(View.VISIBLE);
                minus.setTag(tv);
                minus.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView tv = (TextView) v.getTag();
                        DB.Item item = (DB.Item) tv.getTag();
                        item.count = db.updateCount(item.name, -1);
                        tv.setText(item.getFormatted());
                        notifyChange(item, false);
                    }
                });
                minus.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        TextView tv = (TextView) v.getTag();
                        DB.Item item = (DB.Item) tv.getTag();
                        addAny(item.name, false);
                        return true;
                    }
                });
            }

            Button plus = (Button) v.findViewById(R.id.plus);
            plus.setTag(tv);
            plus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) v.getTag();
                    DB.Item item = (DB.Item) tv.getTag();
                    item.count = db.updateCount(item.name, 1);
                    tv.setText(item.getFormatted());
                    notifyChange(item, false);
                }
            });
            plus.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    TextView tv = (TextView) v.getTag();
                    DB.Item item = (DB.Item) tv.getTag();
                    addAny(item.name, true);
                    return true;
                }
            });

            return v;
        }

        private void addAny(final String itemname, final boolean add) {
            AlertDialog.Builder alert = new AlertDialog.Builder(
                    TallyphantActivity.this);
            alert.setTitle(R.string.any_title);
            if (add)
                alert.setTitle(R.string.anyadd);
            else
                alert.setTitle(R.string.anysubtract);

            final EditText value = new EditText(TallyphantActivity.this);
            value.setInputType(InputType.TYPE_CLASS_NUMBER);
            alert.setView(value);

            alert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            Integer amount = Integer.parseInt(value.getText()
                                    .toString());
                            if (!add)
                                amount = -amount;
                            DB.Item item = db.getItem(itemname);
                            item.count = db.updateCount(item.name, amount);
                            notifyChange(item, false);
                            updateItemList();
                        }
                    });
            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            // Do nothing!
                        }
                    });
            alert.show();
        }

    }

    private DB db = null;
    private ItemListAdapter mItemList;
    private Vibrator mVibrator;
    private TextToSpeech mTTS = null;
    private boolean mSpeechReady = false;
    private DatagramSocket mUDPSocket = null;

    // Locally cached preferences - see updatePreferences()
    private boolean mPrefVibrate;
    private boolean mPrefSound;
    private boolean mPrefSpeak;
    private boolean mPrefUDP;
    private String mPrefUDPHost;
    private int mPrefUDPPort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        db = new DB(this);

        setContentView(R.layout.main);

        // Populate the item list...
        mItemList = new ItemListAdapter(this);
        updateItemList();
        setListAdapter(mItemList);

        readPrefs();

    }

    @Override
    protected void onStop() {
        db.close();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.shutdown();
            mTTS = null;
        }
        if (mUDPSocket != null) {
            mUDPSocket.close();
            mUDPSocket = null;
        }
        super.onDestroy();
    }

    private void updateItemList() {
        Vector<DB.Item> items = db.getItems();
        mItemList.clear();
        for (DB.Item item : items) {
            mItemList.addItem(item);
        }
        mItemList.notifyDataSetChanged();
    }

    private static final int NEW_ITEM = Menu.FIRST;
    private static final int RESET_ALL = Menu.FIRST + 1;
    private static final int SHARE = Menu.FIRST + 2;
    private static final int PREFERENCES = Menu.FIRST + 3;
    private static final int ABOUT = Menu.FIRST + 4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, NEW_ITEM, 1, R.string.menu_newitem).setIcon(
                android.R.drawable.ic_menu_add);
        menu.add(Menu.NONE, RESET_ALL, 2, R.string.menu_resetall).setIcon(
                android.R.drawable.ic_menu_revert);
        menu.add(Menu.NONE, SHARE, 3, R.string.menu_share).setIcon(
                android.R.drawable.ic_menu_share);
        menu.add(Menu.NONE, PREFERENCES, 4, R.string.menu_preferences).setIcon(
                android.R.drawable.ic_menu_preferences);
        menu.add(Menu.NONE, ABOUT, 5, R.string.menu_about).setIcon(
                android.R.drawable.ic_menu_help);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case NEW_ITEM:
            Intent call = new Intent(TallyphantActivity.this, ItemEdit.class);
            startActivityForResult(call, REQUEST_EDITITEM);
            return true;

        case RESET_ALL:
            db.resetAll();
            remoteNotifyAll();
            updateItemList();
            return true;

        case SHARE:
            Intent i = new Intent(android.content.Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, R.string.share_subject);
            Vector<DB.Item> items = db.getItems();
            String msg = "";
            for (DB.Item titem : items)
                msg += titem.getFormatted() + "\n";
            i.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(i,
                    getString(R.string.share_title)));
            return true;

        case PREFERENCES:
            Intent prefs = new Intent(getBaseContext(), Preferences.class);
            startActivityForResult(prefs, REQUEST_PREFS);
            return true;

        case ABOUT:
            LayoutInflater li = LayoutInflater.from(this);
            View view = li.inflate(R.layout.about, null);

            // Fill in the version...
            TextView tv = (TextView) view.findViewById(R.id.version);
            PackageManager pm = getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo(getApplicationContext()
                        .getPackageName(), 0);
                tv.setText(pi.versionName);
            } catch (Exception e) {
            }

            Builder p = new AlertDialog.Builder(this).setView(view);
            final AlertDialog alrt = p.create();
            alrt.setIcon(R.drawable.ic_launcher);
            alrt.setTitle(getString(R.string.about_title));
            alrt.setButton(AlertDialog.BUTTON_NEUTRAL,
                    getString(R.string.about_website),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            Uri uri = Uri
                                    .parse("http://projects.ciarang.com/p/tallyphant");
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    });
            alrt.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                        }
                    });
            alrt.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void remoteNotifyAll() {
        if (mPrefUDP) {
            Vector<DB.Item> items = db.getItems();
            String msg = "all\n";
            for (DB.Item item : items)
                msg += Integer.toString(item.count) + "\t" + item.name + "\t"
                        + item.pname + "\n";
            udpSend(msg);
        }
    }

    // Notify a change.
    // 'item' - the item that changed
    // 'remoteonly' - true to only notify remotes
    private void notifyChange(DB.Item item, boolean remoteonly) {

        if (!remoteonly) {
            if (mPrefVibrate)
                mVibrator.vibrate(100);

            if (mPrefSound) {
                MediaPlayer mp = MediaPlayer.create(this, R.raw.beep);
                mp.start();
                mp.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
            }

            if (mPrefSpeak && mTTS != null && mSpeechReady) {
                mTTS.speak(item.getFormatted(), 0, null);
            }
        }

        if (mPrefUDP) {
            String msg = "change " + Integer.toString(item.count) + " "
                    + item.name;
            udpSend(msg);
        }

    }

    private void udpSend(String msg) {
        try {
            InetAddress dest = InetAddress.getByName(mPrefUDPHost);
            byte[] msgb = msg.getBytes("UTF-8");
            DatagramPacket p = new DatagramPacket(msgb, msgb.length, dest,
                    mPrefUDPPort);
            mUDPSocket.send(p);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_EDITITEM:
            if (resultCode == RESULT_OK) {
                remoteNotifyAll();
                updateItemList();
                getWindow()
                        .setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        case REQUEST_PREFS:
            readPrefs();
            break;
        }
    }

    // Read frequently accessed preferences into local members. Also gets
    // objects which are only needed based on prefs.
    private void readPrefs() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        mPrefVibrate = prefs.getBoolean("vibrate", false);
        mPrefSound = prefs.getBoolean("playSound", false);
        mPrefSpeak = prefs.getBoolean("speak", false);
        mPrefUDP = prefs.getBoolean("udp", false);
        if (mPrefUDP) {
            mPrefUDPHost = prefs.getString("udpHost", "");
            mPrefUDPPort = Integer.valueOf(prefs.getString("udpPort", "0"));
        }

        if (mPrefVibrate && mVibrator == null)
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (mPrefSpeak && mTTS == null) {
            mTTS = new TextToSpeech(this, this);
            Toast.makeText(this, R.string.speechInit, Toast.LENGTH_SHORT)
                    .show();
            mSpeechReady = false;
        } else if (!mPrefSpeak && mTTS != null) {
            mSpeechReady = false;
            mTTS.shutdown();
            mTTS = null;
        }

        if (mPrefUDP && mUDPSocket == null) {
            try {
                mUDPSocket = new DatagramSocket();
                remoteNotifyAll();
            } catch (SocketException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (!mPrefUDP && mUDPSocket != null) {
            mUDPSocket.close();
            mUDPSocket = null;
        }

    }

    // Called when speech engine is ready.
    @Override
    public void onInit(int arg0) {
        Toast.makeText(this, R.string.speechReady, Toast.LENGTH_SHORT).show();
        mSpeechReady = true;
    }

}