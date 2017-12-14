package com.jshaz.daigo.client;

/**
 * 发布订单页面
 */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.jshaz.daigo.LoginActivity;
import com.jshaz.daigo.R;
import com.jshaz.daigo.broadcasts.NetWorkStateReceiver;
import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ToolBarView;
import com.jshaz.daigo.util.Order;
import com.jshaz.daigo.util.Setting;
import com.jshaz.daigo.util.Utility;

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

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class AddOrderActivity extends BaseActivity {

    private List<String> typeList = new ArrayList<String>();
    private List<String> campusList = new ArrayList<String>();
    private List<String> timeList = new ArrayList<String>();

    private Spinner typeChoose;
    private Spinner campusChoose;
    private Spinner timeChoose;
    private Button submitOrder;
    private EditText publicContent;
    private EditText privateContent;
    private EditText contact;
    private EditText price;

    private ArrayAdapter<String> typeAdapter;
    private ArrayAdapter<String> campusAdapter;
    private ArrayAdapter<String> timeAdapter;

    private String orderType;
    private int campusCode;
    private int requestTimeIndex;

    private ToolBarView toolBarView;

    public static final String[] TYPE_NAME = new String[] {"代购", "取快递", "搬运和送物品"};
    public static final String[] TIME_LIST = new String[] {"20", "30", "40", "60", "90", "120"};

    public static final int TYPE_PURCHASE = 0;
    public static final int TYPE_EXPRESS = 1;
    public static final int TYPE_CARRY = 2;
    public static final long[] TIME_MILLIS = new long[] {20 * 60 * 1000, 30 * 60 * 1000,
        40 * 60 * 1000, 60 * 60 * 1000, 90 * 60 * 1000, 120 * 60 * 1000};

    private Thread addThread;

    private Setting setting;

    private ProgressDialog progressDialog;
    private AlertDialog.Builder timeEnsureDialog;

    private NetWorkStateReceiver receiver;

    private UserIntent userIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);

        //获取当前用户实例
        userIntent = (UserIntent) getIntent().getSerializableExtra("user");

        typeChoose = (Spinner) findViewById(R.id.addorder_choose_type);
        campusChoose = (Spinner) findViewById(R.id.addorder_choose_campus);
        timeChoose = (Spinner) findViewById(R.id.addorder_choose_time);
        toolBarView = (ToolBarView) findViewById(R.id.addorder_toolbar);
        submitOrder = (Button) findViewById(R.id.addorder_submit);
        publicContent = (EditText) findViewById(R.id.addorder_public_content);
        privateContent = (EditText) findViewById(R.id.addorder_private_content);
        contact = (EditText) findViewById(R.id.addorder_contact);
        price = (EditText) findViewById(R.id.addorder_price);

        /*获取设置对象*/
        setting = new Setting(this);
        setting.readFromLocalSharedPref();
        campusCode = setting.getCampusCode();

        /*设置标题*/
        toolBarView.setTitleText("发布订单");
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /*选择类型的Spinner添加选项*/
        typeList.add(TYPE_NAME[TYPE_PURCHASE]);
        typeList.add(TYPE_NAME[TYPE_EXPRESS]);
        typeList.add(TYPE_NAME[TYPE_CARRY]);

        receiver = new NetWorkStateReceiver(this);
        /*选择校区的Spinner添加选项*/
        for (int i = 0; i < 8; i++) {
            campusList.add(Setting.getCampusName(i));
        }
        /*选择递送时间的Spinner添加选项*/
        for (int i = 0; i < 6; i++) {
            timeList.add(TIME_LIST[i] + " 分钟");
        }

        /*添加默认联系方式*/
        contact.setText(userIntent.getPhoneNum());
        publicContent.setText(userIntent.getDefaultAddress());

        /*设置Spinner的Adapter和选项监听事件*/
        typeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeChoose.setAdapter(typeAdapter);
        typeChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                orderType = typeAdapter.getItem(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                typeChoose.setSelection(0);
                orderType = TYPE_NAME[TYPE_PURCHASE];
            }
        });

        campusAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                campusList);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campusChoose.setAdapter(campusAdapter);
        campusChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                campusCode = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        campusChoose.setSelection(campusCode);

        timeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                timeList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeChoose.setAdapter(timeAdapter);
        timeChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                requestTimeIndex = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                requestTimeIndex = 0;
                timeChoose.setSelection(0);
            }
        });
        /*---------------------------------------------*/
        /*
        * 设置金额框的监听器，补齐金额格式*/
        price.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    checkPrice();
                }
            }
        });

        submitOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPrice();
                if (publicContent.getText().toString().isEmpty()) {
                    Toast.makeText(AddOrderActivity.this, "有内容未填写", Toast.LENGTH_SHORT).show();
                    publicContent.requestFocus();
                } else if (contact.getText().toString().isEmpty()) {
                    Toast.makeText(AddOrderActivity.this, "有内容未填写", Toast.LENGTH_SHORT).show();
                    contact.requestFocus();
                } else if (price.getText().toString().isEmpty()) {
                    Toast.makeText(AddOrderActivity.this, "有内容未填写", Toast.LENGTH_SHORT).show();
                    price.requestFocus();
                } else {
                    startSubmissionDialog();
                    getNetTime();
                }
            }
        });
    }

    private void checkPrice() {
        if (!TextUtils.isEmpty(price.getText().toString())) {
            String p = price.getText().toString();
            if (!p.contains(".")) {
                        /*没有小数点*/
                double amount = Double.parseDouble(p);
                if (amount > 100) {
                    price.setText("100.00");
                } else if (amount < 1) {
                    price.setText("1.00");
                } else {
                    price.setText(p + ".00");
                }
                p = price.getText().toString();
                            /*去除前导零*/
                while (p.charAt(0) == '0') {
                    String tmp = p.substring(1, p.length());
                    p = tmp;
                }
                price.setText(p);
            } else {
                if (p.indexOf(".") != p.lastIndexOf(".")) {
                            /*出现多个小数点*/
                    price.setText(p.substring(0, p.indexOf(".")) + ".00");
                } else {
                            /*只有一个小数点*/
                    double amount = 1.00;

                            /*输入不合法*/
                    if (p.charAt(0) < '0' || p.charAt(0) > '9') {
                        price.setText("1.00");
                    }
                            /*重新获取正确的输入*/
                    p = price.getText().toString();
                    amount = Double.parseDouble(p);

                    if (p.substring( p.indexOf(".") + 1 ).length() == 1) {
                        price.setText(p + "0");
                    } else if (p.substring( p.indexOf(".") + 1 ).length() == 0) {
                        price.setText(p + "00");
                    } else if (p.substring( p.indexOf(".") + 1 ).length() > 2) {
                        price.setText(p.substring(0, p.indexOf(".") + 3));
                    }
                    if (amount > 100) {
                        price.setText("100.00");
                    } else if (amount < 1) {
                        price.setText("1.00");
                    }

                    p = price.getText().toString();
                            /*去除前导零*/
                    while (p.charAt(0) == '0') {
                        String tmp = p.substring(1, p.length());
                        p = tmp;
                    }
                    price.setText(p);

                }
            }
        }
    }

    /**
     * 创建订单逻辑
     * 获取网络时间
     */
    private void getNetTime() {
        if (receiver.isNetWorkAvailable()) {
            String netTime = "";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    Message msg = mHandler.obtainMessage();
                    String result = "";
                    try {
                        URL url = new URL("https://www.baidu.com");
                        URLConnection uc = url.openConnection();
                        uc.connect();
                        long date = uc.getDate();
                        //result = new SimpleDateFormat("y年M月d日 HH:mm:ss").format(new Date(date));
                        msg.what = 0;
                        msg.obj = date;
                        mHandler.handleMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg.what = 1;
                        mHandler.handleMessage(msg);
                    }
                    Looper.loop();
                }
            }).start();
        }

    }


    /**
     * 数据库模块
     */
    //数据库模块
    public String response = "";

    public void addOrderToDatabase(final String orderId, final String title,final int campusCode, final String requestTime,
                                   final String publicContent, final String privateContent,
                                   final String contact, final String price) {
        startSubmissionDialog();
        addThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLAddOrder);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list
                    params.add(new BasicNameValuePair("orderid", orderId));
                    params.add(new BasicNameValuePair("title", title));
                    params.add(new BasicNameValuePair("campusid", campusCode + ""));
                    params.add(new BasicNameValuePair("requesttime", requestTime));
                    params.add(new BasicNameValuePair("publiccontent", publicContent));
                    params.add(new BasicNameValuePair("privatecontent", privateContent));
                    params.add(new BasicNameValuePair("contact", contact));
                    params.add(new BasicNameValuePair("price", price));
                    params.add(new BasicNameValuePair("senderid", userIntent.getUserId()));
                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if(httpResponse.getStatusLine().getStatusCode()==200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = mHandler.obtainMessage();
                        message.what = 2;
                        message.obj = response;
                        mHandler.handleMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = mHandler.obtainMessage();
                    message.what = 3;
                    mHandler.handleMessage(message);
                }
                Looper.loop();

            }

        });
        addThread.start();
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Order order = new Order(setting.getCurUser().toDAO(),
                            orderType,
                            publicContent.getText().toString(),
                            privateContent.getText().toString(),
                            setting.getCampusCode());
                    stopSubmissionDialog();
                    startTimeEnsureDialog(requestTimeIndex, (long) msg.obj);
                    break;
                case 1:
                    Toast.makeText(AddOrderActivity.this,
                            "网络连接错误", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    //网络正常
                    String response = (String) msg.obj;
                    if (response.equals("false")) {
                        Toast.makeText(AddOrderActivity.this,"发布失败",Toast.LENGTH_SHORT).show();
                        stopSubmissionDialog();
                    } else {
                        stopSubmissionDialog();
                        Toast.makeText(AddOrderActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case 3:
                    //连接超时
                    Toast.makeText(AddOrderActivity.this,"连接超时",Toast.LENGTH_SHORT).show();
                    stopSubmissionDialog();
                    break;
            }
        }
    };

    /**
     * 创建订单逻辑
     * 开启正在获取信息的dialog
     */
    private void startSubmissionDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("发布订单");
        progressDialog.setMessage("正在获取信息");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * 关闭正在获取信息的dialog
     */
    private void stopSubmissionDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void startTimeEnsureDialog(final int timeIndex, final long netTime) {
        timeEnsureDialog = new AlertDialog.Builder(this);
        timeEnsureDialog.setMessage("您的订单预计接单后" + TIME_LIST[timeIndex] + "分钟内送达");
        timeEnsureDialog.setPositiveButton("确定提交", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO:创建订单号，生成订单对象，提交服务器
                String orderId = Utility.convertMillToDate(netTime, 2) +
                        campusCode + userIntent.getUserId();
                Log.d("order", orderId);
                addOrderToDatabase(orderId, orderType, campusCode, TIME_MILLIS[timeIndex] + "",
                        publicContent.getText().toString(), privateContent.getText().toString(),
                        contact.getText().toString(), price.getText().toString());
            }
        });
        timeEnsureDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        timeEnsureDialog.show();
    }
}
