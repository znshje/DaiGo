package com.jshaz.daigo;
/**
 * 修改个人资料的活动
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jshaz.daigo.intents.UserIntent;
import com.jshaz.daigo.serverutil.ServerUtil;
import com.jshaz.daigo.ui.BaseActivity;
import com.jshaz.daigo.ui.ComplexButton;
import com.jshaz.daigo.ui.PopUtil;
import com.jshaz.daigo.ui.ToolBarView;
import com.jshaz.daigo.util.User;
import com.jshaz.daigo.util.Utility;
import com.yalantis.ucrop.UCrop;

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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ModifyInfoActivity extends BaseActivity implements View.OnClickListener{

    private LinearLayout layoutPopUp;

    private ToolBarView toolBarView;

    private ProgressDialog loadingDialog;

    private PopUtil pop;

    private View view;

    private Uri imageUri;
    private Uri imageCroppedUri;

    private File outputImage;
    private File croppedImage;

    private Bitmap finalHeadBitmap;

    private EditText ETNickName;
    private EditText ETAddress;

    private ComplexButton CBModifyHead;
    private ComplexButton CBVerification;
    private ComplexButton CBPassword;
    private ComplexButton CBPhoneNum;

    private Thread updateThread;

    public static final int CAMERA = 0;
    public static final int GALLARY = 1;
    public static final int RESET_PASSWORD = 2;
    public static final int RESET_PHONE = 3;
    public static final int VERIFICATION = 4;

    private User user;

    private ProgressDialog progressDialog;

    private MyHandler mHandler = new MyHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.activity_modify_info, null);
        setContentView(view);

        //初始化View
        //注意：一定要最先初始化View
        initView();

        //获得用户完整信息
        getUserInfo();

        //发起权限请求
        requestPermission();

        //初始化图片文件路径
        initImageFile();

        //初始化弹窗
        initPop();

    }

    /**
     * 获得用户完整信息
     */
    private void getUserInfo() {
        Intent intent = getIntent();
        user = new User(this);
        user.readFromLocalDatabase();
        user.cloneData(mHandler);
    }

    /**
     * 填充用户信息
     */
    private void fillUserInfo() {
        CBModifyHead.setImageBitmap(Utility.convertStringToBitmap(user.getHeadIcon()));
        finalHeadBitmap = Utility.convertStringToBitmap(user.getHeadIcon());
        String phoneT = user.getPhoneNum();
        CBPhoneNum.setDetail(phoneT.substring(0, 3) + "****" + phoneT.substring(7));
        ETNickName.setText(user.getNickName());
        ETAddress.setText(user.getDefaultAddress());
        if (user.isVerified()) {
            CBVerification.setDetail("已认证");
            CBVerification.setClickable(false);
        } else {
            CBVerification.setDetail("未认证");
            CBVerification.setClickable(true);
            CBVerification.setButtonOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ModifyInfoActivity.this, VerificationActivity.class);
                    UserIntent userIntent = new UserIntent();
                    userIntent.setUserId(user.getUserId());
                    intent.putExtra("user", userIntent);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * 初始化View
     */
    private void initView() {
        startProgressDialog();

        toolBarView = (ToolBarView) findViewById(R.id.clip_image_toolbar);
        CBModifyHead = (ComplexButton) findViewById(R.id.cb_modify_head);
        CBPassword = (ComplexButton) findViewById(R.id.modif_password);
        CBVerification = (ComplexButton) findViewById(R.id.modify_verification);
        ETNickName = (EditText) findViewById(R.id.modify_nick_name);
        CBPhoneNum = (ComplexButton) findViewById(R.id.modify_phone_num);
        ETAddress = (EditText) findViewById(R.id.modify_address);

        /*不可滑动退出*/
        setSlideExit(true);
        /*设置标题栏相关*/
        toolBarView.setTitleText("修改个人信息");
        toolBarView.setBackButtonVisible(true);
        toolBarView.setBackButtonImage(R.mipmap.icon_back);
        toolBarView.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolBarView.setRightButtonImage(R.mipmap.icon_done);
        toolBarView.setRightButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ETNickName.getText().toString().equals("")) {
                    updateToServer();
                } else {
                    Toast.makeText(ModifyInfoActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /*设置按钮*/
        CBModifyHead.selectType(ComplexButton.TYPE_IMAGE_ROUND);
        CBModifyHead.setButtonClickable(false);
        CBModifyHead.setItemName("头像");
        CBModifyHead.setImageOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            }
        });

        CBVerification.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBVerification.setItemName("身份认证");

        CBPassword.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBPassword.setItemName("修改密码");
        CBPassword.setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ModifyInfoActivity.this, ResetPasswordActivity.class);
                UserIntent userIntent = new UserIntent();
                userIntent.setUserId(user.getUserId());
                userIntent.setPassword(user.getPassword());
                intent.putExtra("user", userIntent);

                startActivityForResult(intent, RESET_PASSWORD);
            }
        });

        CBPhoneNum.selectType(ComplexButton.TYPE_TEXT_ONLY);
        CBPhoneNum.setItemName("修改手机号");
        CBPhoneNum.setButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ModifyInfoActivity.this, ResetPhoneActivity.class);
                UserIntent userIntent = new UserIntent();
                userIntent.setUserId(user.getUserId());
                userIntent.setPhoneNum(user.getPhoneNum());
                intent.putExtra("user", userIntent);
                startActivityForResult(intent, RESET_PHONE);
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    /**
     * 初始化弹出框
     */
    private void initPop() {
        pop = new PopUtil(ModifyInfoActivity.this, R.layout.clip_pop_windows,false);
        layoutPopUp = pop.getPopup();

        TextView tvCamera = (TextView) layoutPopUp.findViewById(R.id.clip_item_popupwindows_camera);
        TextView tvAlbum = (TextView) layoutPopUp.findViewById(R.id.clip_item_popupwindows_Photo);
        TextView tvCancel = (TextView) layoutPopUp.findViewById(R.id.clip_item_popupwindows_cancel);

        /*拍照*/
        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
                overridePendingTransition(R.anim.translate_bottom_in, R.anim.translate_bottom_out);
                pop.dismiss();
                layoutPopUp.clearAnimation();
            }
        });

        /*从相册选取*/
        tvAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, GALLARY);

                overridePendingTransition(R.anim.translate_bottom_in, R.anim.translate_normal);
                pop.dismiss();
                layoutPopUp.clearAnimation();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop.dismiss();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA:
                if (resultCode == RESULT_OK) {
                        openCrop(imageUri, imageCroppedUri);
                } else {
                    Toast.makeText(this, "未拍摄图片", Toast.LENGTH_SHORT).show();
                }
                break;
            case GALLARY:
                if (resultCode == RESULT_OK) {
                    String filePath = null;
                    if (Build.VERSION.SDK_INT >= 19) {
                        filePath = handleImageOnKitKat(data);
                    } else {
                        filePath = handleImageBeforeKitKat(data);
                    }
                    //获取打开文件的URI
                    if (Build.VERSION.SDK_INT >= 24) {
                        imageUri = FileProvider.getUriForFile(ModifyInfoActivity.this,
                                "com.jshaz.daigo.fileprovider", new File(filePath));
                        imageCroppedUri = FileProvider.getUriForFile(ModifyInfoActivity.this,
                                "com.jshaz.daigo.fileprovider", croppedImage);
                    } else {
                        imageUri = Uri.fromFile(new File(filePath));
                        imageCroppedUri = Uri.fromFile(croppedImage);
                    }
                    openCrop(imageUri, imageCroppedUri);
                } else {
                    Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show();
                }
                break;
            case UCrop.REQUEST_CROP:
                if (data != null) {
                    final Uri resultUri = UCrop.getOutput(data);
                    finalHeadBitmap = Utility.compressBitmap(
                            BitmapFactory.decodeFile(croppedImage.getAbsolutePath()));
                    CBModifyHead.setImageBitmap(finalHeadBitmap);
                } else {
                    Toast.makeText(this, "裁剪图片失败", Toast.LENGTH_SHORT).show();
                }

                break;
            case UCrop.RESULT_ERROR:
                Toast.makeText(this, "裁剪图片失败", Toast.LENGTH_SHORT).show();
                break;
            case RESET_PASSWORD:
                if (resultCode == RESULT_OK) {
                    user.setPassword((String) data.getStringExtra("data"));
                    user.writeToLocalDatabase();
                }
                break;
            case RESET_PHONE:
                if (resultCode == RESULT_OK) {
                    user.setPhoneNum((String) data.getStringExtra("data"));
                    user.writeToLocalDatabase();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fillUserInfo();
                        }
                    });
                }
                break;
            case VERIFICATION:

                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初始化图片文件路径
     */
    private void initImageFile() {
        outputImage = new File(getExternalCacheDir(), "cache.jpg");
        croppedImage = new File(getExternalCacheDir(), "crop.jpg");
    }

    /**
     * 执行拍照逻辑
     */
    private void takePhoto() {

        try{
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
            if (croppedImage.exists()) {
                croppedImage.delete();
            }
            croppedImage.createNewFile();
        } catch (IOException e) {
            Toast.makeText(this, "无法读取文件", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(ModifyInfoActivity.this,
                    "com.jshaz.daigo.fileprovider", outputImage);
            imageCroppedUri = FileProvider.getUriForFile(ModifyInfoActivity.this,
                    "com.jshaz.daigo.fileprovider", croppedImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
            imageCroppedUri = Uri.fromFile(croppedImage);
        }
        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA);
    }

    /**
     * 执行打开相册逻辑
     */
    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        return getImagePath(uri, null);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


    /**
     * 开启裁剪API
     * @param sourceUri
     * @param destinationUri
     */
    private void openCrop(Uri sourceUri, Uri destinationUri) {
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(320, 320)
                .start(this);
    }

    /**
     * 开启正在处理对话框
     */
    private void startLoadingDialog() {
        if (loadingDialog == null) loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("正在上传...");
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    /**
     * 关闭正在处理对话框
     */
    private void stopLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(ModifyInfoActivity.this, Manifest.permission
            .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(ModifyInfoActivity.this, Manifest.permission
                .READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(ModifyInfoActivity.this, permissions, 1);
        }
    }

    /**
     * 将数据上传到服务器
     */
    private String response;
    private void updateToServer() {
        startLoadingDialog();
        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    BasicHttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                    HttpConnectionParams.setSoTimeout(httpParams, 5000);

                    HttpClient httpclient = new DefaultHttpClient(httpParams);

                    //服务器地址，指向Servlet
                    HttpPost httpPost = new HttpPost(ServerUtil.SLUpdateUserInfo);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();//将数据装入list

                    if (finalHeadBitmap != null) {
                        //finalHeadBitmap = Utility.compressBitmap(finalHeadBitmap);
                        params.add(new BasicNameValuePair("headicon",
                                Utility.convertBitmapToString(finalHeadBitmap)));
                    }


                    params.add(new BasicNameValuePair("nickname", ETNickName.getText().toString()));
                    params.add(new BasicNameValuePair("address", ETAddress.getText().toString()));
                    params.add(new BasicNameValuePair("userid", user.getUserId()));
                    final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "utf-8");//以UTF-8格式发送
                    httpPost.setEntity(entity);
                    //对提交数据进行编码
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if(httpResponse.getStatusLine().getStatusCode()==200)//在5000毫秒之内接收到返回值
                    {
                        HttpEntity entity1 = httpResponse.getEntity();
                        response = EntityUtils.toString(entity1, "utf-8");//以UTF-8格式解析
                        Message message = mHandler.obtainMessage();
                        message.what = 0;
                        message.obj = response;
                        mHandler.handleMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = mHandler.obtainMessage();
                    message.what = User.NET_ERROR;
                    mHandler.handleMessage(message);
                }
                Looper.loop();
            }
        });
        updateThread.start();
    }

    private void startProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("正在加载信息");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void stopProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 权限回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "未获得权限", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static class MyHandler extends Handler {
        WeakReference<ModifyInfoActivity> activityWeakReference;
        public MyHandler(ModifyInfoActivity activity) {
            this.activityWeakReference = new WeakReference<ModifyInfoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ModifyInfoActivity activity = activityWeakReference.get();
            activity.stopLoadingDialog();
            activity.stopProgressDialog();
            switch (msg.what) {
                case User.USER_RESPONSE:

//                            if (!user.getHeadIcon().equals("")) {
//                                finalHeadBitmap = Utility.convertStringToBitmap(user.getHeadIcon());
//                                CBModifyHead.setImageBitmap(finalHeadBitmap);
//                            }
                    activity.user.convertJSON((String) msg.obj);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.fillUserInfo();
                        }
                    });


                    break;
                case User.USER_WRONG:
                    Toast.makeText(activity, "用户信息出错", Toast.LENGTH_SHORT).show();
                    activity.user.setNullValue();
                    break;
                case User.NET_ERROR:
                    Toast.makeText(activity, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    //TODO:保存并上传个人信息成功
                    if (activity.finalHeadBitmap != null) {
                        activity.user.setHeadIcon(Utility.convertBitmapToString(activity.finalHeadBitmap));
                    }

                    activity.user.setNickName(activity.ETNickName.getText().toString());
                    activity.user.setDefaultAddress(activity.ETAddress.getText().toString());
                    activity.user.writeToLocalDatabase();
                    Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show();
                    activity.finish();
                    break;
            }
        }
    }
}
