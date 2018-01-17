package se.johanhil.duckduckgo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Help extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	 
		
		WebView webview = new WebView(this);
		setContentView(webview);
		webview.loadData(getString(getResources().openRawResource(R.raw.help)), "text/html", "utf8");
	}
	
	/** 
	 * Convert all the data in the input stream to a string (utf8 encoding)
	 * @param in the stream
	 * @return the contents of the stream as a string
	 */
	private String getString(InputStream in)
	{
		StringBuilder str = new StringBuilder();
		String line;
		
		try
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf8"));
			
			while ((line = r.readLine()) != null)
			{
				str.append(line).append('\n');
			}
		}
		catch (Exception e)
		{
			return null;
		}

		return str.toString();
	}
}
