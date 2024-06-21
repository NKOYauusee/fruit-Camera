package com.example.fruit_camera;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ImageView picture;
    private TextView res;
    private Uri pitUri;

    private final ActivityResultLauncher<String> openPit = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            pitUri = uri;

            this.picture = findViewById(R.id.picture);
            this.picture.setImageURI(pitUri);
            try {
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
                setRes(pitUri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

        }
    });
    private final ActivityResultLauncher<Uri> takePit = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
        if (success) {
            this.picture = findViewById(R.id.picture);
            this.picture.setImageURI(pitUri);
            try {
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
                setRes(pitUri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //动态获取相机权限
        Permission.checkPermission(this);
        AIManager.getInstance().init(this);
        init();
    }
    //初始化
    private void init() {
        picture = findViewById(R.id.picture);
        res = findViewById(R.id.res);
        Button reBtn = findViewById(R.id.restart);
        reBtn.setOnClickListener(v -> restartVerify());
    }
    // 选择图片
    public void selectPit(View view) {
        openPit.launch("image/*");
    }
    // 跳转拍照界面
    public void jumpToCamera(View view) {
        pitUri = createImageUri();
        Log.i("TAG", "jumpToCamera: " + pitUri);
        takePit.launch(pitUri);
    }

    //重新识别
    public void restartVerify() {
        try {
            if (picture.getDrawable() == null) {
                Toast.makeText(this, "请先选择图片或拍照", Toast.LENGTH_SHORT).show();
                return;
            }
            BitmapDrawable bitmapDrawable = (BitmapDrawable) (picture.getDrawable());
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Toast.makeText(this, "重新识别成功", Toast.LENGTH_SHORT).show();
            String info = AIManager.getInstance().restartInference(this, bitmap);
            res = findViewById(R.id.res);
            res.setText(info);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRes(Uri uri) throws FileNotFoundException {
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        String info = AIManager.getInstance().inference(bitmap);
        Log.i("TAG", "setRes: " + info);
        res.setText(info);
    }

    private Uri createImageUri() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + "image.jpg";
            File file = new File(path);
            return Uri.fromFile(file);
        }
    }
}