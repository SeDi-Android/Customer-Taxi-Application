package ru.sedi.customerclient.activitys.move_activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import ru.sedi.customer.R;

public class MoveVersionActivity extends AppCompatActivity {

    public final String NEW_APP_PACKAGE = "ru.sedi.customer.klass";
    public final String MESSAGE = "Здравствуйте. Для дальнейшей работы этого приложения его необходимо обновить - для этого перейдите на Google Play.";

    public static Intent getIntent(Context context) {
        return new Intent(context, MoveVersionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_version);

        hideActionBar();

        View.OnClickListener clickListener = view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + NEW_APP_PACKAGE)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + NEW_APP_PACKAGE)));
            }
        };

        findViewById(R.id.iv_go_on_google_play).setOnClickListener(clickListener);
        findViewById(R.id.btn_go_on_play).setOnClickListener(clickListener);
        ((TextView)findViewById(R.id.tv_message)).setText(MESSAGE);
    }

    private void hideActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
    }


}
