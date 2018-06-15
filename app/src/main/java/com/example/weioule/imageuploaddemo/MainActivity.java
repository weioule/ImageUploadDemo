package com.example.weioule.imageuploaddemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "uploadImage";

    /**
     * 去上传文件
     */
    protected static final int TO_UPLOAD_FILE = 1;
    /**
     * 上传文件响应
     */
    protected static final int UPLOAD_FILE_DONE = 2;
    /**
     * 选择文件
     */
    public static final int TO_SELECT_PHOTO = 3;
    /**
     * 上传初始化
     */
    private static final int UPLOAD_INIT_PROCESS = 4;
    /**
     * 上传中
     */
    private static final int UPLOAD_IN_PROCESS = 5;

    private static String requestURL = "http://192.168.10.160:8080/fileUpload/p/file!upload";

    private Button selectButton, uploadButton;
    private ProgressDialog progressDialog;
    private TextView uploadImageResult;
    private ProgressBar progressBar;
    private ImageView imageView;
    private String picPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        selectButton = (Button) this.findViewById(R.id.selectImage);
        uploadButton = (Button) this.findViewById(R.id.uploadImage);
        selectButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        imageView = (ImageView) this.findViewById(R.id.imageView);
        uploadImageResult = (TextView) findViewById(R.id.uploadImageResult);
        progressDialog = new ProgressDialog(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.selectImage:
                Intent intent = new Intent(this, SelectPicActivity.class);
                startActivityForResult(intent, TO_SELECT_PHOTO);
                break;
            case R.id.uploadImage:
                if (picPath != null) {
                    handler.sendEmptyMessage(TO_UPLOAD_FILE);
                } else {
                    Toast.makeText(this, "上传的文件路径出错", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == TO_SELECT_PHOTO) {
            picPath = data.getStringExtra(SelectPicActivity.KEY_PHOTO_PATH);
            Log.i(TAG, "最终选择的图片=" + picPath);
            Bitmap bm = BitmapFactory.decodeFile(picPath);
            imageView.setImageBitmap(bm);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void toUploadFile() {
        progressDialog.setMessage("正在上传文件...");
        progressDialog.show();
        String fileKey = "pic";
        UploadUtil uploadUtil = UploadUtil.getInstance();

        uploadUtil.setOnUploadProcessListener(new UploadUtil.OnUploadProcessListener() {
            Message msg = Message.obtain();

            @Override
            public void onUploadDone(int responseCode, String message) {
                //上传服务器响应回调
                progressDialog.dismiss();
                progressBar.setVisibility(View.GONE);
                msg.what = UPLOAD_FILE_DONE;
                msg.arg1 = responseCode;
                msg.obj = message;
                handler.sendMessage(msg);
            }

            @Override
            public void onUploadProcess(int uploadSize) {
                msg.what = UPLOAD_IN_PROCESS;
                msg.arg1 = uploadSize;
                handler.sendMessage(msg);
            }

            @Override
            public void initUpload(int fileSize) {
                progressBar.setVisibility(View.VISIBLE);
                msg.what = UPLOAD_INIT_PROCESS;
                msg.arg1 = fileSize;
                handler.sendMessage(msg);
            }
        });  //设置监听器监听上传状态

        Map<String, String> params = new HashMap<>();
        params.put("orderId", "11111");
        uploadUtil.uploadFile(picPath, fileKey, requestURL, params);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TO_UPLOAD_FILE:
                    toUploadFile();
                    break;
                case UPLOAD_INIT_PROCESS:
                    progressBar.setMax(msg.arg1);
                    break;
                case UPLOAD_IN_PROCESS:
                    progressBar.setProgress(msg.arg1);
                    break;
                case UPLOAD_FILE_DONE:
                    uploadImageResult.setVisibility(View.VISIBLE);
                    String result = "响应码：" + msg.arg1 + "\n响应信息：" + msg.obj + "\n耗时：" + UploadUtil.getRequestTime() + "秒";
                    uploadImageResult.setText(result);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };
}
