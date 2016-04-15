package com.example.huaif.myfiletransfer;

import android.app.Activity;
import android.content.Context;
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
                UIWiget test1=(UIWiget)msg.obj;
                test1.getPds().dismiss();
                test1.getPd().setTitle("正在发送");
                test1.getPd().show();
                break;
            case 2:
                Toast.makeText((Context)msg.obj, "发送成功!", Toast.LENGTH_LONG).show();
                break;
            case 3:
                UIWiget test2=(UIWiget)msg.obj;
                test2.getPds().dismiss();
                test2.getPd().setTitle("正在接收");
                test2.getPd().show();
                break;
            case 4:
                Toast.makeText((Context)msg.obj,"接收成功!",Toast.LENGTH_LONG).show();
                break;
        }
    }
}

