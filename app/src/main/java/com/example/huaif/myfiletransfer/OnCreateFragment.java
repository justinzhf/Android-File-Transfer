package com.example.huaif.myfiletransfer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by huaif on 2016/4/15.
 */
public class OnCreateFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.oncreate_fragment, null);
        TextView tv=(TextView)view.findViewById(R.id.tv_init);
        tv.setGravity(Gravity.CENTER);
        return view;
    }
}
