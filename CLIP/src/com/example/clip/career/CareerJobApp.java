package com.example.clip.career;

import com.example.clip.R;
import com.example.clip.R.id;
import com.example.clip.R.layout;
import com.example.clip.R.menu;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class CareerJobApp extends ListActivity implements OnItemClickListener, OnItemLongClickListener {

	ArrayAdapter<String> listViewAdapter, popUpAdapter;
	ArrayList<String> jobList, popUpItems;
	ListPopupWindow popUp;
	String jobName;
	
	HashMap<String, String[]> dataMap;	//<jobName, jobData>
	HashMap<String, int[]> dateAppMap;	//<jobName, dateApplied>
	String[] jobData;					//{appStatus, comments}
	int[]	jobDateApplied;				//{Month, Day, Year}
	
	ParseQuery<ParseObject> query = ParseQuery.getQuery("careerJob");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		//initiate list
		this.createEmptyList();
		
		
		//initiate the pop-up list
		popUp = new ListPopupWindow(this);
		popUpItems = new ArrayList<String>();
		popUpItems.add(getString(R.string.action_edit));
		popUpItems.add(getString(R.string.action_remove));
		popUpAdapter = new ArrayAdapter<String>(this, R.layout.edit_remove_popup,
				R.id.label_popUp, popUpItems);
		popUp.setAdapter(popUpAdapter);
		popUp.setModal(true);
		popUp.setWidth(200);
		popUp.setHeight(ListPopupWindow.WRAP_CONTENT);
		
	//	ParseAnalytics.trackAppOpened(getIntent());
		ParseQuery<ParseObject> query = ParseQuery.getQuery("careerJob");
		query.whereEqualTo("Owner", ParseUser.getCurrentUser());
		
		// Create query for objects of type "Post"

			// Restrict to cases where the author is the current user.
			// Note that you should pass in a ParseUser and not the
			// String reperesentation of that user
			// Run the query
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> postList, ParseException e) {
					if (e == null) {
						jobList.clear();
						dataMap.clear();
						// If there are results, update the list of posts
						// and notify the adapter
						for (ParseObject job : postList) {
							jobData[0] = job.getString("Status");
							jobData[1] = job.getString("Comments");
							jobDateApplied[0] = job.getInt("DateAppliedMonth");
							jobDateApplied[1] = job.getInt("DateAppliedDay");
							jobDateApplied[2] = job.getInt("DateAppliedYear");
							jobList.add(job.getString("Name"));
							dataMap.put(job.getString("Name"), jobData);
						}
						((ArrayAdapter<String>)getListAdapter()).notifyDataSetChanged();

					} else {
						Log.d("Post retrieval", "Error: " + e.getMessage());
					}

				}

			});
			
		listViewAdapter = new ArrayAdapter<String>(this, R.layout.activity_career_job_app,
				R.id.label_jobList, jobList);
		this.setListAdapter(listViewAdapter);
		//listeners
		this.getListView().setOnItemClickListener(this);
		this.getListView().setOnItemLongClickListener(this);
	}
	
	
	/*@Override
	protected void onPause()
	{
		super.onPause(); 
		query.whereEqualTo("Owner", ParseUser.getCurrentUser());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> postList, ParseException e) {
				if (e == null) {
					// If there are results, update the list of posts
					// and notify the adapter
					for (ParseObject job : postList) {
						job.deleteInBackground();
					}
					
				} else {
					Log.d("Post retrieval", "Error: " + e.getMessage());
				}

			}

		});
		
		 for(Map.Entry<String, String[]> entry : dataMap.entrySet()){
			ParseObject careerJob = new ParseObject("careerJob");
			careerJob.put("Owner", ParseUser.getCurrentUser());
			careerJob.put("Name", entry.getKey());
			careerJob.put("Status", entry.getValue()[0]);
			careerJob.put("Comments", entry.getValue()[1]);
			 for(Map.Entry<String, int[]> entry1 : dateAppMap.entrySet())
			 {
				 if (entry1.getKey().toString().equalsIgnoreCase(entry.getKey().toString()))
				 {
					 careerJob.put("DateAppliedMonth", entry.getValue()[0]);
					 careerJob.put("DateAppliedDay", entry.getValue()[1]);
					 careerJob.put("DateAppliedYear", entry.getValue()[2]);
					 break;
				 }
			 }
			careerJob.saveInBackground();
		 }
		 
	}*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		//edit job
		if(requestCode == 1) {	
			
			//remove old data first
			this.jobName = data.getStringExtra("oldName");
			this.jobList.remove(jobName);
			this.dataMap.remove(jobName);
			this.dateAppMap.remove(jobName);
		}
		//add job
		else if(requestCode == 0) {
			
			//clears any initial data
			if(jobList.get(0).equals(getString(R.string.none))) {
			
				jobList.remove(0);
				dataMap.remove(getString(R.string.none));
			}
		}
		
		//add new data
		this.jobName = data.getStringExtra("name");
		this.jobData = data.getStringArrayExtra("data");
		this.jobDateApplied = data.getIntArrayExtra("date");
		
		//adding data to data maps
		this.jobList.add(this.jobName);
		this.dataMap.put(this.jobName, this.jobData);
		this.dateAppMap.put(this.jobName, this.jobDateApplied);
		
		//updating screen, resetting listeners
		this.onContentChanged();
		getListView().setOnItemClickListener(this);
		popUp.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.career_job_app, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add) {
			
			Intent i = new Intent(CareerJobApp.this, CareerJobAppEdit.class);
			startActivityForResult(i, 0);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
       
		if(popUp.isShowing()) {
			
			popUp.dismiss();
			
			//edit is clicked && current item is not "None"
			if(this.popUpItems.get(position).equals(getString(R.string.action_edit))
					&& !jobList.get(position).equals(getString(R.string.none))) {
											
				Intent i = new Intent(CareerJobApp.this, CareerJobAppEdit.class);
				
				this.jobData = dataMap.get(jobList.get(position));
				this.jobDateApplied = this.dateAppMap.get(jobList.get(position));
				
				i.putExtra("name", jobList.get(position));
				i.putExtra("data", this.jobData);
				i.putExtra("date", this.jobDateApplied);
				
				this.startActivityForResult(i, 1);
			}
			//remove is clicked
			else if(this.popUpItems.get(position).equals(getString(R.string.action_remove))) {
				
				jobList.remove(jobName);
				dataMap.remove(jobName);
				this.dateAppMap.remove(jobName);
				
				if(jobList.isEmpty()) {
				
					this.resetEmptyList();
				}
				this.onContentChanged();
			}
		}
		//show details
		else {
			
			Intent i = new Intent(CareerJobApp.this, CareerJobAppDetail.class);		
			this.jobData = dataMap.get(jobList.get(position));
			this.jobDateApplied = this.dateAppMap.get(jobList.get(position));
			
			i.putExtra("name", jobList.get(position));
			i.putExtra("data", this.jobData);
			i.putExtra("date", this.jobDateApplied);
			
			startActivity(i);
		}
    }
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		
		this.jobName = jobList.get(position);
		popUp.setAnchorView(view);
		popUp.show();
		popUp.getListView().setOnItemClickListener(this); 
		return true;
	}
	
	private void createEmptyList() {
		
		jobList = new ArrayList<String>();
		jobList.add(getString(R.string.none));
		jobData = new String[] {"Status N/A", "No comments."};
		dataMap = new HashMap<String, String[]>();
		dataMap.put(jobList.get(0), jobData);
		jobDateApplied = new int[3];
		dateAppMap = new HashMap<String, int[]>();
		dateAppMap.put(jobList.get(0), jobDateApplied);
	}
	
	private void resetEmptyList() {
		
		jobList.add(getString(R.string.none));
		jobData = new String[] {"Status N/A", "No comments."};
		dataMap = new HashMap<String, String[]>();
		dataMap.put(jobList.get(0), jobData);
		jobDateApplied = new int[3];
		dateAppMap = new HashMap<String, int[]>();
		dateAppMap.put(jobList.get(0), jobDateApplied);
	}
}
