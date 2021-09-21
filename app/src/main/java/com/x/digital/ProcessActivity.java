package com.x.digital;

import android.annotation.SuppressLint;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.x.digital.databinding.ActivityProcessBinding;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessActivity extends AppCompatActivity {

    private ActivityProcessBinding binding;

    private Intent intent;
    private Bitmap bitmapOriginal, bitmapDone;
    private Bitmap bitmap1Beauty, bitmap2Fix, bitmap3Bilateral, bitmap4Median, bitmap5Gray;
    private int function = Type.ORIGINAL;//标记当前所选功能
    private int digital = 0;//图像处理的数值
    private Thread thread;//线程
    Handler mHandler;
    private boolean isRunning = false;

    private RecyclerView processRecycleView;//RecyclerView
    private ProcessRecycleViewAdapter processRecycleViewAdapter;//适配器
    LinearLayoutManager linearLayoutManager;//布局管理器
    private final List<ProcessItem> processItems = new ArrayList<>();//传入的Item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProcessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        intent = getIntent();
        function = intent.getIntExtra("function", Type.ORIGINAL);//得到选择的功能，只执行一次，后续以点击后的值为准

        bitmapOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.crotch);//缺省为裤裆的图片
        bitmapDone = Bitmap.createBitmap(bitmapOriginal);
        choose();

        //binding.imageView.setOnClickListener(v -> choose());//因多线程bug禁用

        binding.imageView.setOnLongClickListener(v -> {//长按监听器
            showLayoutDialog();//弹出对话框
            return true;//接口源码中有注释论述了此处的返回值代表啥
        });

        binding.buttonReset.setOnClickListener(v -> showOriginal());//显示原图

        binding.buttonSave.setOnClickListener(v -> addBitmapToAlbum(bitmapDone, String.valueOf(System.currentTimeMillis()), "image/png", Bitmap.CompressFormat.PNG));

        processRecycleView = binding.recyclerViewProcess;
        //初始化数据
        initData(processItems);
        //创建适配器
        processRecycleViewAdapter = new ProcessRecycleViewAdapter(processItems);
        //设置布局管理器，垂直设置LinearLayoutManager.VERTICAL，水平设置LinearLayoutManager.HORIZONTAL
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        processRecycleView.setLayoutManager(linearLayoutManager);
        //设置适配器adapter，并将数据传递给适配器
        processRecycleView.setAdapter(processRecycleViewAdapter);

        //processRecycleView.getRecycledViewPool().setMaxRecycledViews(0, 0);//old-缓存最大值，必须要设置为0，否则RecycleView的复用机制会阻挠点击效果

        //((DefaultItemAnimator)processRecycleView.getItemAnimator()).setSupportsChangeAnimations(false);//禁用动画会造成view刷新失败
        //((SimpleItemAnimator) processRecycleView.getItemAnimator()).setSupportsChangeAnimations(false);

        processRecycleViewAdapter.setOnItemClickListener((view, position) -> {
            //setCurrentAlpha(position);//点击效果
            //processRecycleView.smoothScrollToPosition(position);//转到位置

            processRecycleViewAdapter.setThisPosition(position);
            //嫑忘记刷新适配器
            processRecycleViewAdapter.notifyDataSetChanged();
            //processRecycleViewAdapter.notifyItemRangeChanged(0, processRecycleViewAdapter.getItemCount());

            switch (position + 1) {
                case Type.BEAUTY:
                    bitmapDone = bitmap1Beauty;
                    function = Type.BEAUTY;
                    break;
                case Type.FIX:
                    bitmapDone = bitmap2Fix;
                    function = Type.FIX;
                    break;
                case Type.BILATERAL:
                    bitmapDone = bitmap3Bilateral;
                    function = Type.BILATERAL;
                    break;
                case Type.MEDIAN:
                    bitmapDone = bitmap4Median;
                    function = Type.MEDIAN;
                    break;
                case Type.GRAY:
                    bitmapDone = bitmap5Gray;
                    function = Type.GRAY;
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

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.textViewDigital.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                digital = seekBar.getProgress();
                process();//重新处理

            }
        });
    }

    private void showOriginal() {//显示原图
        bitmapDone = Bitmap.createBitmap(bitmapOriginal);
        binding.imageView.setImageBitmap(bitmapDone);
        //setCurrentAlpha(Type.ORIGINAL - 1);//点击效果
        function = Type.ORIGINAL;//置当前为原图的标记
        processRecycleViewAdapter.setThisPosition(-1);//清空点击效果
        processRecycleViewAdapter.notifyDataSetChanged();
    }

    /*private void setCurrentAlpha(int position) {//点击效果
        processRecycleViewAdapter.notifyItemRangeChanged(0, processRecycleViewAdapter.getItemCount());//刷新全部view
        if (position != -1) {
            processRecycleView.scrollToPosition(position);//转到位置
            processRecycleView.postDelayed(() ->
                            processRecycleView.getLayoutManager().findViewByPosition(position).setAlpha(0.7f)
                    , 500);//点击效果
        } else {//否则是原图，不必点击
            //processRecycleView.scrollToPosition(Type.ORIGINAL);//回到首位
        }
            *//*processRecycleView.scrollToPosition(Type.BEAUTY-1);
            processRecycleView.postDelayed(() -> {
                        for (int p = linearLayoutManager.findFirstVisibleItemPosition(); p <= linearLayoutManager.findLastVisibleItemPosition(); p++) {
                            processRecycleView.getLayoutManager().findViewByPosition(p).setAlpha(1.0f);
                        }

                        processRecycleView.scrollToPosition(Type.GRAY - 1);
                        processRecycleView.postDelayed(() -> {
                                    for (int p = linearLayoutManager.findFirstVisibleItemPosition(); p <= linearLayoutManager.findLastVisibleItemPosition(); p++) {
                                        processRecycleView.getLayoutManager().findViewByPosition(p).setAlpha(1.0f);
                                    }

                                    if (position!=-1){
                                        processRecycleView.scrollToPosition(position);
                                        processRecycleView.postDelayed(() ->
                                                        processRecycleView.getLayoutManager().findViewByPosition(position).setAlpha(0.7f)
                                                , 10);
                                    }
                                    else {
                                        processRecycleView.scrollToPosition(Type.ORIGINAL);
                                    }


                                }, 10);//延迟50毫秒后，再进行操作，避免crash
                    }, 10);//延迟50毫秒后，再进行操作，避免crash*//*


        *//*View itemView;
        for (int p = linearLayoutManager.findFirstVisibleItemPosition(); p <= linearLayoutManager.findLastVisibleItemPosition(); p++)
        //for (int p=0;p<=3;p++)
        //for(int p=0;p<processRecycleViewAdapter.getItemCount();p++)
        {
            itemView = Objects.requireNonNull(processRecycleView.getLayoutManager()).findViewByPosition(p);
            //itemView = processRecycleView.getChildAt(p);
            //itemView = Objects.requireNonNull(processRecycleView.findViewHolderForAdapterPosition(p)).itemView;
            //Log.d("p=", String.valueOf(p));
            assert itemView != null;
            itemView.setAlpha(1.0f);
        }*//*
    }*/

    private void process() {//处理图像

        //binding.imageView.setImageBitmap(bitmapOriginal);//只要开始处理就先设置显示原图，避免点击保存后造成crash
        binding.imageView.setImageBitmap(bitmapDone);//只要开始处理就先设置显示上一次处理的图片，避免点击保存后造成crash
        UpdateUI(false);//隐藏UI

        //mHandler = new Handler();//创建Handler，接收子线程数据更新UI

        thread = new Thread(() -> {
            isRunning = false;
                //于子线程中处理图像，避免卡顿
                Beauty();
                Fix();
                Bilateral();
                Median();
                Gray();


            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateUI(true);

                    if (function != Type.ORIGINAL) {//模拟首页点击或者上一次点击view操作
                        processRecycleView.smoothScrollToPosition(function - 1);//带动画的移动滚动条操作
                        processRecycleView.postDelayed(() ->
                                        processRecycleView.findViewHolderForAdapterPosition(function - 1).itemView.performClick()
                                , 500);//延迟500毫秒后，再进行模拟点击操作，避免crash
                    } else {//function==Type.ORIGINAL，从通知栏进的或者点击重置按钮后默认显示原图
                        showOriginal();
                    }
                }
            });
            //Runnable放在主线程或新开线程中都可以
            /*Runnable runnable = () -> {
                //处理完毕，更新显示UI
                UpdateUI(true);

                if (function != Type.ORIGINAL) {//模拟首页点击或者上一次点击view操作
                    processRecycleView.smoothScrollToPosition(function - 1);//带动画的移动滚动条操作
                    processRecycleView.postDelayed(() ->
                                    processRecycleView.findViewHolderForAdapterPosition(function - 1).itemView.performClick()
                            , 500);//延迟500毫秒后，再进行模拟点击操作，避免crash
                } else {//function==Type.ORIGINAL，从通知栏进的或者点击重置按钮后默认显示原图
                    showOriginal();
                }

                *//*if (function >= Type.BEAUTY && function <= 4) {//从首页进的，模拟点击view操作
                    processRecycleView.scrollToPosition(function);
                    Objects.requireNonNull(processRecycleView.findViewHolderForAdapterPosition(function - 1)).itemView.performClick();
                } else if (function == 5) {//对灰度的处理，目前也只能如此了...
                    processRecycleView.scrollToPosition(function - 1);
                    bitmapDone = bitmap5Gray;
                    binding.imageView.setImageBitmap(bitmapDone);
                    //processRecycleView.scrollToPosition(4);
                    //Objects.requireNonNull(Objects.requireNonNull(processRecycleView.getLayoutManager()).findViewByPosition(linearLayoutManager.findLastVisibleItemPosition())).performClick();
                } else {//function==0，从通知栏进的，显示原图
                    bitmapDone = Bitmap.createBitmap(bitmapOriginal);
                    binding.imageView.setImageBitmap(bitmapDone);
                }*//*

             *//*switch (function) {
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
                        bitmapDone = Bitmap.createBitmap(bitmapOrigin);
                        break;
                }
                binding.imageView.setImageBitmap(bitmapDone);*//*
            };
            //加入线程队列
            mHandler.post(runnable);*/
        });

        thread.start();//开启子线程
    }

    private void UpdateUI(boolean enabled) {
        if (!enabled) {//使控件无法操作，表示正在处理
            binding.progressBar.setVisibility(View.VISIBLE);//显示进度条
            //binding.buttonReset.setEnabled(false);
            binding.recyclerViewProcess.setVisibility(View.INVISIBLE);//避免空值保存造成crash
        } else {//处理完毕，可以操作
            binding.progressBar.setVisibility(View.GONE);
            //binding.buttonReset.setEnabled(true);
            binding.recyclerViewProcess.setVisibility(View.VISIBLE);
        }
    }

    private void Gray() {//灰度
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOriginal, mat);

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2GRAY);

        //图像归一化，参数：输入、输出、归一化最小值、归一化最大值、归一化类型、种类（负数时输入输出种类相同，否则只是通道相同）、遮罩范围（处理范围）
        Core.normalize(mat, mat, 0, 255, Core.NORM_MINMAX, -1, new Mat());

        bitmap5Gray = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap5Gray);
    }

    private void Median() {//中值滤波
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOriginal, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);

        Imgproc.medianBlur(mat, mat, (1 + digital) % 2 == 1 ? (1 + digital) : (2 + digital));//必须设置ksize为奇数，否则会出现crash

        bitmap4Median = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap4Median);
    }

    private void Bilateral() {//双边滤波
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOriginal, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);

        Mat matFilter = new Mat(mat.width(), mat.height(), Imgproc.COLOR_BGRA2BGR);
        Imgproc.bilateralFilter(mat, matFilter, 10 + digital, 50, 25 / 2.0);

        bitmap3Bilateral = Bitmap.createBitmap(matFilter.width(), matFilter.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matFilter, bitmap3Bilateral);
    }

    private void Fix() {//已知在线程中会因处理缓慢出现问题，显示上一张图片的结果
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOriginal, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);

        //灰度
        Mat matGray = new Mat();
        Imgproc.cvtColor(mat, matGray, Imgproc.COLOR_BGRA2GRAY);

        //Mask
        Mat matMask = new Mat();
        //通过域值生成Mask
        Imgproc.threshold(matGray, matMask, 245, 255, Imgproc.THRESH_BINARY);

        //膨胀，增加Mask面积
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3), new Point(-1, -1));//内核，宽高必须是奇数
        Imgproc.morphologyEx(matMask, matMask, Imgproc.MORPH_DILATE, kernel);

        Mat matFix = new Mat();//按两次Shift键直接搜索方法名找类名是个好方法
        Photo.inpaint(mat, matMask, matFix, 5, Photo.INPAINT_NS);//修复图像


            bitmap2Fix = Bitmap.createBitmap(matFix.width(), matFix.height(), Bitmap.Config.ARGB_8888);
        if (!isRunning) {
            Utils.matToBitmap(matFix, bitmap2Fix);
        }
    }

    private void Beauty() {//美颜
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmapOriginal, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);

        //归一化，为了后面方便处理而进行收敛（不确定好不好使）
        Core.normalize(mat, mat, 0, 255, Core.NORM_MINMAX, -1, new Mat());
        //图像亮度，制衡对比度，显得更饱满而不玄幻
        Core.add(mat, new Scalar(-10 + digital, -10 + digital, -10 + digital), mat);
        //图像的对比度
        Core.multiply(mat, new Scalar(1.1, 1.1, 1.1), mat);
        //均值滤波
        Imgproc.blur(mat, mat, new Size(1, 1), new Point(-1, -1), Core.BORDER_DEFAULT);
        //高斯滤波
        Imgproc.GaussianBlur(mat, mat, new Size(1, 1), 10, 20);
        //中值滤波
        Imgproc.medianBlur(mat, mat, 1);//必须设置ksize为奇数，否则会出现crash

        //方框滤波，暂时没弄明白如何使用，目前会引起crash
        //Imgproc.sqrBoxFilter(mat, mat,-1,new Size(3,3),new Point(-1,-1),true);

        Mat matFilter = new Mat(mat.width(), mat.height(), Imgproc.COLOR_BGRA2BGR);
        //双边滤波，要求src与dst不相同
        Imgproc.bilateralFilter(mat, matFilter, 15 + digital, 50, 50 / 2.0);

        bitmap1Beauty = Bitmap.createBitmap(matFilter.width(), matFilter.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matFilter, bitmap1Beauty);
    }

    private void initData(List<ProcessItem> processItems) {
        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.dog), getString(R.string.functionBeauty))
        );

        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.awalak), getString(R.string.functionFix))
        );

        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.dragon), getString(R.string.functionBilateral))
        );

        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.roach), getString(R.string.functionMedian))
        );

        processItems.add(
                new ProcessItem(BitmapFactory.decodeResource(getResources(), R.drawable.sera), getString(R.string.functionGraying))
        );
    }

    private void choose() {//选择图像
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
                bitmapOriginal = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                bitmapDone = Bitmap.createBitmap(bitmapOriginal);
                //binding.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                //thread.interrupt();//重新选择前销毁之前的线程
                //mHandler.removeCallbacksAndMessages(null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            finish();//未选择图像则销毁Activity
        }

        process();//选择图像后（未选择图像则默认为上一次选择的图片，初始化时定义为裤裆的图片）立即于子线程中处理
    }

    private void showLayoutDialog() {//弹出对话框
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

                toast.show();//弹出toast

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
    private OnItemTouchListener onItemTouchListener;

    //先声明一个int成员变量
    private int thisPosition = -1;

    //再定义一个int类型的返回值方法
    public int getThisPosition() {
        return thisPosition;
    }

    //其次定义一个方法用来绑定当前参数值的方法
    //此方法是在调用此适配器的地方调用的，此适配器内不会被调用到
    public void setThisPosition(int thisPosition) {
        this.thisPosition = thisPosition;
    }


    //设置点击监听
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setOnItemTouchListener(OnItemTouchListener onItemTouchListener) {
        this.onItemTouchListener = onItemTouchListener;
    }

    //自定义点击监听接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnLongClickListener {
        void onLongClick(View view, int position);
    }

    public interface OnItemTouchListener {
        void onItemTouch(View view, int position);
    }

    //通过方法提供的ViewHolder，将数据绑定到ViewHolder中
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        ProcessItem list = mList.get(position);
        holder.CardImageView.setImageBitmap(list.getCardImage());
        holder.CardTextTitleView.setText(list.getCardTextTitle());

        Log.d("ppp", String.valueOf(position));
        //点击效果
        if (position == getThisPosition()) {
            //holder.itemView.setAlpha(0.7f);
            holder.itemView.animate().alpha(0.7f).setDuration(150).start();
        } else {
            //holder.itemView.setAlpha(1.0f);
            holder.itemView.animate().alpha(1.0f).setDuration(150).start();
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onLongClickListener != null) {
                onLongClickListener.onLongClick(v, holder.getAdapterPosition());
            }
            return false;//return true;
        });

        holder.itemView.setOnTouchListener((v, event) -> {
            //因动画遭刷新后有割裂感，故此弃用
            /*switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }*/

            if (onItemTouchListener != null) {
                onItemTouchListener.onItemTouch(v, holder.getAdapterPosition());
            }
            //v.onTouchEvent(event);
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
            CardTextContentView = itemView.findViewById(R.id.CardTextVersion);
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