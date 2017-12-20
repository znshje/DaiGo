package com.jshaz.daigo.client;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jshaz.daigo.R;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends BaseActivity {

    private Spinner spinner;

    private EditText editText;

    private Button submit;

    private ToolBarView toolBarView;

    private String[] types = new String[] {"界面设计不好看", "功能建议", "Bug反馈", "其他"};

    private int select = 0;

    private ProgressDialog dialog;

    private Thread submitThread;

    private MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initView();

        initSpinner();
    }

    private void initView() {
        toolBarView = (ToolBarView) findViewById(R.id.report_toolbar);
        spinner = (Spinner) findViewById(R.id.report_type);
        editText = (EditText) findViewById(R.id.report_content);
        submit = (Button) findViewById(R.id.report_submit);

        setSlideExit(true);

        toolBarView.setTitleText("反馈");
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitReport();
            }
        });
    }

    private void initSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                select = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                select = 0;
                spinner.setSelection(0);
            }
        });
    }

    private void submitReport() {
        startDialog();
        final String title = types[select];
        final String content = editText.getText().toString();
        submitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String response = "";
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLReport);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("title", title));
                    params.add(new BasicNameValuePair("content", content));
                    /**
                     * 上传系统信息
                     */
                    params.add(new BasicNameValuePair("model", Build.MODEL));
                    params.add(new BasicNameValuePair("device", Build.DEVICE));
                    params.add(new BasicNameValuePair("sdk", Build.VERSION.SDK_INT + ""));

                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = handler.obtainMessage();
                        message.what = 0;
                        handler.handleMessage(message);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = 1;
                        handler.handleMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = 1;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        });
        submitThread.start();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (dialog != null && dialog.isShowing()) {
            /*正在登录*/
            submitThread.interrupt();
            stopDialog();
        } else {
            /*未在登录*/
            super.onBackPressed();
        }
    }

    private void startDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.setMessage("正在提交");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void stopDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }


    private static class MyHandler extends Handler {
        WeakReference<ReportActivity> activityWeakReference;

        public MyHandler(ReportActivity activity) {
            this.activityWeakReference = new WeakReference<ReportActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            activityWeakReference.get().stopDialog();
            switch (msg.what) {
                case 0:
                    Toast.makeText(activityWeakReference.get(), "提交成功", Toast.LENGTH_SHORT).show();
                    activityWeakReference.get().finish();
                    break;
                case 1:
                    Toast.makeText(activityWeakReference.get(), "网络错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


}
