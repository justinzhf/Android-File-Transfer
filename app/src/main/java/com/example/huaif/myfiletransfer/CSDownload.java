package com.example.huaif.myfiletransfer;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by huaif on 2016/4/15.
 */
public class CSDownload {
    private String host = "192.168.43.1";
    private int port = 7777;
    private String fileDir = "/storage/sdcard0/myFileTransfer/receive";

    //更新UI的控件
    private EHandler mainHandler = null;
    private ProgressDialog pd = null;
    private ProgressDialog pds = null;
    private Context context = null;
    ArrayList<String> fileNames = new ArrayList<>();
    private FragmentManager fm = null;
    private String selectedFile = null;

    //每个分片的大小
    private int perChipSpace = 256;

    //报头信息
    private String ID = "021310429";
    private String name = "zhf";

    //每个分片中实际最多用来存放文件信息的字节数
    private int factSpace = perChipSpace - ID.getBytes().length - name.getBytes().length - 1 - 8;


    public CSDownload(EHandler mainHandler, ProgressDialog pd, ProgressDialog pds, Context context) {
        this.mainHandler = mainHandler;
        this.pd = pd;
        this.pds = pds;
        this.context = context;
    }

    public CSDownload(EHandler mainHandler, ProgressDialog pd, ProgressDialog pds, Context context, FragmentManager fm) {
        this.mainHandler = mainHandler;
        this.pd = pd;
        this.pds = pds;
        this.context = context;
        this.fm = fm;
    }

    public ArrayList<String> getFilesName() {

        boolean connected = false;
        Socket s = null;
        DataInputStream dis = null;
        while (!connected) {
            try {
                Thread.sleep(500);
                s = new Socket(host, port);
                connected = true;
            } catch (Exception e1) {
                e1.printStackTrace();
                connected = false;
            }
        }
        try {

            //已连接上服务器，撤销pds，生成ListView显示服务器端文件
            pds.dismiss();
            dis = new DataInputStream(s.getInputStream());
            String temp = null;
            while (true) {
                try {
                    temp = dis.readUTF();
                    if (temp.equals("/END/")) {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                fileNames.add(temp);
            }

            //将接受到的文件信息显示出来
      /*      UIWiget obj = new UIWiget(context, fileNames, fm);
            Message msg = mainHandler.obtainMessage(7, 1, 1, obj);
            mainHandler.sendMessage(msg);
*/
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                s.close();
                dis.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            return fileNames;
        }
    }

    public void startDownload(String fileName) {
        boolean connected = false;
        Socket s = null;
        String tempFileName = null;
        int tempInt = 0;
        FileOutputStream fos = null;
        DataInputStream dis = null;
        DataOutputStream dos=null;
        while (!connected) {
            try {
                Thread.sleep(500);
                s = new Socket(host, port);
                connected = true;
            } catch (Exception e1) {
                e1.printStackTrace();
                connected = false;
            }
        }
        try {

            dos=new DataOutputStream(s.getOutputStream());
            dos.writeUTF(fileName);
            dos.flush();
            byte[] receivedBytes = new byte[perChipSpace];
            dis = new DataInputStream(s.getInputStream());
            fileName = dis.readUTF();
            tempFileName = fileName + ".tmp";
            File file = new File(fileDir, tempFileName);
            int len = (int) dis.readLong();
            System.out.println("====================len" + len);
            int totalChipsCount = (len / perChipSpace) + 1;//总长度除以实际长度，的片数
            pd.setMax(totalChipsCount);

            fos = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }

            int readLen = 0;
            int receivedCount=0;
            while ((readLen = dis.read(receivedBytes, 0, perChipSpace)) > 0) {

                //此处卡了我五天，注意server端写n次，但是本客户端并不是要n次才能读完，要大于n次
                fos.write(receivedBytes, 0, readLen);
                fos.flush();
                receivedCount++;
                pd.setProgress(receivedCount);
                tempInt++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                dos.close();
                s.close();
                dis.close();
                fos.close();
                makeFile(tempFileName, fileName);
                Message msg=mainHandler.obtainMessage(8,1,1,context);
                mainHandler.sendMessage(msg);
                pd.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void setSelectedFile(String selectedFile) {
        this.selectedFile = selectedFile;
    }
}
