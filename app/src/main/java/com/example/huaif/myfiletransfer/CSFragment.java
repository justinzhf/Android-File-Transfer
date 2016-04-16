package com.example.huaif.myfiletransfer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by huaif on 2016/4/15.
 */

public class CSFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.cs_fragment, null);
        TextView tv_cs_tutor=(TextView)view.findViewById(R.id.tv_cs_tutorial);
        String con="将要共享的文件放到/AMyFileTransfer/share文件夹下，点击“我要分享”后，对方就可以选择下载share文件夹下的所有文件。";
        tv_cs_tutor.setText(ToDBC(con));

        return view;
    }

    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i< c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }if (c[i]> 65280&& c[i]< 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }
}
