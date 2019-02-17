package com.example.fitec.testegsm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

public class FileHelper
{
    private Context context;
    /** SD���Ƿ���� **/
    private boolean hasSD = false;
    /** SD����·�� **/
    private String SDPATH;
    /** ��ǰ�������·�� **/
    private String FILESPATH;

    public FileHelper(Context context)
    {
        this.context = context;
        hasSD = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        SDPATH = Environment.getExternalStorageDirectory().getPath();
        FILESPATH = this.context.getFilesDir().getPath();
    }

    /**
     * ��SD���ϴ����ļ�
     *
     * @throws IOException
     */
    public File createSDFile(String fileName) throws IOException
    {
        File file = new File(SDPATH + "//" + fileName);
        if (!file.exists())
        {
            file.mkdir();
            file.createNewFile();
        }
        return file;
    }

    /**
     * ɾ��SD���ϵ��ļ�
     *
     * @param fileName
     */
    public boolean deleteSDFile(String fileName)
    {
        File file = new File(SDPATH + "//" + fileName);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        return file.delete();
    }

    /**
     * д�����ݵ�SD���е�txt�ı��� strΪ����
     */
    public void writeSDFile(String str, String fileName)
    {
        try
        {
//			FileWriter fw = new FileWriter(SDPATH + "//" + fileName);
//			BufferedWriter bw = new BufferedWriter(fw);
            File f = new File(SDPATH + "//" + fileName);
//			bw.append("\n" + str);
            FileOutputStream os = new FileOutputStream(f,true);
            DataOutputStream out = new DataOutputStream(os);
            out.writeBytes("\n"+str);
            out.close();
            os.close();
//			bw.close();
//			os.write(str.getBytes());
//			os.close();
//			fw.close();
//			DataOutputStream out = new DataOutputStream(os);
//			out.writeShort(2);
//			out.writeUTF("");
//
//			fw.flush();
//			fw.close();

        } catch (Exception e)
        {
        }
    }

    /**
     * ��ȡSD�����ı��ļ�
     *
     * @param fileName
     * @return
     */
    public String readSDFile(String fileName)
    {
        StringBuffer sb = new StringBuffer();
        File file = new File(SDPATH + "//" + fileName);
        try
        {
            FileInputStream fis = new FileInputStream(file);
            int c;
            while ((c = fis.read()) != -1)
            {
                sb.append((char) c);
            }
            fis.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public String getFILESPATH()
    {
        return FILESPATH;
    }

    public String getSDPATH()
    {
        return SDPATH;
    }

    public boolean hasSD()
    {
        return hasSD;
    }
}
