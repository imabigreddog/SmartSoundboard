package com.smartutils.smartsoundboard;

import java.io.IOException;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
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
	
	public final void onNavigationDrawerItemSelected(int position) {
	
		System.out.println(position);
		
		// run is for weird android thing
		if (position == 0 && run) {
			EditText textField = new EditText(MainActivity.this);
			textField.setHint("Enter the URL");
			new AlertDialog.Builder(MainActivity.this).setTitle("Please Enter a Link to a sound file").setView(textField)
					.setPositiveButton("Download", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						
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
				
			} catch (IOException e) {
			}
			return null;
		}
		
	}
}
