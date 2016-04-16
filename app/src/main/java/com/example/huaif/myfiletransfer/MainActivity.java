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
import android.text.ClipboardManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentManager fragmentManager = getFragmentManager();
    private Looper mainLooper = Looper.getMainLooper();
    private EHandler mainHandler = new EHandler(mainLooper);
    private FloatingActionButton actionA, actionB;
    private FloatingActionsMenu menu;
    private String sharedDir = "/storage/sdcard0/AMyFileTransfer";

    private String filePath = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionA = (FloatingActionButton) findViewById(R.id.action_a);
        actionB = (FloatingActionButton) findViewById(R.id.action_b);
        menu = (FloatingActionsMenu) findViewById(R.id.fam);

        actionA.setColorNormalResId(R.color.pink);
        actionA.setColorPressedResId(R.color.pink_pressed);
        actionB.setColorNormalResId(R.color.colorPrimary);
        actionB.setColorPressedResId(R.color.colorPrimaryDark);
        menu.setEnabled(false);



        File file = new File(sharedDir);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        }
        File file1 = new File(sharedDir + "/share");
        if (!file1.exists() && !file1.isDirectory()) {
            file1.mkdir();
        }

        File file2 = new File(sharedDir + "/receive");
        if (!file2.exists() && !file2.isDirectory()) {
            file2.mkdir();
        }


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

            if (requestCode == 1) {
                startSend();
            } else if (requestCode == 2) {
                TextView tv_fileName = (TextView) findViewById(R.id.tv_selected_file);
                Button bt = (Button) findViewById(R.id.fuck_button);
                final File tempfile = new File(filePath);
                tv_fileName.setText(tempfile.getName() + "\n" + "文件片数：" + ((tempfile.length() / 235) + 1) + "\n");
                bt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startFuckSend((int) (tempfile.length() / 235) + 1);
                    }
                });
            } else if (requestCode == 3) {
                startAdvanceSend();
            }

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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        menu.setEnabled(true);
        TextView tv_init=(TextView)findViewById(R.id.tv_init);
        tv_init.setText("");

        if (id == R.id.nav_p2pMode) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            menu.collapse();
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
                            P2PReceive p2pReceive = new P2PReceive(mainHandler, pd, pds, getApplicationContext());
                            p2pReceive.startDownload();
                        }
                    };
                    thread.start();

                }
            });
            fragmentTransaction.replace(R.id.content_main, p2pFragment);
            fragmentTransaction.commit();


        } else if (id == R.id.nav_csMode) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            menu.collapse();
            CSFragment csFragment = new CSFragment();
            actionA.setTitle("我要分享");
            actionB.setTitle("我要下载");
            actionA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                    final ProgressDialog pds = new ProgressDialog(MainActivity.this);
                    pds.setTitle("提醒");
                    pds.setMessage("正在等待对方下载");
                    pds.show();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            CSShare csShare = new CSShare(mainHandler, pd, pds, getApplicationContext());
                            csShare.startDownload();
                        }
                    };
                    thread.start();
                }
            });
            actionB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                    final ProgressDialog pds = new ProgressDialog(MainActivity.this);
                    pds.setTitle("提醒");
                    pds.setMessage("正在等待对方分享");
                    pds.show();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            CSDownload csDownload = new CSDownload(mainHandler, pd, pds, getApplicationContext());
                            ArrayList<String> fileNames = csDownload.getFilesName();
                            Intent intent = new Intent(MainActivity.this, ShowFilesActivity.class);
                            intent.putStringArrayListExtra("fileNames", fileNames);
                            startActivity(intent);
                        }
                    };
                    thread.start();
                }
            });
            fragmentTransaction.replace(R.id.content_main, csFragment);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_advance) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            menu.collapse();
            AdvanceFragment aft = new AdvanceFragment();
            actionA.setTitle("我要发送");
            actionB.setTitle("我要接收");
            //发送文件
            actionA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //打开文件管理器，获取要发送的文件的路径
                    //发送的启动工作交给onActivityResult
                    openAdvanceFileManager();
                }
            });

            //接收文件
            actionB.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final ProgressDialog pds = new ProgressDialog(MainActivity.this);
                    final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                    EditText et_ip = (EditText) findViewById(R.id.et_input_ip);
                    final String hostIp = et_ip.getText().toString();
                    if (hostIp.equals("")) {
                        Toast.makeText(MainActivity.this,"请输入发送方IP",Toast.LENGTH_LONG).show();
                        return;
                    }
                    pds.setTitle("提醒");
                    pds.setMessage("正在等待对方发送");
                    pds.show();
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            P2PReceive p2pReceive = new P2PReceive(hostIp, mainHandler, pd, pds, getApplicationContext());
                            p2pReceive.startDownload();
                        }
                    };
                    thread.start();

                }
            });
            fragmentTransaction.replace(R.id.content_main, aft);
            fragmentTransaction.commit();
        } else if (id == R.id.nav_other) {

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            menu.collapse();
            OtherFragment otf = new OtherFragment();
            actionA.setTitle("我要发送");
            actionB.setTitle("我要接收");

            //发送文件
            actionA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //打开文件管理器，获取要发送的文件的路径
                    //发送的启动工作交给onActivityResult
                    openShitFileManager();
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
                            P2PReceive p2pReceive = new P2PReceive(mainHandler, pd, pds, getApplicationContext());
                            p2pReceive.startDownload();
                        }
                    };
                    thread.start();

                }
            });
            fragmentTransaction.replace(R.id.content_main, otf);
            fragmentTransaction.commit();


        }
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

    protected void openShitFileManager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 2);
    }

    protected void openAdvanceFileManager() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 3);
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

    protected void startFuckSend(int fileLen) {
        EditText et = (EditText) findViewById(R.id.ed_input_num);
        final int inputNum = Integer.parseInt(et.getText().toString());
        if (inputNum == 0 || et.getText().toString() == "" || inputNum > fileLen) {
            Toast.makeText(MainActivity.this, "不合法的输入！", Toast.LENGTH_LONG).show();
            return;
        }
        final ProgressDialog pds = new ProgressDialog(MainActivity.this);
        final ProgressDialog pd = new ProgressDialog(MainActivity.this);
        pds.setTitle("提醒");
        pds.setMessage("正在等待对方接收");
        Thread thread = new Thread() {
            @Override
            public void run() {
                P2PSend p2pSend = new P2PSend(mainHandler, pd, pds, getApplicationContext());
                p2pSend.startShitDownload(filePath, inputNum);
            }
        };

        //显示pds，提示等待对方接受
        pds.show();

        thread.start();
    }

    protected void startAdvanceSend() {
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
