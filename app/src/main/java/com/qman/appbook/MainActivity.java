package com.qman.appbook;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.qman.appbook.common.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    AlertDialog.Builder alert;
    private Context context;
    AssetManager assetManager;
    TextView txtRead;
    TextView txtPage;
    ScrollView scrVw;
    String chpaterNo;
    String pageNo;
    String fileNm = "a little princees";
    String fileExt = ".txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alert = setAlert(MainActivity.this);
        assetManager = getResources().getAssets();
        txtRead = (TextView)findViewById(R.id.txtRead);
        txtPage = (TextView)findViewById(R.id.txtPage);
        scrVw = (ScrollView)findViewById(R.id.scrVw);

        context = getApplicationContext();

        chpaterNo = PreferenceManager.getString(context, "chpaterNo");
        if(chpaterNo == null || chpaterNo.equals("")){
            chpaterNo = "1";
            PreferenceManager.setString(context, "chpaterNo",chpaterNo);
        }

        pageNo = PreferenceManager.getString(context, "pageNo");
        if(pageNo == null || pageNo.equals("")){
            pageNo = "1";
            PreferenceManager.setString(context, "pageNo",pageNo);

        }

        readTxtAction();
    }

    public void readTxtAction(){
        String read = getDataFromAsset(getFileNm(chpaterNo,pageNo));
        txtRead.setText(read);
        txtPage.setText("chpater"+chpaterNo+" page - "+pageNo);


    }

    public void prePageAction(View v){
        Integer chpaterNoInt = Integer.parseInt(PreferenceManager.getString(context, "chpaterNo"));
        Integer pageNoInt = Integer.parseInt(PreferenceManager.getString(context, "pageNo"));
        boolean isPreFile = true;

        if(pageNoInt != 1){
            pageNoInt--;
        } else if(pageNoInt == 1 && chpaterNoInt != 1){
            chpaterNoInt--;
            pageNoInt = getLastPageByChapter(Integer.toString(chpaterNoInt));//해당챕터 마지막pg
        }else {
            alert("첫번째 페이지 입니다.");
            isPreFile = false;
        }

        if(isPreFile){
            chpaterNo = Integer.toString(chpaterNoInt);
            pageNo = Integer.toString(pageNoInt);
            PreferenceManager.setString(context, "chpaterNo",chpaterNo);
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
        Map<String,String> nextFileInfo = getNextFileInfo();

        if(nextFileInfo != null){
            chpaterNo = nextFileInfo.get("chpaterNo");
            pageNo = nextFileInfo.get("pageNo");
            PreferenceManager.setString(context, "chpaterNo",chpaterNo);
            PreferenceManager.setString(context, "pageNo",pageNo);
            readTxtAction();
        }else {
            alert("마지막 페이지 입니다.\n첫페이지로 이동합니다.");
            chpaterNo = "1";
            pageNo = "1";
            PreferenceManager.setString(context, "chpaterNo",chpaterNo);
            PreferenceManager.setString(context, "pageNo",pageNo);
            readTxtAction();
        }

        scrVw.post(new Runnable(){
            public void run(){
                scrVw.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    public Map<String,String> getNextFileInfo(){
        HashMap<String,String> resultMap = null; //다음페이지 없음
        //페이지 + 1
        Integer chpaterNoInt = Integer.parseInt(PreferenceManager.getString(context, "chpaterNo"));
        Integer pageNoInt = Integer.parseInt(PreferenceManager.getString(context, "pageNo"));
        pageNoInt++;
        String tmpChpaterNo = Integer.toString(chpaterNoInt);
        String tmpPageNo = Integer.toString(pageNoInt);


        boolean isNextFile = false;
        if(isFile(getFileNm(tmpChpaterNo,tmpPageNo))){
            isNextFile = true;

        }else{
            chpaterNoInt++;
            tmpChpaterNo = Integer.toString(chpaterNoInt);
            tmpPageNo = "1";
            if(isFile(getFileNm(tmpChpaterNo,tmpPageNo))){
                isNextFile = true;
            }
        }

        if(isNextFile){
            resultMap = new HashMap<String,String>();
            resultMap.put("chpaterNo",tmpChpaterNo);
            resultMap.put("pageNo",tmpPageNo);
        }
        return resultMap;
    }

    public Integer getLastPageByChapter(String chpaterNo){
        int lastPageNo = 1;
        for(int i = 1; i<100; i++){
            if(!isFile(getFileNm(chpaterNo,Integer.toString(i)))){
                lastPageNo =i-1;
                break;
            }
        }
        return lastPageNo;
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



    public String getFileNm(String chpaterNo,String pageNo){
        String tmpChpaterNo = chpaterNo;
        int chpaterNoLng = chpaterNo.length();
        for(int i = chpaterNoLng ;i<2 ;i++){
            tmpChpaterNo = "0"+tmpChpaterNo;
        }

        String tmpPage = pageNo;
        int pageNoLng = pageNo.length();
        for(int i = pageNoLng ;i<3 ;i++){
            tmpPage = "0"+tmpPage;
        }
        return fileNm+"_"+tmpChpaterNo+"_"+tmpPage+fileExt;
    }

    //Alert Dialog 셋팅
    private AlertDialog.Builder setAlert(Context context){
        AlertDialog.Builder resultAlert = new AlertDialog.Builder(context);
        resultAlert.setTitle("알림");
        resultAlert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(),"Pressed OK",
                        Toast.LENGTH_SHORT).show();
            }
        });
        return resultAlert;
    }
    private void alert(String msg){
        alert.setMessage(msg).show();
    }

}