package com.example.huaif.myfiletransfer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by huaif on 2016/4/15.
 */
public class ShowFilesActivity extends Activity {
    private ListView listView;
    private ArrayList<String> fileNames;
    private ProgressDialog pd;
    private ProgressDialog pds;
    private Looper mainLooper;
    private EHandler mainHandler;
    private String selectedFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showfiles);
        listView=(ListView)findViewById(R.id.lv_showfiles);
        Intent intent=getIntent();
        fileNames=intent.getStringArrayListExtra("fileNames");
        pd=new ProgressDialog(this);
        pds=new ProgressDialog(this);
        mainLooper = Looper.getMainLooper();
        mainHandler = new EHandler(mainLooper);

        listView.setAdapter(new MyAdapter());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFile=fileNames.get(position);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setTitle("正在下载");
                final CSDownload csDownload=new CSDownload(mainHandler,pd,pds,getApplicationContext());
                Thread  thread=new Thread(){
                    @Override
                    public void run() {
                        csDownload.startDownload(selectedFile);
                    }
                };
                pd.show();
                thread.start();
            }
        });



    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return fileNames.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(getApplicationContext());
            String fileName = fileNames.get(position).split("/")[5];
            tv.setTextSize(25);
            tv.setTextColor(Color.BLACK);
            tv.setText(fileName);
            return tv;
        }
    }

}
