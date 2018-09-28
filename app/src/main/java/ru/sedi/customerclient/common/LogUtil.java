package ru.sedi.customerclient.common;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import ru.sedi.customerclient.classes.App;

public class LogUtil {

    private static String TAG = "Sedi_TaxiClient";
    private static boolean writeLogs;

    //region Log type
    public static final int INFO = 1;
    public static final int ERROR = 2;
    public static final int WARNING = 3;
    //endregion


    public LogUtil(boolean useLogs) {
        writeLogs = useLogs;
    }

    /**
     * Write logs with calling point.
     *
     * @param logType      - log type.
     * @param stringFormat - string format.
     * @param element      - object uses in string format.
     */
    public static void log(int logType, String stringFormat, Object... element) {
        if (!writeLogs)
            return;

        String point = getStackTrace();
        String message = (element == null || element.length < 1)
                ? stringFormat
                : String.format(stringFormat, element);
        message = point + " : " + message;
        switch (logType) {
            case INFO:
                Log.i(TAG, message);
                break;
            case ERROR:
                Log.e(TAG, message);
                writeToFile(message);
                break;
            case WARNING:
                Log.w(TAG, message);
        }
    }

    /**
     * Write OnFailureCalculate logs with stack trace and write in file.
     *
     * @param e - Exception
     */
    public static void log(Exception e) {

        //Get point and write Log.e
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        String point = String.format("[%s : %s : %d line]",
                stackTrace[1].getFileName(),
                stackTrace[1].getMethodName(),
                stackTrace[1].getLineNumber());

        if (writeLogs) {
            e.printStackTrace();
            Log.e(TAG, point + ":" + e.getMessage());
        }

        //Get string writer and write to file
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        writeToFile(sw.toString() + "\n------------------------------------------------------------------------------");
    }

    /**
     * Receiving call points
     */
    private static String getStackTrace() {
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        return String.format("[%s : %s : %d line]",
                stackTrace[2].getFileName(),
                stackTrace[2].getMethodName(),
                stackTrace[2].getLineNumber());
    }

    /**
     * Write the message to a file. The path: SDCard -> SeDiLogs -> file.txt
     *
     * @param message - message, OnFailureCalculate or other/
     */
    public static void writeToFile(final String message) {
        if (!writeLogs) return;

        try {
            File dir = new File(getLogFolderPath());
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(getLogFolderPath()
                    + String.format("%s.txt", DateTime.Now().toString("dd.MM.yyyy")));
            if (!file.exists())
                file.createNewFile();

            if (file.exists()) {
                FileWriter wfile = new FileWriter(file, true);
                wfile.append(DateTime.Now().toString("dd.MM.yyyy HH:mm") + " " + message + "\n");
                wfile.flush();
                wfile.close();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static String getLogFolderPath() {
        return Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + App.getInstance().getPackageName()
                + "/";
    }
}
