package com.example.huaif.myfiletransfer;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by huaif on 2016/4/15.
 */
public class UIWiget {

    private Context context=null;
    private ProgressDialog pd=null;
    private ProgressDialog pds=null;

    public UIWiget(Context context) {
        this.context = context;
    }

    public UIWiget(ProgressDialog pd, ProgressDialog pds) {
        this.pd = pd;
        this.pds = pds;
    }

    public UIWiget(Context context, ProgressDialog pd, ProgressDialog pds) {
        this.context = context;
        this.pd = pd;
        this.pds = pds;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ProgressDialog getPd() {
        return pd;
    }

    public void setPd(ProgressDialog pd) {
        this.pd = pd;
    }

    public ProgressDialog getPds() {
        return pds;
    }

    public void setPds(ProgressDialog pds) {
        this.pds = pds;
    }

}
