package com.smartutils.smartsoundboard;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.EditText;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CharSequence mTitle;
	private boolean run = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();
		
		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}
	
	public final void onNavigationDrawerItemSelected(int position) {
	
		System.out.println(position);
		
		// run is for weird android thing
		if (position == 0 && run) {
			new AlertDialog.Builder(MainActivity.this).setTitle("Please Enter a Link to a sound file").setView(new EditText(MainActivity.this))
					.setPositiveButton("Download", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						
							// TODO download form link and launch progress dialog.
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
	
}
