package com.example.scoreinquiry_old;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {
	private CourseDataUtil courseDataUtil;
	private ListView listview;
	private List<Map<String,String>> courseInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listview=(ListView)findViewById(R.id.listView1);
		courseDataUtil = new CourseDataUtil(this);
		courseInfo=courseDataUtil.getAllCourseScoreData();
		CourseListAdapter cLA=new CourseListAdapter(this,courseInfo,courseInfo);
		listview.setAdapter(cLA);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.login) {
			Intent intent=new Intent(this,Login.class);
			startActivityForResult(intent,1);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onPause() {
		super.onPause();
		if(courseDataUtil!=null){
			courseDataUtil.close();
			courseDataUtil=null;
		}
	}
	
	public void onResume(){
		super.onResume();
		if(courseDataUtil==null){
			courseDataUtil = new CourseDataUtil(this);
		}
	}
	
	public void onActivityResult(int requestCode,int resultCode,Intent intent){
		if(requestCode==1&&resultCode==0){
			if(courseDataUtil==null){
				courseDataUtil = new CourseDataUtil(this);
			}
			courseInfo=courseDataUtil.getAllCourseScoreData();
			CourseListAdapter cLA=new CourseListAdapter(this,courseInfo,courseInfo);
			listview.setAdapter(cLA);
		}
		if(requestCode==1&&resultCode==1){
			
		}
	}
	public void onDestroy(){
		if(courseDataUtil==null){
			courseDataUtil = new CourseDataUtil(this);
		}
		super.onDestroy();		
	}
}
