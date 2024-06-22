package com.example.fruit_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.activity.ComponentActivity;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AIManager {
    private static AIManager instance;

    public static AIManager getInstance() {
        return instance == null ? new AIManager() : instance;
    }

    private AIManager() {
        instance = this;
    }

    private Module model;

    // Define recognition result mapping table
    private final Map<Integer, String> map = new HashMap<Integer, String>() {{
        put(0, "香蕉");
        put(1, "玉米");
        put(2, "柠檬");
        put(3, "橙子");
        put(4, "桃子");
        put(5, "梨");
        put(6, "西瓜");
    }};

    // 获取模型路径
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                try (OutputStream os = Files.newOutputStream(file.toPath())) {
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                    }
                    os.flush();
                }
            }
            return file.getAbsolutePath();
        }
    }

    // 初始化基础模型
    public void init(ComponentActivity ctx) {
        String modelPath;
        try {
            modelPath = assetFilePath(ctx, "model.ptl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.i("TAG", "模型路径：" + modelPath);
        model = Module.load(modelPath);
    }

    // 识别
    public String inference(Bitmap bitmap) {
        Tensor outputTensor = model.forward(IValue.from(getTensor(bitmap))).toTensor();
        float[] scores = outputTensor.getDataAsFloatArray();
        Log.i("TAG", "识别结果集: " + Arrays.toString(scores));
        int maxIdx = getMaxIdx(scores);
        return "识别结果：" + map.get(maxIdx);
    }

    // 获取最大值的索引
    private int getMaxIdx(float[] scores) {
        int idx = 0;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] >= scores[idx]) {
                idx = i;
            }
        }
        return idx;
    }

    // 重新识别
    public String restartInference(Context ctx, Bitmap bitmap) throws IOException {
        Module newModel = Module.load(assetFilePath(ctx, "model_cpu.ptl"));
        Tensor outputTensor = newModel.forward(IValue.from(getTensor(bitmap))).toTensor();
        float[] scores = outputTensor.getDataAsFloatArray();
        Log.i("TAG", "重新识别结果集" + Arrays.toString(scores));
        int maxIdx = getMaxIdx(scores);
        return "识别结果：" + map.get(maxIdx);
    }

    //获取图片张量
    public Tensor getTensor(Bitmap bitmap) {
        Bitmap resizeBm = Bitmap.createScaledBitmap(bitmap, 100, 100, true);

        return TensorImageUtils.bitmapToFloat32Tensor(
                resizeBm,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
        );
    }
}
