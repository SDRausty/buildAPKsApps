/****************************************************************************
 * Copyright 2016 kraigs.android@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ****************************************************************************/

package com.angrydoughnuts.android.alarmclock;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.MediaColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.io.IOException;

public class MediaPicker extends DialogFragment {
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    super.onCreateDialog(savedInstanceState);

    if (savedInstanceState != null) {
      uri = savedInstanceState.getParcelable("uri");
      title = savedInstanceState.getString("title");
      tab = savedInstanceState.getInt("tab");
    }

    final boolean has_external = PackageManager.PERMISSION_GRANTED ==
      getContext().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                                   Process.myPid(), Process.myUid());

    final TabHost t = (TabHost)(View.inflate(
        getContext(), R.layout.media_picker, null));
    t.setup();

    if (has_external) {
      t.addTab(
          t.newTabSpec("artists").setIndicator(getString(R.string.artists))
          .setContent(
              new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) { return newFlip(); }
              }));
      t.addTab(
          t.newTabSpec("external").setIndicator(getString(R.string.songs))
          .setContent(
              new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                  return newList(Audio.Media.EXTERNAL_CONTENT_URI);
                }
              }));
    }
    t.addTab(
        t.newTabSpec("internal").setIndicator(getString(R.string.internal))
        .setContent(
            new TabHost.TabContentFactory() {
              @Override
              public View createTabContent(String tag) {
                return newList(
                    Audio.Media.INTERNAL_CONTENT_URI, new ExtraEntry[] {
                      new ExtraEntry(Settings.System.DEFAULT_NOTIFICATION_URI,
                                     getString(R.string.system_default)) });
              }
            }));
    t.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String id) {
          tab = t.getCurrentTab();
        }
      });
    t.setCurrentTab(tab);

    if (title != null)
      ((TextView)t.findViewById(R.id.selected))
        .setText(getString(R.string.selected) + title);

    if (player == null)
      player = new MediaPlayer();

    return new AlertDialog.Builder(getContext())
      .setTitle(R.string.alarm_tone)
      .setView(t)
      .setNegativeButton(R.string.cancel, null)
      .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (listener != null && uri != null && title != null)
              listener.onMediaPick(uri, title);
          }
        }).create();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (uri != null) outState.putParcelable("uri", uri);
    if (title != null) outState.putString("title", title);
    outState.putInt("tab", tab);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (player.isPlaying())
      player.stop();
    player.reset();
    player.release();
    player = null;
  }

  private AdapterView.OnItemClickListener newListener() {
    return new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View v, int x, long id) {
        TextView t = (TextView)v;
        uri = (Uri)t.getTag();
        title = t.getText().toString();
        ((TextView)getDialog().findViewById(R.id.selected))
          .setText(getString(R.string.selected) + title);

        player.reset();
        try {
          player.setDataSource(getContext(), uri);
          player.prepare();
        } catch (IOException e) {
          Log.e(TAG, "Failed to set data " + e);
        }
        player.start();
      }
    };
  }

  private View newFlip() {
    final ViewAnimator flip = new ViewAnimator(getContext());
    final ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);

    final AdapterView.OnItemClickListener song_selected = newListener();
    final AdapterView.OnItemClickListener album_selected =
      new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int x, long id) {
          flip.addView(
              newList(new MediaQuery(id), flip, song_selected), -1, layout);
          flip.showNext();
        }
      };
    final AdapterView.OnItemClickListener artist_selected =
      new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int x, long id) {
          flip.addView(
              newList(new AlbumsQuery(id), flip, album_selected), -1, layout);
          flip.showNext();
        }
      };

    flip.addView(newList(new ArtistsQuery(), artist_selected), -1, layout);
    flip.setInAnimation(getContext(), R.anim.slide_in_right);
    flip.setOutAnimation(getContext(), R.anim.slide_out_left);
    return flip;
  }

  private View newList(Uri q) {
    return newList(new MediaQuery(q, null), newListener());
  }

  private View newList(Uri q, ExtraEntry[] entries) {
    return newList(new MediaQuery(q, entries), newListener());
  }

  private View newList(
      final PickerQuery q, final ViewAnimator flip,
      AdapterView.OnItemClickListener click) {
    final View list = newList(q, click);
    list.setOnKeyListener(new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK &&
              event.getAction() == KeyEvent.ACTION_UP) {
            flip.setInAnimation(getContext(), R.anim.slide_in_left);
            flip.setOutAnimation(getContext(), R.anim.slide_out_right);
            flip.showPrevious();
            flip.removeView(list);
            flip.setInAnimation(getContext(), R.anim.slide_in_right);
            flip.setOutAnimation(getContext(), R.anim.slide_out_left);
            return true;
          }
          return false;
        }
      });
    return list;
  }

  private View newList(
      final PickerQuery q, AdapterView.OnItemClickListener click) {
    final ResourceCursorAdapter adapter = new ResourceCursorAdapter(
        getContext(), R.layout.media_picker_item, null, 0) {
        @Override
        public void bindView(View v, Context context, Cursor c) {
          TextView t = (TextView)v;
          t.setText(c.getString(c.getColumnIndex(q.display)));
          int index = c.getColumnIndex("uri");
          if (index >= 0)
            t.setTag(Uri.parse(c.getString(index)));
          else
            t.setTag(ContentUris.withAppendedId(
                q.query, c.getLong(c.getColumnIndex(BaseColumns._ID))));
        }
      };

    final ListView list = new ListView(getContext());
    list.setId(View.generateViewId());
    list.setAdapter(adapter);
    list.setOnItemClickListener(click);

    final Loader<Cursor> loader = getLoaderManager().initLoader(
        list.getId(), null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
              return new CursorLoader(
                  getContext(), q.query, null, q.selection, null, q.sort);
            }
            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
              if (q.entries == null) {
                adapter.changeCursor(data);
              } else {
                MatrixCursor static_entries = new MatrixCursor(new String[] {
                    BaseColumns._ID, "uri", q.display });
                for (ExtraEntry entry : q.entries)
                  static_entries.newRow()
                    .add(BaseColumns._ID, -1)
                    .add("uri", entry.uri.toString())
                    .add(q.display, entry.display);
                adapter.changeCursor(
                    new MergeCursor(new Cursor[] { static_entries, data }));
              }
            }
            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
              adapter.changeCursor(null);
            }
          });
    return list;
  }

  private abstract class PickerQuery {
    Uri query;
    String selection;
    String sort;
    String display;
    ExtraEntry[] entries;
  }

  private class ExtraEntry {
    final Uri uri;
    final String display;
    public ExtraEntry(Uri u, String d) { uri = u; display = d; }
  }

  private final class ArtistsQuery extends PickerQuery {
    public ArtistsQuery() {
      query = Audio.Artists.EXTERNAL_CONTENT_URI;
      sort = Audio.Artists.DEFAULT_SORT_ORDER;
      display = Audio.ArtistColumns.ARTIST;
    }
  }

  private final class AlbumsQuery extends PickerQuery {
    public AlbumsQuery(long artist_id) {
      query = Audio.Artists.Albums.getContentUri(volume, artist_id);
      sort = Audio.Albums.DEFAULT_SORT_ORDER;
      display = Audio.Albums.ALBUM;
    }
    private final String volume =
      Audio.Artists.EXTERNAL_CONTENT_URI.getPathSegments().get(0);
  }

  private final class MediaQuery extends PickerQuery {
    public MediaQuery(Uri q, ExtraEntry[] e) {
      query = q;
      sort = Audio.Media.DEFAULT_SORT_ORDER;
      display = MediaColumns.TITLE;
      entries = e;
    }
    public MediaQuery(long album_id) {
      query = Audio.Media.EXTERNAL_CONTENT_URI;
      selection = Audio.AudioColumns.ALBUM_ID + " == " + album_id;
      sort = Audio.AudioColumns.TRACK;
      display = MediaColumns.TITLE;
      entries = null;
    }
  }

  private static final String TAG = MediaPicker.class.getSimpleName();

  public static interface Listener {
    abstract void onMediaPick(Uri uri, String title);
  }

  private Uri uri = null;
  private String title = null;
  private Listener listener = null;
  private MediaPlayer player = null;
  private int tab = 0;
  public void setListener(Listener l) { listener = l; }
}
