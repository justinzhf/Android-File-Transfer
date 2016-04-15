package com.example.huaif.myfiletransfer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.*;
import java.net.*;

/**
 * Created by huaif on 2016/4/15.
 */
public class P2PSend {

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

    private int port = 7777;

    //每个分片中实际最多用来存放文件信息的字节数
    private int factSpace = perChipSpace - ID.getBytes().length - name.getBytes().length - 1 - 8;

    public P2PSend(EHandler handler, ProgressDialog pd, ProgressDialog pds, Context context) {
        this.mainHandler = handler;
        this.pd = pd;
        this.pds = pds;
        this.context = context;
    }


    public void startDownload(String filename) {
        File file = new File(filename);
        byte[] sourceBytes = new byte[factSpace];
        byte[] destinationBytes;
        int readLength;
        int startPosition;
        int endPosition = -1;
        ServerSocket ss = null;
        Socket s = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;


        try {
            ss = new ServerSocket(port);
            s = ss.accept();

            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(file.getName());
            dos.flush();
            dos.writeLong(file.length());
            dos.flush();


            /*
             *接收方已经连接上并准备开始接收文件，将pds撤销，并显示pd
             * 表示发送进度
             */
            pds.dismiss();
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setTitle("正在发送");
            pd.setMax(((int) file.length() / factSpace) + 1);
            UIWiget obj = new UIWiget(pd, pds);
            Message msg = mainHandler.obtainMessage(1, 1, 1, obj);
            mainHandler.sendMessage(msg);

            fis = new FileInputStream(file);
            int sentCount = 0;
            while ((readLength = fis.read(sourceBytes, 0, factSpace)) > 0) {
                startPosition = endPosition + 1;
                endPosition = startPosition + readLength - 1;
                destinationBytes = slice(sourceBytes, startPosition, endPosition);
                dos.write(destinationBytes, 0, perChipSpace);
                dos.flush();
                sentCount++;
                pd.setProgress(sentCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                dos.close();
                s.close();
                ss.close();

                pd.dismiss();

                /*
                * 弹出Toast，提示文件发送完成.
                * */
                Context obj = context;
                Message msg = mainHandler.obtainMessage(2, 1, 1, obj);
                mainHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }

    public void startShitDownload(String filename,int selectedLen) {
        File file = new File(filename);
        byte[] sourceBytes = new byte[factSpace];
        byte[] destinationBytes;
        int readLength;
        int startPosition;
        int endPosition = -1;
        ServerSocket ss = null;
        Socket s = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;


        try {
            ss = new ServerSocket(port);
            s = ss.accept();

            dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(file.getName());
            dos.flush();
            dos.writeLong(file.length());
            dos.flush();


            /*
             *接收方已经连接上并准备开始接收文件，将pds撤销，并显示pd
             * 表示发送进度
             */
            pds.dismiss();
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setTitle("正在发送");
            pd.setMax(((int) file.length() / factSpace) + 1);
            UIWiget obj = new UIWiget(pd, pds);
            Message msg = mainHandler.obtainMessage(1, 1, 1, obj);
            mainHandler.sendMessage(msg);

            fis = new FileInputStream(file);
            int sentCount = 0;
            while ((readLength = fis.read(sourceBytes, 0, factSpace)) > 0&&sentCount<=selectedLen) {
                startPosition = endPosition + 1;
                endPosition = startPosition + readLength - 1;
                destinationBytes = slice(sourceBytes, startPosition, endPosition);
                dos.write(destinationBytes, 0, perChipSpace);
                dos.flush();
                sentCount++;
                pd.setProgress(sentCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                dos.close();
                s.close();
                ss.close();

                pd.dismiss();

                /*
                * 弹出Toast，提示文件发送完成.
                * */
                Context obj = context;
                Message msg = mainHandler.obtainMessage(2, 1, 1, obj);
                mainHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }


    public byte[] slice(byte[] bytes, int startPosition, int endPosition) {

        //container中实际用来存放文件内容的最多只有（perChipSpace-tempIDBytes.length-tempNameBytes.length-1）个字节
        byte[] container = new byte[perChipSpace];

        String ID = "021310429";
        String name = "zhf";

        byte[] tempNameBytes = name.getBytes();
        byte[] tempIDBytes = ID.getBytes();

        //将ID和name转换的数组赋值到分片中
        //其中ID占前9个字节，name占后3个字节
        for (int i = 0; i < tempIDBytes.length; i++) {
            container[i] = tempIDBytes[i];
        }

        for (int i = 0; i < tempNameBytes.length; i++) {
            container[i + tempIDBytes.length] = tempNameBytes[i];
        }

        //将分片在文件中的位置写到该分片的报头
        //起始位置占四个字节，结束位置占四个字节
        byte[] tempStartPosition = intToByte(startPosition);
        byte[] tempEndPosition = intToByte(endPosition);
        for (int i = 0; i < 4; i++) {
            container[tempIDBytes.length + tempNameBytes.length + i] = tempStartPosition[i];
            container[tempIDBytes.length + tempNameBytes.length + 4 + i] = tempEndPosition[i];
        }

        //将文件内容写到分片中，无法填满则用0填充
        int temp = 0;
        for (int i = tempIDBytes.length + tempNameBytes.length + 8; i < perChipSpace - 1; i++) {
            container[i] = bytes[temp];
            temp++;
        }


        //设置片的最后一位校验和
        container[perChipSpace - 1] = check(container, 0, perChipSpace - 1);
        return container;
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

}
