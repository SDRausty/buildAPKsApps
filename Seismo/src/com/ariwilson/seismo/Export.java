package com.ariwilson.seismo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Export extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                         WindowManager.LayoutParams.FLAG_FULLSCREEN);
    FrameLayout layout = new FrameLayout(this);
    export_view_ = new ExportView(this);
    layout.addView(export_view_);
    setContentView(layout);
  }

  @Override 
  public boolean onContextItemSelected(MenuItem item) { 
    AdapterContextMenuInfo menu_info =
        (AdapterContextMenuInfo) item.getMenuInfo(); 

    switch (item.getItemId()) { 
      case 0:
        export_view_.db_.open(this);
        if (export_view_.db_.deleteGraph(export_view_.graph_names_.get(
                menu_info.position))) {
          export_view_.graph_names_.remove(menu_info.position);
          export_view_.adapter_.notifyDataSetChanged();
        } else {
          Toast.makeText(this, "Failed to delete graph in position " +
                               Long.toString(menu_info.position) + ".",
                         Toast.LENGTH_LONG).show();
        }
        export_view_.db_.close();
        return true;
    } 
    return false; 
  }

  private class ExportView extends GraphListView implements
      AdapterView.OnItemClickListener, OnCreateContextMenuListener {
    ExportView(Context ctx) {
      super(ctx);
      ctx_ = ctx;
      setOnItemClickListener(this);
      setOnCreateContextMenuListener(this);
    }

    public void onItemClick(AdapterView<?> parent_view, View child_view,
                            int position, long id) {
      db_.open(ctx_);
      ArrayList<ArrayList<Float>> graph = db_.fetchGraph(graph_names_.get(
          position));
      db_.close();
      try {
    	File path = Environment.getExternalStoragePublicDirectory(
    	    Environment.DIRECTORY_DOWNLOADS);
        File temp_file = File.createTempFile("Seismo", ".csv", path);
        FileOutputStream out = new FileOutputStream(temp_file);
        out.write(graphToCsv(graph).getBytes());
        out.close();
        Intent send_intent = new Intent(Intent.ACTION_SEND);
        send_intent.setType("text/csv");
        send_intent.putExtra(Intent.EXTRA_SUBJECT,
           "Seismo data from " + graph_names_.get(position));
        send_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(
            temp_file)); 
        startActivity(Intent.createChooser(send_intent, "E-mail"));
        temp_file.deleteOnExit();
      } catch (Exception e) {
        Log.e("Seismo", e.toString());
      }
    }

    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menu_info) {
      menu.setHeaderTitle("Options");
      menu.add(0, 0, 0, "Delete");
    }

    private String graphToCsv(ArrayList<ArrayList<Float>> graph) {
      StringBuffer csv = new StringBuffer();
      csv.append("time (seconds),");
      csv.append("x acceleration (m/s^2),");
      csv.append("y acceleration (m/s^2),");
      csv.append("z acceleration (m/s^2)\n");
      for (int i = 0; i < graph.size(); ++i) {
        assert(graph.get(i).size() == 3);
        csv.append(Float.toString(graph.get(i).get(0) / 1000));
        csv.append(",");
        csv.append(graph.get(i).get(1).toString());
        csv.append(",");
        csv.append(graph.get(i).get(2).toString());
        csv.append(",");
        csv.append(graph.get(i).get(3).toString());
        csv.append("\n");
      }
      return csv.toString();
    }

    private Context ctx_;
  }

  private ExportView export_view_;
}
