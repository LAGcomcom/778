package com.example.smsdemo;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvSms, tvDes;
    private Button btnSms_view, btnSms_backup, btnSaveSettings;
    private android.widget.EditText etUploadUrl, etPhone;
    private String text = "";
    private List<SmsInfo> smsInfos;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSms = (TextView) findViewById(R.id.tv_sms);
        tvDes = (TextView) findViewById(R.id.tv_des);
        etUploadUrl = (android.widget.EditText) findViewById(R.id.et_upload_url);
        etPhone = (android.widget.EditText) findViewById(R.id.et_phone);
        btnSaveSettings = (Button) findViewById(R.id.btn_save_settings);
        smsInfos = new ArrayList<SmsInfo>();
        String savedUrl = ConfigManager.getUploadUrl(this);
        String savedPhone = ConfigManager.getPhoneNumber(this);
        if (savedUrl != null) etUploadUrl.setText(savedUrl);
        if (savedPhone != null) etPhone.setText(savedPhone);
        maybeStartService();
        // 查看短消息
        btnSms_view = (Button) findViewById(R.id.btn_sms_view);
        btnSms_view.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                askGetSMSPermission(); // 调用方法，申请读权限
            }
        });
        // 备份短消息
        btnSms_backup = (Button) findViewById(R.id.btn_sms_backup);
        btnSms_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSmsToSdCard(); // 备份短消息到外部存储私有目录的自定义文件夹中
            }
        });

        btnSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etUploadUrl.getText() == null ? "" : etUploadUrl.getText().toString();
                String phone = etPhone.getText() == null ? "" : etPhone.getText().toString();
                ConfigManager.setUploadUrl(MainActivity.this, url);
                ConfigManager.setPhoneNumber(MainActivity.this, phone);
                Toast.makeText(MainActivity.this, "设置已保存", Toast.LENGTH_SHORT).show();
                maybeStartService();
            }
        });
    }

    private void maybeStartService() {
        String url = ConfigManager.getUploadUrl(this);
        String phone = ConfigManager.getPhoneNumber(this);
        if (url != null && url.length() > 0 && phone != null && phone.length() > 0) {
            android.content.Intent i = new android.content.Intent(this, SmsForegroundService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(i);
            } else {
                startService(i);
            }
        }
    }

    // 动态权限申请方法,读取短消息
    private void askGetSMSPermission() {
        // 权限列表
        String[] PERMISSIONS_STORAGE = {android.Manifest.permission.READ_CONTACTS};
        // 动态申请权限
        int permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS}, 1);
        } else {
            Log.e(TAG, "已申请读取短消息权限");
            getSms();
        }
    }

    // 运行时权限处理的回调，处理用户授权结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSms();
                } else {
                    Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    // 读取短消息并显示
    public void getSms() {
        Uri uri = Uri.parse("content://sms/");  //获取系统信息的 uri
        // 获取 ContentResolver 对象
        ContentResolver resolver = getContentResolver();
        // 通过 ContentResolver 对象查询系统短信
        Cursor cursor = resolver.query(uri, new String[]{"_id", "address", "body", "type", "date"}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            tvDes.setVisibility(View.VISIBLE);
            if (smsInfos != null) smsInfos.clear(); //清除集合中的数据
            text = ""; // 清空 text 中原有的数据
            while (cursor.moveToNext()) {
                int _id = cursor.getInt(0);
                String address = cursor.getString(1);
                String body = cursor.getString(2);
                int type = cursor.getInt(3);
                long date = cursor.getLong(4);
                SmsInfo smsInfo = new SmsInfo(_id, address, body, type, date);
                smsInfos.add(smsInfo);
            }
            cursor.close();
        }
        // 将查询到的短信内容显示到界面上
        for (int i = 0; i < smsInfos.size(); i++) {
            text += "手机号码：" + smsInfos.get(i).getAddress() + "\n";
            text += "短信内容：" + smsInfos.get(i).getBody() + "\n";
            text += "短信类型：" + smsInfos.get(i).getFormatType() + "\n";
            text += "短信发送时间：" + smsInfos.get(i).getFormatDate() + "\n";
            text += "------------------------------\n";
        }
        tvSms.setText(text);
        tvSms.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void saveSmsToSdCard() {
        if (smsInfos.isEmpty()) {
            Toast.makeText(this, "暂无需备份的短信数据", Toast.LENGTH_SHORT).show();
            return;
        }
        //获得一个序列化对象
        XmlSerializer xmlSerializer = Xml.newSerializer();
        //将生成的 xml文件保存到sd 卡中名字为"sms.xml"
        File file = new File(PathUtils.INSTANCE.concatFilePath(PathUtils.INSTANCE.getAppPath(), "sms.xml"));
        FileOutputStream fos;

        try {
            fos = new FileOutputStream(file);
            xmlSerializer.setOutput(fos, "utf-8");
            xmlSerializer.startDocument("utf-8", true);
            xmlSerializer.startTag(null, "SmsData");

            xmlSerializer.startTag(null, "SmsLength");
            xmlSerializer.text(smsInfos.size() + "");
            xmlSerializer.endTag(null, "SmsLength");

            for (SmsInfo smsData : smsInfos) {
                xmlSerializer.startTag(null, "sms");
                xmlSerializer.startTag(null, "_id");
                xmlSerializer.text(smsData.get_id() + "");
                xmlSerializer.endTag(null, "_id");

                xmlSerializer.startTag(null, "type");
                xmlSerializer.text(smsData.getFormatType());
                xmlSerializer.endTag(null, "type");

                xmlSerializer.startTag(null, "address");
                xmlSerializer.text(smsData.getAddress());
                xmlSerializer.endTag(null, "address");

                xmlSerializer.startTag(null, "body");
                xmlSerializer.text(smsData.getBody());
                xmlSerializer.endTag(null, "body");

                xmlSerializer.startTag(null, "date");
                xmlSerializer.text(smsData.getFormatDate());
                xmlSerializer.endTag(null, "date");

                xmlSerializer.endTag(null, "sms");
            }
            xmlSerializer.endTag(null, "SmsData");
            xmlSerializer.endDocument();

            fos.flush();
            fos.close();
            Toast.makeText(this, "备份完成", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
