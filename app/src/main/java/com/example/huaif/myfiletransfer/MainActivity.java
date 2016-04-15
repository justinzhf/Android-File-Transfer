package com.example.huaif.myfiletransfer;


import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentManager fragmentManager = getFragmentManager();
    private Looper mainLooper = Looper.getMainLooper();
    private EHandler mainHandler = new EHandler(mainLooper);
    FloatingActionButton actionA, actionB;


    private String filePath = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionA = (FloatingActionButton) findViewById(R.id.action_a);
        actionB = (FloatingActionButton) findViewById(R.id.action_b);


        OnCreateFragment ocf = new OnCreateFragment();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.content_main, ocf);
        ft.commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
            Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
            this.filePath = uri.getPath();
            startSend();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_p2pMode) {
            final P2PFragment p2pFragment = new P2PFragment();
            actionA.setTitle("我要发送");
            actionB.setTitle("我要接收");

            //发送文件
            actionA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //打开文件管理器，获取要发送的文件的路径
                    //发送的启动工作交给onActivityResult
                    openFileManager();
                }
            });

            //接收文件
            actionB.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final ProgressDialog pds = new ProgressDialog(MainActivity.this);
                    final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                    pds.setTitle("提醒");
                    pds.setMessage("正在等待对方发送");
                    pds.show();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            P2PReceive p2pReceive = new P2PReceive(mainHandler, pds, pd, getApplicationContext());
                            p2pReceive.startDownload();
                        }
                    };
                    thread.start();

                }
            });
            fragmentTransaction.replace(R.id.content_main, p2pFragment);


        } else if (id == R.id.nav_csMode) {

        } else if (id == R.id.nav_advance) {

        } else if (id == R.id.nav_other) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        fragmentTransaction.commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    protected void startSend() {
        final ProgressDialog pds = new ProgressDialog(MainActivity.this);
        final ProgressDialog pd = new ProgressDialog(MainActivity.this);
        pds.setTitle("提醒");
        pds.setMessage("正在等待对方接收");

        Thread thread = new Thread() {
            @Override
            public void run() {
                P2PSend p2pSend = new P2PSend(mainHandler, pd, pds, getApplicationContext());
                p2pSend.startDownload(filePath);
            }
        };

        //显示pds，提示等待对方接受
        pds.show();

        thread.start();
    }
}
