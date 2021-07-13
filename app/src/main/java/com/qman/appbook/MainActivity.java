package com.qman.appbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qman.appbook.common.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private Context context;
    AssetManager assetManager;
    TextView txtRead;
    TextView txtPage;
    ScrollView scrVw;
    String pageNo;
    String fileNm = "a little princees";
    String fileExt = ".txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assetManager = getResources().getAssets();
        txtRead = (TextView)findViewById(R.id.txtRead);
        txtPage = (TextView)findViewById(R.id.txtPage);
        scrVw = (ScrollView)findViewById(R.id.scrVw);

        context = getApplicationContext();
        pageNo = PreferenceManager.getString(context, "pageNo");
        if(pageNo == null || pageNo.equals("")){
            pageNo = "1";
            PreferenceManager.setString(context, "pageNo",pageNo);

        }


        readTxtAction();
    }

    public void readTxtAction(){
        String read = getDataFromAsset(fileNm+getFilePage(pageNo)+fileExt);
        txtRead.setText(read);
        txtPage.setText("page : "+pageNo);


    }

    public void prePageAction(View v){
        Integer pageNoInt = Integer.parseInt(PreferenceManager.getString(context, "pageNo"));
        if(pageNoInt != 1){
            pageNoInt--;
            pageNo = Integer.toString(pageNoInt);
            PreferenceManager.setString(context, "pageNo",pageNo);

            readTxtAction();
            scrVw.post(new Runnable(){
                public void run(){
                    scrVw.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

    }

    public void nextPageAction(View v){
        Integer pageNoInt = Integer.parseInt(PreferenceManager.getString(context, "pageNo"));
        pageNoInt++;
        pageNo = Integer.toString(pageNoInt);

        boolean isFile = isFile(fileNm+getFilePage(pageNo)+fileExt);
        if(isFile){
            PreferenceManager.setString(context, "pageNo",pageNo);
            readTxtAction();
        }else {
            pageNo = "1";
            PreferenceManager.setString(context, "pageNo",pageNo);
            readTxtAction();
        }

        scrVw.post(new Runnable(){
            public void run(){
                scrVw.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    public boolean isFile(String fileNm){
        boolean isFile = false;
        InputStream inputStream= null;
        try{
            inputStream = assetManager.open(fileNm, AssetManager.ACCESS_BUFFER);
            if(inputStream != null){
                isFile = true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) {}
            }
        }
        return isFile;
    }

    public String getDataFromAsset(String fileNm){
        Log.d("fileNm : ",fileNm);
        InputStream inputStream = null;
        String strResult = "";
        try{
            //asset manager에게서 inputstream 가져오기
            inputStream = assetManager.open(fileNm, AssetManager.ACCESS_BUFFER);

            //문자로 읽어들이기
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            //파일읽기

            String line = "";
            while((line=reader.readLine()) != null){
                strResult += line + "\n";
            }

            //읽은내용 출력
            return strResult;
        }catch (IOException e){
            e.printStackTrace();
            return "";
        }finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) {}
            }
        }
    }



    public String getFilePage(String pageNo){
        String returnPageNo = pageNo;
        int pageNoLng = pageNo.length();
        for(int i = pageNoLng ;i<3 ;i++){
            returnPageNo = "0"+returnPageNo;
        }
        return returnPageNo;
    }
}