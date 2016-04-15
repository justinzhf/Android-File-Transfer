package com.example.huaif.myfiletransfer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by huaif on 2016/4/15.
 */
public class EHandler extends Handler {

    public EHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                UIWiget test1 = (UIWiget) msg.obj;
                test1.getPd().show();
                break;
            case 2:
                Toast.makeText((Context) msg.obj, "发送成功!", Toast.LENGTH_LONG).show();
                break;
            case 3:
                UIWiget test2 = (UIWiget) msg.obj;
                test2.getPd().show();
                break;
            case 4:
                Toast.makeText((Context) msg.obj, "接收成功!", Toast.LENGTH_LONG).show();
                break;
            case 5:
                UIWiget test3 = (UIWiget) msg.obj;
                test3.getPd().show();
                break;
            case 6:
                Toast.makeText((Context) msg.obj, "传输成功!", Toast.LENGTH_LONG).show();
                break;
            case 7:

                break;
            case 8:
                Toast.makeText((Context)msg.obj,"下载成功!",Toast.LENGTH_LONG).show();
                break;
        }
    }
}

