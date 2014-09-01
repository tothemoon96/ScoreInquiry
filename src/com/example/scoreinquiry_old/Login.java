package com.example.scoreinquiry_old;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Login extends Activity {
	private EditText accountView;
	private EditText passwordView;
	private EditText authView;
	private ImageView authPicture;
	private Bitmap authBitmap;
	private Button login;
	private Button clear;
	private String account;
	private String password;
	private String authCode;
	private String cookie;
	private static String LogInURL = "http://202.114.74.198/servlet/Login";
	private static String AuthPicture = "http://202.114.74.198/GenImg";
	private static String QueryStuScore ="http://202.114.74.198/stu/query_score.jsp";
	private CourseDataUtil courseDataUtil;
	private SharedPreferences savedAccount;
	private SharedPreferences.Editor editor;
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			if(msg.what == 10){
				authPicture.setImageBitmap(authBitmap);
				Toast.makeText(Login.this, "验证码获取成功", Toast.LENGTH_SHORT).show();
			}
			if(msg.what == 11){
				Toast.makeText(Login.this, "请检查网络", Toast.LENGTH_SHORT).show();
			}
			if(msg.what == 12){
				authPicture.setEnabled(true);
			}
			if(msg.what==20){
				editor.putString("account", account);
				editor.putString("password", password);
				editor.commit();
				
				Intent intent = new Intent();
				Login.this.setResult(0, intent);
				Login.this.finish();
			}
			if(msg.what==21){
				Toast.makeText(Login.this, "登陆错误，请重新登陆", Toast.LENGTH_SHORT).show();
			}
			if(msg.what == 22){
				Toast.makeText(Login.this, "请检查网络", Toast.LENGTH_SHORT).show();
			}
			if(msg.what == 23){
				login.setText("登陆");
				login.setEnabled(true);
			}
		}
	};
	
	public void onDestroy(){
		super.onDestroy();
		if(courseDataUtil!=null){
			courseDataUtil.close();
			courseDataUtil=null;
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		courseDataUtil = new CourseDataUtil(this);
		accountView=(EditText)findViewById(R.id.editText1);
		passwordView=(EditText)findViewById(R.id.editText2);
		authView=(EditText)findViewById(R.id.editText3);
		login=(Button)findViewById(R.id.button1);
		clear=(Button)findViewById(R.id.button2);
		authPicture=(ImageView)findViewById(R.id.imageView1);
		
		savedAccount=getSharedPreferences("accountInfo", MODE_PRIVATE);
		editor=savedAccount.edit();
		account=savedAccount.getString("account", null);
		password=savedAccount.getString("password", null);
		accountView.setText(account);
		passwordView.setText(password);
		
		authPicture.callOnClick();
	}
	public void onBackPressed() {
		Intent intent = new Intent();
		this.setResult(1, intent);
		this.finish();
	}
	public void login(View v){
	
		account = accountView.getText().toString();
		password = passwordView.getText().toString();
		authCode=authView.getText().toString();
		if(cookie!=null&&!account.equals("") && !password.equals("")&&!authCode.equals("")&&checkNetwork()){
			login.setText("登陆中……");
			login.setEnabled(false);
			new Thread(new LoginThread()).start();			
		}
		else{
			Toast.makeText(this, "请获取验证码&&检查输入&&检查网络", Toast.LENGTH_SHORT).show();
		}
	}	
	
	public void clear(View v){
		accountView.setText("");
		passwordView.setText("");
		authView.setText("");
	}
	
	public void changeAuthCode(View v){
		if (checkNetwork()){
			Toast.makeText(this,"正在获取验证码",Toast.LENGTH_SHORT).show();
			authPicture.setEnabled(false);
			new Thread(new GetAuthThread()).start();
		}
	}
	
	public boolean checkNetwork(){ 
		boolean net = false;  
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();       
        if(networkInfo==null){
        	Toast.makeText(this,"请检查网络", Toast.LENGTH_SHORT).show();
            return net; 
        } 
        int nType = networkInfo.getType(); 
        if(nType==ConnectivityManager.TYPE_MOBILE){ 
            Toast.makeText(this, networkInfo.getExtraInfo(), Toast.LENGTH_SHORT).show();
            net=true;
        } 
        else if(nType==ConnectivityManager.TYPE_WIFI){ 
        	Toast.makeText(this, networkInfo.getExtraInfo(), Toast.LENGTH_SHORT).show();
        	net=true;
        } 
        return net; 
    } 
			
	private void getCookie(DefaultHttpClient httpClient) {
        List<Cookie> cookies = httpClient.getCookieStore().getCookies();
        StringBuffer sb = new StringBuffer();
        Cookie cookie = cookies.get(0);
        String cookieName = cookie.getName();
        String cookieValue = cookie.getValue();
        sb.append(cookieName + "=" );
        sb.append(cookieValue);
        this.cookie=sb.toString();            
	}
	
	public static String getErrorMessage(String html) {
		Document doc = null;
		doc = Jsoup.parse(html);
		Elements links = doc.getElementsByClass("TR_TITLE");
		return links.text().toString();
	}	
	
	public class LoginThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			HttpClient httpClient=new DefaultHttpClient();
			HttpPost post=new HttpPost(LogInURL);
			HttpResponse response = null;
			post.setHeader( "Cookie" , cookie);				
			List<NameValuePair> params = new ArrayList<NameValuePair>(); 
			params.add(new BasicNameValuePair("who", "student"));
			params.add(new BasicNameValuePair("id",account));
			params.add(new BasicNameValuePair("pwd",password));
			params.add(new BasicNameValuePair("yzm",authCode)); 
			params.add(new BasicNameValuePair("submit", "%D5%FD%B3%A3%B5%C7%C2%BC"));
			boolean flag=false;
			try {
				post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String strResult;
			try {
				response = httpClient.execute(post);
				if (response.getStatusLine().getStatusCode() == 200){
					strResult = EntityUtils.toString(response.getEntity());
					strResult = getErrorMessage(strResult);
					if(strResult.indexOf("密码") > 0||strResult.indexOf("验证码") > 0||strResult==null){
						post.abort();
						handler.sendEmptyMessage(21);
					}
					else{
						post.abort();
						HttpGet get=new HttpGet(QueryStuScore);
						get.setHeader( "Cookie" , cookie);
						response = httpClient.execute(get);
						HttpEntity httpEntity = response.getEntity();
			            String content = EntityUtils.toString(httpEntity, "GBK");
			            GetScoreTools courses=new GetScoreTools(content);
			            courses.parse();
			            courseDataUtil.deleteAllCourseScoreData();		            
			            courseDataUtil.addCourseScoreData(courses.getList());
			            flag=true;			            
			            get.abort();
					}
				}
				else{
					post.abort();
					handler.sendEmptyMessage(22);					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
	            httpClient.getConnectionManager().shutdown();
	            handler.sendEmptyMessage(23);
	            if(flag=true) handler.sendEmptyMessage(20);	            
	        }
		}
		
	}
	
	public class GetAuthThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			HttpClient httpClient=new DefaultHttpClient(); 
			HttpGet get=new HttpGet(AuthPicture);
			HttpResponse response;
			try {
				response = httpClient.execute(get);
				if(response.getStatusLine().getStatusCode() == 200){
					getCookie((DefaultHttpClient) httpClient);
					
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					authBitmap = BitmapFactory.decodeStream(is);
					handler.sendEmptyMessage(10);
					is.close();
				}
				else{
					handler.sendEmptyMessage(11);
				}
				get.abort();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}finally{
				httpClient.getConnectionManager().shutdown();
				handler.sendEmptyMessage(12);
			}
		}		
	}
}