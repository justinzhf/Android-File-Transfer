package com.example.huaif.myfiletransfer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by huaif on 2016/4/15.
 */
public class P2PFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.p2p_fragment,null);
        TextView tv=(TextView)view.findViewById(R.id.tv_p2p_tutorial);
        String con="点击“我要发送”，选择文件进行发送，点击“我要接收”，接收对方发送的文件";
        tv.setText(ToDBC(con));
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
