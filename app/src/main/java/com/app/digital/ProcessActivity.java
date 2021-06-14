package com.app.digital;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.digital.databinding.ActivityProcessBinding;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessActivity extends AppCompatActivity {

    private ActivityProcessBinding binding;

    private Intent intent;
    private Bitmap bitmapOrigin, bitmapDone;
    private Bitmap bitmap1, bitmap2, bitmap3, bitmap4, bitmap5;

    private RecyclerView processRecycleView;//RecyclerView
    private ProcessRecycleViewAdapter processRecycleViewAdapter;//适配器
    private final List<ProcessItem> processItems = new ArrayList<>();//传入的Item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProcessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bitmapOrigin = BitmapFactory.decodeResource(getResources(), R.drawable.crotch);
        bitmapDone = Bitmap.createBitmap(bitmapOrigin);
        choose();

        binding.imageView.setOnClickListener(v -> choose());

        binding.imageView.setOnLongClickListener(v -> {
            showLayoutDialog();
            return true;//接口源码中有注释论述了此处的返回值代表啥
        });

        binding.buttonReset.setOnClickListener(v -> binding.imageView.setImageBitmap(bitmapOrigin));//finish();销毁Activity

        binding.buttonSave.setOnClickListener(v -> addBitmapToAlbum(bitmapDone, String.valueOf(System.currentTimeMillis()), "image/png", Bitmap.CompressFormat.PNG));

        processRecycleView = binding.recyclerViewProcess;
        //初始化数据
        initData(processItems);
        //创建适配器
        processRecycleViewAdapter = new ProcessRecycleViewAdapter(processItems);
        //设置布局管理器，垂直设置LinearLayoutManager.VERTICAL，水平设置LinearLayoutManager.HORIZONTAL
        processRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //设置适配器adapter，并将数据传递给适配器
        processRecycleView.setAdapter(processRecycleViewAdapter);

        processRecycleViewAdapter.setOnItemClickListener((view, position) -> {
            switch (position + 1) {
                case 1:
                    bitmapDone = bitmap1;
                    break;
                case 2:
                    bitmapDone = bitmap2;
                    break;
                case 3:
                    bitmapDone = bitmap3;
                    break;
                case 4:
                    bitmapDone = bitmap4;
                    break;
                case 5:
                    bitmapDone = bitmap5;
                    break;
            }
            binding.imageView.setImageBitmap(bitmapDone);
        });

        processRecycleViewAdapter.setOnLongClickListener((view, position) -> {
            switch (position + 1) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
            }
        });
    }

    private void process() {
        binding.progressBar.setVisibility(View.VISIBLE);
        Handler mHandler = new Handler();//创建Handler，接收子线程数据更新UI
        new Thread(() -> {
            //于子线程中处理图像，避免卡顿
            Beauty();
            Open();
            Bilateral();
            Median();
            Graying();

            //Runnable放在主线程或新开线程中都可以
            Runnable runnable = () -> {
                intent = getIntent();
                int function = intent.getIntExtra("function", 0);
                //更新UI
                binding.progressBar.setVisibility(View.GONE);

                switch (function) {
                    case 1:
                        bitmapDone = bitmap1;
                        break;
                    case 2:
                        bitmapDone = bitmap2;
                        break;
                    case 3:
                        bitmapDone = bitmap3;
                        break;
                    case 4:
                        bitmapDone = bitmap4;
                        break;
                    case 5:
                        bitmapDone = bitmap5;
                        break;
                    default:
                        bitmapDone = bitmapOrigin;
                        break;
                }
                binding.imageView.setImageBitmap(bitmapDone);
            };
            //加入线程队列
            mHandler.post(runnable);
        }).start();//开启子线程
    }

    private void Graying() {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOrigin, mat);
        //灰度
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2GRAY);
        //图像归一化，参数：输入、输出、归一化最小值、归一化最大值、归一化类型、种类（负数时输入输出种类相同，否则只是通道相同）、遮罩范围（处理范围）
        Core.normalize(mat, mat, 0, 255, Core.NORM_MINMAX, -1, new Mat());

        bitmap5 = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap5);
    }

    private void Median() {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOrigin, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);

        Imgproc.medianBlur(mat, mat, 5);//必须设置ksize为奇数，否则会出现crash

        bitmap4 = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap4);
    }

    private void Bilateral() {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOrigin, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);

        Mat mat2 = new Mat(mat.width(), mat.height(), Imgproc.COLOR_BGRA2BGR);
        Imgproc.bilateralFilter(mat, mat2, 9, 50, 25 / 2.0);

        bitmap3 = Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, bitmap3);
    }

    private void Open() {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOrigin, mat);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5), new Point(-1, -1));//内核，宽高必须是奇数
        Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_OPEN, kernel);//开运算

        bitmap2 = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap2);
    }

    private void Beauty() {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOrigin, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);

        Core.normalize(mat, mat, 0, 255, Core.NORM_MINMAX, -1, new Mat());
        //图像亮度
        Core.add(mat, new Scalar(-10, -10, -10), mat);
        //图像的对比度
        Core.multiply(mat, new Scalar(1.1, 1.1, 1.1), mat);
        //模糊
        //Imgproc.blur(mat, mat, new Size(2, 2), new Point(-1, -1), Core.BORDER_DEFAULT);

        Mat mat2 = new Mat(mat.width(), mat.height(), Imgproc.COLOR_BGRA2BGR);
        Imgproc.bilateralFilter(mat, mat2, 9, 50, 25 / 2.0);

        bitmap1 = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, bitmap1);
    }

    private void initData(List<ProcessItem> processItems) {
        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.dog), getString(R.string.functionBeauty))
        );

        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.awalak), getString(R.string.functionOpen))
        );

        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.dragon), getString(R.string.functionBilateral))
        );

        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.roach), getString(R.string.functionMedian))
        );

        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.sera2), getString(R.string.functionGraying))
        );
    }

    private void choose() {
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 1 && data != null) {
            Uri uri = data.getData();

            ContentResolver contentResolver = this.getContentResolver();

            try {
                bitmapOrigin = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                bitmapDone = Bitmap.createBitmap(bitmapOrigin);
                //binding.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        process();//选择图像后（未选择图像则默认为上一次选择的图片，初始化时定义为裤裆的图片）立即于子线程中处理
    }

    private void showLayoutDialog() {
        //加载布局并初始化组件
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset, null);
        TextView dialogSave = dialogView.findViewById(R.id.textViewDialogSave);
        TextView dialogCancel = dialogView.findViewById(R.id.textViewDialogCancel);
        final AlertDialog.Builder layoutDialog = new AlertDialog.Builder(this, R.style.TransparentDialog);

        layoutDialog.setView(dialogView);
        AlertDialog dialog = layoutDialog.create();

        dialogSave.setOnClickListener(v -> {
            addBitmapToAlbum(bitmapDone, String.valueOf(System.currentTimeMillis()), "image/png", Bitmap.CompressFormat.PNG);
            dialog.dismiss();
        });
        dialogCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    //因Android10及以上系统版本已无法访问Data目录，故采用Android作用域存储
    public final void addBitmapToAlbum(Bitmap bitmap, String displayName, String mimeType, Bitmap.CompressFormat compressFormat) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/数字图像处理");
        } else {
            values.put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_PICTURES}/OpenCV/$displayName");
        }

        Uri uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = this.getContentResolver().openOutputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (outputStream != null) {
                bitmap.compress(compressFormat, 100, outputStream);

                Toast toast = Toast.makeText(getApplicationContext(), "图像保存成功，文件目录为" + Environment.DIRECTORY_PICTURES + "/数字图像处理/" + displayName + ".png", Toast.LENGTH_LONG);

                toast.show();

                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class ProcessRecycleViewAdapter extends RecyclerView.Adapter<ProcessRecycleViewAdapter.ViewHolder> {

    private final List<ProcessItem> mList;//数据源

    private OnItemClickListener onItemClickListener;//点击监听器
    private OnLongClickListener onLongClickListener;//长按监听器

    //设置点击监听
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    //自定义点击监听接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnLongClickListener {
        void onLongClick(View view, int position);
    }

    //通过方法提供的ViewHolder，将数据绑定到ViewHolder中
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        ProcessItem list = mList.get(position);
        holder.CardImageView.setImageBitmap(list.getCardImage());
        holder.CardTextTitleView.setText(list.getCardTextTitle());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onLongClickListener != null) {
                onLongClickListener.onLongClick(v, holder.getAdapterPosition());
            }
            return false;
        });
    }

    ProcessRecycleViewAdapter(List<ProcessItem> list) {
        mList = list;
    }

    //创建ViewHolder并返回，后续item布局里控件都是从ViewHolder中取出，即加载布局
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //将自定义的item布局转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_process_item, parent, false);
        //将view传递给我们自定义的ViewHolder，即返回这个ViewHolder实体
        return new ViewHolder(view);
    }

    //获取数据源总的条数
    @Override
    public int getItemCount() {
        return mList.size();
    }

    //自定义ViewHolder，绑定布局中的id
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView CardImageView;
        TextView CardTextTitleView;
        TextView CardTextContentView;

        public ViewHolder(View itemView) {
            super(itemView);
            CardImageView = itemView.findViewById(R.id.CardImage);
            CardTextTitleView = itemView.findViewById(R.id.CardTextTitle);
            CardTextContentView = itemView.findViewById(R.id.CardTextContent);
        }
    }

    //移除数据
    public void removeData(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    //新增数据
    public void addData(int position, Bitmap CardImage, String CardTextTitle) {
        mList.add(position, new ProcessItem(CardImage, CardTextTitle));
        notifyItemInserted(position);
    }

    //更改某个位置的数据
    public void changeData(int position, Bitmap CardImage, String CardTextTitle) {
        mList.set(position, new ProcessItem(CardImage, CardTextTitle));
        notifyItemChanged(position);
    }
}

class ProcessItem {
    //Item对应图片的资源id，在drawable中
    private final Bitmap CardImage;

    //Item的TextView内容
    private final String CardTextTitle;

    //构造函数
    public ProcessItem(Bitmap CardImage, String CardTextTitle) {
        this.CardImage = CardImage;
        this.CardTextTitle = CardTextTitle;
    }

    //getter
    public Bitmap getCardImage() {
        return CardImage;
    }

    public String getCardTextTitle() {
        return CardTextTitle;
    }
}