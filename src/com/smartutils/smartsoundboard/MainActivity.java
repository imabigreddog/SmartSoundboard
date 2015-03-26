package com.smartutils.smartsoundboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
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

	private List<MediaPlayer> players;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		buttonLabels = new ArrayList<String>();
		fileNames = new ArrayList<String>();
		players = new ArrayList<MediaPlayer>();

		grid = (GridView) findViewById(R.id.grid);

		assetManager = getAssets();

		try {
			for (String s : assetManager.list("sfx")) {
				buttonLabels.add(s.substring(0, 1).toUpperCase() + s.substring(1, s.indexOf(".")).replaceAll("_", ""));

				fileNames.add("sfx/" + s);
			}

			for (File f : getFilesDir().listFiles()) {
				System.out.println("File " + f.getAbsolutePath());
				String file = f.getName();
				buttonLabels.add(file.substring(0, 1).toUpperCase() + file.substring(1, file.indexOf(".")).replaceAll("_", ""));
				fileNames.add(f.getAbsolutePath());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		appendButtonsToView();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

		Thread d = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {

					synchronized (players) {
						for (int i = 0; i < players.size(); i++) {
							if (!players.get(i).isPlaying()) {
								System.out.println(players.get(i) + " is released");
								players.get(i).release();
								players.remove(i);
								i--;
							}
						}
					}

				}

			}
		});
		d.setDaemon(true);
		d.start();
	}

	private void downloadDank(final String url) {

		final ProgressDialog pd = new ProgressDialog(this);
		pd.setTitle("Downloading dank beatz");
		pd.setMessage("Hold on right kwik");
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		final EditText textField = new EditText(MainActivity.this);
		textField.setHint("Enter a name to save it as");
		new AlertDialog.Builder(MainActivity.this).setTitle("Enter File Name").setView(textField)
		.setPositiveButton("Save", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				new DankDownloader(url, pd, textField.getText().toString()).execute(new Void[] {});
				dialog.cancel();
				dialog.dismiss();
				dialog = null;
				System.gc();
			}

		}).setCancelable(false).show();

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

				System.out.println(fileNames.size());
				System.out.println(fileNames.get(fileNames.size() - 1));
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

				if (fileName.contains("sfx")) {
					AssetFileDescriptor descriptor = assetManager.openFd(fileName);
					MediaPlayer player = new MediaPlayer();
					long start = descriptor.getStartOffset();
					long end = descriptor.getLength();

					player.setDataSource(descriptor.getFileDescriptor(), start, end);
					descriptor.close();
					player.setVolume(1f, 1f);
					player.prepare();

					player.start();
				} else {
					FileInputStream in = null;
					try {
						MediaPlayer player = new MediaPlayer();

						in = new FileInputStream(fileName);
						player.setDataSource(in.getFD());

						player.setVolume(1f, 1f);
						player.prepare();

						player.start();
						synchronized (players) {
							players.add(player);
						}

					} finally {
						in.close();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

	}

	private class DankDownloader extends AsyncTask<Void, Void, Void> {

		private ProgressDialog d;
		private String url;
		private String fileName;

		private DankDownloader(String url, ProgressDialog dankweed, String filename) {

			this.fileName = filename;
			d = dankweed;
			this.url = url;
		}

		@Override
		protected Void doInBackground(Void... params) {

			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					d.show();
				}

			});

			try {

				URLConnection connection;
				try {
					connection = new URL(url).openConnection();
				} catch (MalformedURLException x) {
					d.dismiss();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							Toast.makeText(getBaseContext(), "Invalid Url", Toast.LENGTH_LONG).show();
						}

					});
					return null;
				}
				InputStream in = connection.getInputStream();

				FileOutputStream out = openFileOutput(fileName + ".mp3", Context.MODE_PRIVATE);

				byte[] buffer = new byte[4096];
				int len;

				while ((len = in.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
				out.close();
				in.close();

				synchronized (buttonLabels) {
					buttonLabels.add(fileName);
				}

				synchronized (fileNames) {
					System.out.println(getFilesDir().getAbsolutePath() + fileName + ".mp3");
					fileNames.add(getFilesDir().getAbsolutePath() + "/" + fileName + ".mp3");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			d.dismiss();

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					appendButtonsToView();

				}
			});

		}
	}

	
	//TODO:Make a drawer button that calls this, make the ProgDialog have the option to throw away or 
	//TODO:keep the recorded file. Potentially work on saving as MP3, but the current hacky method of 
	//TODO:using just the extension with the real 3GPP works fine for now. MAKE BUTTONS THAT CALL START & STOP
	
	private class DankRecorder extends AsyncTask<Void, Void, Void> {

		private ProgressDialog d;
		private String fileName;

		private DankRecorder(ProgressDialog dankweed, String filename) {

			this.fileName = filename;
			d = dankweed;
		}

		@Override
		protected Void doInBackground(Void... params) {

			runOnUiThread(new Runnable()  {
				MediaRecorder mRecorder=new MediaRecorder();
				@Override
				public void run() {
					d.setMessage("Now recording...");
					d.setCancelable(true);
					d.show();
					startRecording();
				}

				private void startRecording() {
			        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			        mRecorder.setOutputFile(fileName+".mp3");
			        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

			        try {
			            mRecorder.prepare();
			        } catch (IOException e) {
			        	e.printStackTrace();
			        }

			        mRecorder.start();		
			        
				}
				private void stopRecording(){
					
					mRecorder.stop();
					mRecorder.release();
					mRecorder=null;
					synchronized (buttonLabels) {
						buttonLabels.add(fileName);
					}

					synchronized (fileNames) {
						System.out.println(getFilesDir().getAbsolutePath() + fileName + ".mp3");
						fileNames.add(getFilesDir().getAbsolutePath() + "/" + fileName + ".mp3");
					}
				}

			});
			
			

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					appendButtonsToView();

				}
			});

		}
	}

}
