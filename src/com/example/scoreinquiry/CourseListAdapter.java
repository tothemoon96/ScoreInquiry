package com.example.scoreinquiry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CourseListAdapter extends BaseAdapter {

	Context mContext;
	LayoutInflater inflater;
	List<Map<String,String>> courseScoreList,courseList;
	List<String> termList;
	int typesCount;
	List<ListItems> mListItems=new ArrayList<ListItems>();
	final int VIEW_TYPE = 2;
    final int TYPE_1 = 0;
    final int TYPE_2 = 1;
	
    //几个值都不能为null
	public CourseListAdapter(Context context,List<Map<String,String>> courseScoreList,
			List<Map<String,String>> courseList){
		mContext=context;
		inflater = LayoutInflater.from(mContext);
		this.courseScoreList=courseScoreList;
		this.courseList=courseList;
		termList=GetScoreTools.getTerms(courseScoreList,false);
		typesCount=termList.size();
		init();
	}
	
	private void init(){
		mListItems.add(new LableItem("当前可管理课程"));
		for(Map<String,String> course:courseList){
			mListItems.add(new ContentItem(course));
		}
		
		List<String> rawTermList=GetScoreTools.getTerms(courseScoreList,true);
		for(int i=0;i<typesCount;i++){
			mListItems.add(new LableItem(termList.get(i)));
			String rawTerm=rawTermList.get(i);
			List<Map<String,String>> courseInTheTerm=GetScoreTools.getCoursesGroupedByTerm(rawTerm, courseScoreList);
			for(Map<String,String> course:courseInTheTerm){
				mListItems.add(new ContentItem(course));
			}
			Map<String, String> map1=new HashMap<String, String>();
			map1.put("name","GPA");
			map1.put("score",GetScoreTools.getAvarageGPA(courseInTheTerm)+"");
			Map<String, String> map2=new HashMap<String, String>();
			map2.put("name","必修课GPA");
			map2.put("score",GetScoreTools.getRequiredCourseGPA(courseInTheTerm)+"");
			mListItems.add(new ContentItem(map1));
			mListItems.add(new ContentItem(map2));
		}
		mListItems.add(new LableItem("各学期平均GPA"));
		Map<String, String> map1=new HashMap<String, String>();
		map1.put("name","GPA");
		map1.put("score",GetScoreTools.getAvarageGPA(courseScoreList)+"");
		Map<String, String> map2=new HashMap<String, String>();
		map2.put("name","必修课GPA");
		map2.put("score",GetScoreTools.getRequiredCourseGPA(courseScoreList)+"");
		mListItems.add(new ContentItem(map1));
		mListItems.add(new ContentItem(map2));
	}
	
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListItems.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mListItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		LabelViewHolder labelViewHolder = null;
		ContentViewHolder contentViewHolder = null;
		int type = getItemViewType(position);
		if(convertView == null){
			switch(type){
				case TYPE_1:
					convertView=mListItems.get(position).getView(mContext, convertView, inflater);
					labelViewHolder=new LabelViewHolder();
					labelViewHolder.title=(TextView) convertView;
					convertView.setTag(labelViewHolder);
					break;
				case TYPE_2:
					convertView=mListItems.get(position).getView(mContext, convertView, inflater);
					contentViewHolder=new ContentViewHolder();
					contentViewHolder.id=(TextView) convertView.findViewById(R.id.lessons_management_content_item_id);
					contentViewHolder.courseName=(TextView) convertView.findViewById(R.id.lessons_management_content_item_courseName);
					contentViewHolder.score=(TextView) convertView.findViewById(R.id.lessons_management_content_item_score);
					convertView.setTag(contentViewHolder);
					break;
			}
		}else{
			switch(type){
				case TYPE_1:labelViewHolder=(LabelViewHolder) convertView.getTag();break;
				case TYPE_2:contentViewHolder=(ContentViewHolder) convertView.getTag();break;
			}
		}
		switch(type){
			case TYPE_1:labelViewHolder.setView((String) mListItems.get(position).getItem());break;
			case TYPE_2:contentViewHolder.setView((Map<String, String>) mListItems.get(position).getItem());break;
		}
		return convertView;
	}
	
	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return mListItems.get(position).isClickable();
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		ListItems mlistItem=mListItems.get(position);
		if(mlistItem instanceof LableItem){
			return TYPE_1;
		}
		else return TYPE_2;
	}



	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return VIEW_TYPE;
	}
}

interface ListItems<T>{
    public int getLayout();
    public boolean isClickable();
    public View getView(Context context, View convertView, LayoutInflater inflater);
    public T getItem();
}

class LableItem implements ListItems<String>{

	private String mLabel;
	
	public LableItem(String label){
		mLabel=label;
	}
	
	@Override
	public int getLayout() {
		// TODO Auto-generated method stub
		return R.layout.lessons_management_lable_item;
	}

	@Override
	public boolean isClickable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getView(Context context, View convertView,
			LayoutInflater inflater) {
		// TODO Auto-generated method stub
		convertView = inflater.inflate(getLayout(), null);
		TextView title = (TextView) convertView;
		title.setText(mLabel);
		return convertView;	
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mLabel.toString();
	}

	@Override
	public String getItem() {
		// TODO Auto-generated method stub
		return mLabel;
	}
}


class ContentItem implements ListItems<Map<String,String>>{
	private Map<String,String> mItem;
	String _score;
	
	public ContentItem(Map<String,String> item){
		mItem = item;
		_score=mItem.get("score");
	}
	
	@Override
	public int getLayout() {
		return R.layout.lessons_management_content_item;
	}

	@Override
	public boolean isClickable() {
		if(_score==null) return true;
		else return false;
	}

	@Override
	public View getView(Context context, View convertView, LayoutInflater inflater) {
		convertView = inflater.inflate(getLayout(), null);
		TextView id = (TextView) convertView.findViewById(R.id.lessons_management_content_item_id);
		TextView courseName=(TextView) convertView.findViewById(R.id.lessons_management_content_item_courseName);
		TextView score = (TextView) convertView.findViewById(R.id.lessons_management_content_item_score);
		if(_score==null) {
			id.setText(mItem.get("id"));
			score.setText("");
		}
		//在这里设置一下颜色
		else {
			score.setText(_score);
			id.setText("");
		}
		courseName.setText(mItem.get("name"));	
		return convertView;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mItem.get("name");
	}

	@Override
	public Map<String, String> getItem() {
		// TODO Auto-generated method stub
		return mItem;
	}
}


class LabelViewHolder{
	TextView title;
	public void setView(String title){
		this.title.setText(title);
	}
}

class ContentViewHolder{
	TextView id;
	TextView courseName;
	TextView score;
	public void setView(Map<String, String> mItem){
		String _score=mItem.get("score");
		if(_score==null) {
			id.setText(mItem.get("id"));
			score.setText("");
		}
		//在这里设置一下颜色
		else {
			score.setText(_score);
			id.setText("");
		}
		courseName.setText(mItem.get("name"));
	}
}



