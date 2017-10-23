package ru.sedi.customerclient.classes.Helpers;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;

public class FileSaveHelper {

    public static void Save(final Object object, final String fileName) {
        AsyncAction.run(() -> {
            try {

                Gson gson = new Gson();
                String s = gson.toJson(object);

                File dataDir = new File(BaseActivity.Instance.getFilesDir().getPath());
                if (!dataDir.exists())
                    dataDir.mkdirs();

                //Create temp file for a guaranteed saving file
                String tempFilename = fileName;
                tempFilename = "temp_" + tempFilename;

                File file = new File(BaseActivity.Instance.getFilesDir() + File.separator + tempFilename);
                FileOutputStream fos = new FileOutputStream(file);
                //byte[] serialize = TinyFormatter.Serialize(object);
                fos.write(s.getBytes());
                fos.close();

                //Hack for a guaranteed saving file
                file = new File(BaseActivity.Instance.getFilesDir() + File.separator + tempFilename);
                if (file.exists()) {
                    file.renameTo(new File(BaseActivity.Instance.getFilesDir() + File.separator + fileName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static Object Load(String fileName, Class<?> type) {
        File file = new File(BaseActivity.Instance.getFilesDir() + File.separator + fileName);
        if (!file.exists()){
            file = new File(BaseActivity.Instance.getFilesDir() + File.separator + "temp_" + fileName);
            if(!file.exists()) return null;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fis.close();
            return new Gson().fromJson(new String(buffer), SaveDataHelper.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
