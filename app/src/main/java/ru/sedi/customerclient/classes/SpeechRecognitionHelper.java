package ru.sedi.customerclient.classes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.List;

import ru.sedi.customer.R;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

/**
 * Created with IntelliJ IDEA.
 * User: Stalker
 * Date: 22.10.13
 * Time: 9:00
 * To onSuccessCalculate this template use File | Settings | File Templates.
 */
public class SpeechRecognitionHelper
{
    public static void run(Activity ownerActivity) {
        if (isSpeechRecognitionActivityPresented(ownerActivity)) {
            startRecognitionActivity(ownerActivity);
        } else {
            Toast.makeText(ownerActivity, R.string.voice_speech_activation_message, Toast.LENGTH_LONG).show();
            installGoogleVoiceSearch(ownerActivity);
        }
    }

    public static void run(Fragment ownerFragment) {
        if (isSpeechRecognitionActivityPresented(ownerFragment.getContext())) {
            startRecognitionActivity(ownerFragment);
        } else {
            Toast.makeText(ownerFragment.getContext(), R.string.voice_speech_activation_message, Toast.LENGTH_LONG).show();
            installGoogleVoiceSearch(ownerFragment.getActivity());
        }
    }

    private static boolean isSpeechRecognitionActivityPresented(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() != 0) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static void startRecognitionActivity(Activity ownerActivity) {
        Intent intent = getIntent();
        ownerActivity.startActivityForResult(intent, 1);
    }

    private static void startRecognitionActivity(Fragment ownerFragment) {
        Intent intent = getIntent();
        ownerFragment.startActivityForResult(intent, 1);
    }


    private static Intent getIntent() {
        String languagePref = Prefs.getString(PrefsName.LOCALE_CODE);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languagePref);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languagePref);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languagePref);
        return intent;
    }

    private static void installGoogleVoiceSearch(final Activity ownerActivity) {
        AlertDialog dialog = new AlertDialog.Builder(ownerActivity)
                .setMessage(R.string.voice_speech_activation_message)
                .setTitle(R.string.attention)
                .setPositiveButton(R.string.setup, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            ownerActivity.startActivity(intent);
                        } catch (Exception ex) {
                        }
                    }})
                .setNegativeButton(ownerActivity.getString(R.string.cancel), null)
                .create();
        dialog.show();
    }
}
