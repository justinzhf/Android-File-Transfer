package com.example.huaif.myfiletransfer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by huaif on 2016/4/15.
 */
public class CSShare {
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
    private String sharedDir = "/storage/sdcard0/myFileTransfer";

    //每个分片中实际最多用来存放文件信息的字节数
    private int factSpace = perChipSpace - ID.getBytes().length - name.getBytes().length - 1 - 8;

    public CSShare(EHandler mainHandler, ProgressDialog pd, ProgressDialog pds, Context context) {
        this.mainHandler = mainHandler;
        this.pd = pd;
        this.pds = pds;
        this.context = context;
    }

    public void startDownload() {
        File file = null;
        byte[] sourceBytes = new byte[factSpace];
        byte[] destinationBytes;
        int readLength;
        int startPosition;
        int endPosition = -1;
        ServerSocket ss = null;
        Socket s = null,dlSocket=null;
        DataOutputStream dos = null;
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            ss = new ServerSocket(port);
            s = ss.accept();



            //服务器向客户端发送本机share文件夹下所有文件的文件名
            File tempFile = new File(sharedDir + "/share");
            File[] files = tempFile.listFiles();
            dos = new DataOutputStream(s.getOutputStream());
            for (int i = 0; i < files.length; i++) {
                dos.writeUTF(files[i].toString());
                dos.flush();
            }

            //表示本机已发送所有文件名
            dos.writeUTF("/END/");
            dos.flush();
            dos.close();
            s.close();

            //等待客户端选择文件下载

            dlSocket=ss.accept();
            dos=new DataOutputStream(dlSocket.getOutputStream());
            dis=new DataInputStream(dlSocket.getInputStream());
            file = new File(dis.readUTF());
            fis = new FileInputStream(file);
            dos.writeUTF(file.getName());
            dos.flush();
            dos.writeLong(file.length());
            dos.flush();

            //服务器已选择文件进行下载，撤销pds，pd显示进度
            pd.setTitle("正在传输");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(((int) file.length() / perChipSpace) + 1);
            UIWiget obj=new UIWiget(pd,pds);
            Message msg=mainHandler.obtainMessage(5,1,1,obj);
            pds.dismiss();
            mainHandler.sendMessage(msg);

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
                dis.close();
                fis.close();
                dos.close();
                s.close();
                dlSocket.close();
                ss.close();
                pd.dismiss();
                Message msg = mainHandler.obtainMessage(6, 1, 1, context);
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
        int containerTotalLength = tempIDBytes.length + tempNameBytes.length;

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


