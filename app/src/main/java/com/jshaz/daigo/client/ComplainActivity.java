package com.jshaz.daigo.client;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jshaz.daigo.R;
import com.jshaz.daigo.serverutil.ServerUtil;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComplainActivity extends AppCompatActivity implements View.OnClickListener{

    private ToolBarView toolBarView;

    private TextView orderID;
    private EditText content;
    private Button submit;

    private String orderId;

    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complain);

        initView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.complain_submit:
                if (content.getText().equals("")) {
                    Toast.makeText(this, "投诉内容请勿留空", Toast.LENGTH_SHORT).show();
                } else {
                    submitToServer();
                }
                break;
        }
    }

    private void initView() {
        toolBarView = (ToolBarView) findViewById(R.id.complain_toolbar);
        orderID = (TextView) findViewById(R.id.complain_order_id);
        content = (EditText) findViewById(R.id.complain_content);
        submit = (Button) findViewById(R.id.complain_submit);


        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolBarView.setTitleText("投诉");
        toolBarView.setTitleCampusVisible(false);

        submit.setOnClickListener(this);
        submit.setLongClickable(true);
        submit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        Intent intent = getIntent();
        orderId = intent.getStringExtra("order_id");
        orderID.setText(orderId);
    }

    private void submitToServer() {
        startProgressDialog();
        new Thread(new Runnable() {
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
                    HttpPost httpPost = new HttpPost(ServerUtil.SLComplain);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("orderid", orderId));
                    params.add(new BasicNameValuePair("content", content.getText().toString()));

                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200)//在5000毫秒之内接收到返回值
                    {
                        Message message = handler.obtainMessage();
                        message.what = 0;
                        handler.handleMessage(message);
                    } else {
                        Message message = handler.obtainMessage();
                        message.what = 1;
                        handler.handleMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = handler.obtainMessage();
                    message.what = 1;
                    handler.handleMessage(message);
                }
                Looper.loop();
            }
        }).start();
    }

    private void startProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("正在上传");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void stopProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            stopProgressDialog();
            switch (msg.what) {
                case 0:
                    AlertDialog.Builder builder = new AlertDialog.Builder(ComplainActivity.this);
                    builder.setMessage("投诉已提交。我们将会尽快处理您的投诉。");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                    break;
                case 1:
                    Toast.makeText(ComplainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
