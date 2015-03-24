package com.smartutils.smartsoundboard;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CharSequence mTitle;
	private boolean run = false;
	
	private ArrayList<String> buttonLabels;
	private ArrayList<String> fileNames;
	private GridView grid;
	private AssetManager assetManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		buttonLabels = new ArrayList<String>();
		fileNames = new ArrayList<String>();
		grid = (GridView) findViewById(R.id.grid);
		
		assetManager = getAssets();
		//TODO shared preferences
		try {
			for (String s : assetManager.list("sfx")) {
				buttonLabels.add(s.substring(0, 1).toUpperCase() + s.substring(1, s.indexOf(".")).replaceAll("_", ""));
				fileNames.add("sfx/" + s);
			}
			appendButtonsToView();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}
	
	private void downloadDank(String string) {
	
		ProgressDialog dankweed = new ProgressDialog(this);
		dankweed.setTitle("Downloading dank beatz");
		dankweed.setMessage("Hold on right kwik");
		dankweed.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dankweed.setProgress(0);
		dankweed.setMax(100);
		new DankDownloader(string, dankweed);
	}
	
	public final void onNavigationDrawerItemSelected(int position) {
	
		System.out.println(position);
		
		// run is for weird android thing
		if (position == 0 && run) {
			final EditText textField = new EditText(MainActivity.this);
			textField.setHint("Enter the URL");
			new AlertDialog.Builder(MainActivity.this).setTitle("Please Enter a Link to a sound file").setView(textField)
					.setPositiveButton("Download", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						
							downloadDank(textField.getText().toString());
							dialog.cancel();
							dialog.dismiss();
							dialog = null;
						}
						
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						
							dialog.cancel();
							dialog.dismiss();
							dialog = null;
							System.gc();
						}
					}).setCancelable(false).show();
			
		} else
			run = true;
	}
	
	public void onSectionAttached(int number) {
	
		switch (number) {
		case 0:
			mTitle = getString(R.string.download);
			break;
		}
	}
	
	public void restoreActionBar() {
	
		ActionBar actionBar = getActionBar();
		
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}
	
	private void appendButtonsToView() {
	
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_dropdown_item_1line, buttonLabels);
		grid.setAdapter(adapter);
		
		grid.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
				new DankMediaplayer(fileNames.get(position)).execute(new Void[] {});
				
			}
		});
		
	}
	
	private class DankMediaplayer extends AsyncTask<Void, Void, Void> {
		
		private String fileName;
		
		public DankMediaplayer(String s) {
		
			fileName = s;
			
		}
		
		@Override
		protected Void doInBackground(Void... params) {
		
			try {
				AssetFileDescriptor descriptor = assetManager.openFd(fileName);
				MediaPlayer player = new MediaPlayer();
				long start = descriptor.getStartOffset();
				long end = descriptor.getLength();
				
				player.setDataSource(descriptor.getFileDescriptor(), start, end);
				descriptor.close();
				player.setVolume(1f, 1f);
				player.prepare();
				
				player.start();
				
				while (player.isPlaying()) {
					continue;
				}
				
				player.release();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private class DankDownloader extends AsyncTask<Void, Void, Void> {
		
		private ProgressDialog d;
		private String url;
		
		private DankDownloader(String url, ProgressDialog dankweed) {
		
			d = dankweed;
			this.url = url;
		}
		/**
         🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸🇺🇸
         */
		@Override
		protected Void doInBackground(Void... params) {
		
			try {
				URL toDownload = new URL(url);
				d.show();
				URLConnection dank = toDownload.openConnection();
				dank.connect();
				InputStream weed = dank.getInputStream();
				FileOutputStream ohbabyatriple = openFileOutput(toDownload.getFile(), Context.MODE_PRIVATE);
                int length=dank.getContentLength();
                byte data[] =new byte[4096];
                long cocks=0;
                int africa;
                while((africa=weed.read(data))!=-1){
                    cocks+=africa;
                    if(length>0){
                        d.setProgress((int)(cocks*100/length));//fuck math amirite
                    }
                    ohbabyatriple.write(data,0,africa);
                }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
            }
            d.dismiss();
            Toast.makeText(MainActivity.this,"Dank memes r ready :^)",Toast.LENGTH_SHORT).show();
			return null;
			
		}
		
	}
}
