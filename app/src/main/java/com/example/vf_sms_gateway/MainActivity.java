package com.example.vf_sms_gateway;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;

    private TextView tvToken, tvStatus;
    private Button btnCopy;
    private View statusIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvToken = findViewById(R.id.tvToken);
        tvStatus = findViewById(R.id.tvStatus);
        btnCopy = findViewById(R.id.btnCopy);
        statusIndicator = findViewById(R.id.statusIndicator);

        String uniqueToken = getOrGenerateToken();
        tvToken.setText(uniqueToken);

        btnCopy.setOnClickListener(v -> requestSmsPermission());

        updateStatus();
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
        } else {
            copyTokenToClipboard();
            updateStatus();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "تم منح صلاحية الوصول للرسائل!", Toast.LENGTH_SHORT).show();
                copyTokenToClipboard();
            } else {
                Toast.makeText(this, "التطبيق لن يعمل بدون صلاحية قراءة الرسائل.", Toast.LENGTH_LONG).show();
            }
            updateStatus();
        }
    }

    private void copyTokenToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("App Token", tvToken.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "تم نسخ التوكن بنجاح!", Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateStatus() {
        if (isNetworkConnected() && hasSmsPermission()) {
            statusIndicator.setBackgroundResource(R.drawable.status_circle_green);
            tvStatus.setText("متصل وجاهز لاستقبال الرسائل");
        } else {
            statusIndicator.setBackgroundResource(R.drawable.status_circle_red);
            String statusText = "";
            if (!isNetworkConnected()) statusText += "الاتصال بالإنترنت مقطوع. ";
            if (!hasSmsPermission()) statusText += "صلاحية الرسائل غير ممنوحة.";
            tvStatus.setText(statusText);
        }
    }

    private String getOrGenerateToken() {
        android.content.SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String token = prefs.getString("device_token", null);
        if (token == null) {
            token = UUID.randomUUID().toString();
            prefs.edit().putString("device_token", token).apply();
        }
        return token;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}
