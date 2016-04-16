package com.example.huaif.myfiletransfer;

import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by huaif on 2016/4/16.
 */
public class AdvanceFragment extends Fragment {

    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.advance_fragment,null);
        Button bt_copy = (Button) view.findViewById(R.id.bt_copy_ip);
        final TextView tv_ip=(TextView)view.findViewById(R.id.tv_ipAdress);
        WifiManager wifiManager=(WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo=wifiManager.getConnectionInfo();
        int ip=wifiInfo.getIpAddress();
        String stringIp=intToIp(ip);
        tv_ip.setText(stringIp);

        bt_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cmb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(tv_ip.getText().toString());
                Toast.makeText(getActivity().getApplicationContext(),"已复制到剪切板",Toast.LENGTH_LONG).show();
            }
        });


        return  view;
    }

}
