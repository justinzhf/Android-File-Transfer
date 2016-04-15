package com.example.huaif.myfiletransfer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

/**
 * Created by huaif on 2016/4/15.
 */
public class P2PReceive {

    private String host = "192.168.43.1";
    private int port = 7777;
    private String fileDir = "/storage/sdcard0/myFileTransfer/receive";

    //更新UI的控件
    private EHandler mainHandler = null;
    private ProgressDialog pd = null;
    private ProgressDialog pds = null;
    private Context context = null;

    //每个分片的大小
    private int perChipSpace = 256;

    //报头信息
    private String ID = "021310429";
    private String name = "zhf";

    //每个分片中实际最多用来存放文件信息的字节数
    private int factSpace = perChipSpace - ID.getBytes().length - name.getBytes().length - 1 - 8;

    public P2PReceive(EHandler mainHandler, ProgressDialog pd, ProgressDialog pds, Context context) {
        this.mainHandler = mainHandler;
        this.pd = pd;
        this.pds = pds;
        this.context = context;
    }

    public void startDownload() {
        boolean connected = false;
        Socket s = null;
        String fileName = null, tempFileName = null;
        int tempInt = 0;
        FileOutputStream fos = null;
        DataInputStream dis = null;

        //服务器端为开启时，等待并不断尝试连接，直至连接到服务器
        while (!connected) {
            try {
                Thread.sleep(500);
                s = new Socket(host, port);
                connected = true;
            } catch (Exception e1) {
                connected = false;
            }
        }

        try {


            //连接上服务器，将pds撤销，并显示pd表示下载进度。
            pds.dismiss();
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setTitle("正在接收");
            UIWiget obj=new UIWiget(pd,pds);
            Message msg1 = mainHandler.obtainMessage(3, 1, 1, obj);
            mainHandler.sendMessage(msg1);

            dis = new DataInputStream(s.getInputStream());

            //生成临时文件，储存接收的数据。
            fileName = dis.readUTF();
            tempFileName = fileName + ".tmp";
            File file = new File(fileDir, tempFileName);

            int len = (int) dis.readLong();
            pd.setMax((len / factSpace) + 1);


            fos = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }

            byte[] receivedBytes = new byte[perChipSpace];
            int readLen;
            int receivedCount = 0;
            while ((readLen = dis.read(receivedBytes, 0, perChipSpace)) > 0) {

                //此处卡了我五天，注意server端写n次，但是本客户端并不是要n次才能读完，要大于n次，因此设置写入
                //长度为readLen。
                fos.write(receivedBytes, 0, readLen);
                fos.flush();
                receivedCount++;
                pd.setProgress(receivedCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                s.close();
                dis.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //将临时文件生成目标文件。
            makeFile(tempFileName, fileName);
        }
    }

    public byte check(byte[] bytes, int startPosition, int endPosition) {
        int sum = 0;
        for (int i = startPosition; i < endPosition; i++) {
            sum += bytes[i];
        }
        return intToByte(sum % 256)[0];
    }

    public byte[] intToByte(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff);// 最低八个位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低八个位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高八个位
        targets[3] = (byte) (res >>> 24);// 最高八个位,无符号右移。
        return targets;
    }

    public int byteToInt(byte[] res) {
// 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000

        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    public void makeFile(String sourFile, String destFile) {
        File sour = new File(fileDir, sourFile);
        File dest = new File(fileDir, destFile);

        FileInputStream sfis = null;
        FileOutputStream dfos = null;
        try {


            //如已经存在同名文件，则更改要生成的文件名（在后缀名前加“（1）”）。
            Integer si = 1;
            while (dest.exists()) {
                String[] temp = destFile.split("\\.");
                String fileName = temp[0] + "(" + si.toString() + ")." + temp[temp.length - 1];
                dest = new File(fileDir, fileName);
                si++;
            }

            sfis = new FileInputStream(sour);
            dfos = new FileOutputStream(dest);
            byte[] receivedBytes = new byte[perChipSpace], temp1 = new byte[4], temp2 = new byte[4];
            int start, end;
            while (sfis.read(receivedBytes, 0, perChipSpace) > 0) {
                if (receivedBytes[perChipSpace - 1] == check(receivedBytes, 0, perChipSpace - 1)) {
                    for (int i = 0; i < 4; i++) {
                        temp1[i] = receivedBytes[12 + i];
                        temp2[i] = receivedBytes[16 + i];
                    }
                    start = byteToInt(temp1);
                    end = byteToInt(temp2);
                    dfos.write(receivedBytes, 20, end - start + 1);
                    dfos.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sfis.close();
                dfos.close();
                sour.delete();
                pd.dismiss();

                //文件接收完成，弹出吐司
                Context obj = context;
                Message msg = mainHandler.obtainMessage(4, 1, 1, obj);
                mainHandler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}