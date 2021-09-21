package com.x.digital;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.x.digital.databinding.ActivityMainBinding;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private RecyclerView mainRecycleView;//RecyclerView
    private MainRecycleViewAdapter mainRecycleViewAdapter;//适配器

    private final List<MainItem> mainItems = new ArrayList<>();//传入的Item

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.textViewMain.setOnClickListener(v -> {
            intent = new Intent(this,AboutActivity.class);
            startActivity(intent);
        });

        OpenCVLoader.initDebug();//OpenCV初始化后才能使用Mat等类

        mainRecycleView = binding.recyclerViewMain;
        //初始化数据
        initData(mainItems);
        //创建适配器
        mainRecycleViewAdapter = new MainRecycleViewAdapter(mainItems);
        //设置布局管理器，垂直设置LinearLayoutManager.VERTICAL，水平设置LinearLayoutManager.HORIZONTAL
        mainRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //设置适配器adapter，并将数据传递给适配器
        mainRecycleView.setAdapter(mainRecycleViewAdapter);

        mainRecycleViewAdapter.setOnItemClickListener((view, position) -> {
            /*intent = new Intent(this, ProcessActivity.class);
            switch (position + 1) {
                case Type.BEAUTY:
                    intent.putExtra("function", Type.BEAUTY);
                    break;
                case Type.FIX:
                    intent.putExtra("function", Type.FIX);
                    break;
                case Type.BILATERAL:
                    intent.putExtra("function", Type.BILATERAL);
                    break;
                case Type.MEDIAN:
                    intent.putExtra("function", Type.MEDIAN);
                    break;
                case Type.GRAY:
                    intent.putExtra("function", Type.GRAY);
                    break;
                default:
                    intent = new Intent(this, AboutActivity.class);
                    break;
            }
            startActivity(intent);*/
        });

        /*mainRecycleViewAdapter.setOnLongClickListener((view, position) -> {
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
                case 6:
                    break;
            }
        });*/

        //一定一定要先创建ChannelID
        createNotificationChannel();//调用创建CHANNEL_ID方法
        Intent intentNotify = new Intent(this, ProcessActivity.class);
        intentNotify.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// | Intent.FLAG_ACTIVITY_CLEAR_TASK
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentNotify, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.flower)
                .setContentTitle("欢迎使用我的APP")
                .setContentText("快去选择图片开启你的图像处理之旅吧！")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)//PRIORITY_HIGH

                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //显示通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(0, builder.build());
    }

    private void initData(List<MainItem> mainItems) {
        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.dog), getString(R.string.functionBeauty), getString(R.string.functionBeautyContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.awalak), getString(R.string.functionFix), getString(R.string.functionFixContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.dragon), getString(R.string.functionBilateral), getString(R.string.functionBilateralContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.roach), getString(R.string.functionMedian), getString(R.string.functionMedianContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.sera), getString(R.string.functionGraying), getString(R.string.functionGrayingContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.sera_origin), getString(R.string.about), getString(R.string.aboutContent))
        );
        /*先初始化适配器后才能用适配器的方法添加数据
        mainRecycleViewAdapter.addData(
                0,BitmapFactory.decodeResource(getResources(), R.drawable.dog), getString(R.string.functionBeauty),  getString(R.string.functionBeautyContent)
        );
        mainRecycleViewAdapter.addData(
                1,BitmapFactory.decodeResource(getResources(), R.drawable.awalak), getString(R.string.functionOpen),  getString(R.string.functionOpenContent)
        );
        mainRecycleViewAdapter.addData(
                2,BitmapFactory.decodeResource(getResources(), R.drawable.dragon), getString(R.string.functionBilateral),  getString(R.string.functionBilateralContent)
        );
        mainRecycleViewAdapter.addData(
                3,BitmapFactory.decodeResource(getResources(), R.drawable.roach), getString(R.string.functionMedian),  getString(R.string.functionMedianContent)
        );
        mainRecycleViewAdapter.addData(
                4, BitmapFactory.decodeResource(getResources(), R.drawable.sera2), getString(R.string.functionGraying),  getString(R.string.functionGrayingContent)
        );*/
    }

    private void createNotificationChannel() {//谷歌文档中的创建CHANNEL_ID方法
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}


class MainRecycleViewAdapter extends RecyclerView.Adapter<MainRecycleViewAdapter.ViewHolder> {

    private final List<MainItem> mList;//数据源

    private OnItemClickListener onItemClickListener;//点击监听器
    //private OnLongClickListener onLongClickListener;//长按监听器
    private OnItemTouchListener onItemTouchListener;

    //设置点击监听
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /*public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }*/

    public void setOnItemTouchListener(OnItemTouchListener onItemTouchListener) {
        this.onItemTouchListener = onItemTouchListener;
    }

    //自定义点击监听接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    /*public interface OnLongClickListener {
        void onLongClick(View view, int position);
    }*/

    public interface OnItemTouchListener {
        void onItemTouch(View view, int position);
    }

    //通过方法提供的ViewHolder，将数据绑定到ViewHolder中
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        MainItem list = mList.get(position);
        holder.CardImageView.setImageBitmap(list.getCardImage());
        holder.CardTextTitleView.setText(list.getCardTextTitle());
        holder.CardTextContentView.setText(list.getCardTextContent());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });

        /*holder.itemView.setOnLongClickListener(v -> {
            if (onLongClickListener != null) {
                onLongClickListener.onLongClick(v, holder.getAdapterPosition());
            }
            return false;//return true;
        });*/

    }

    MainRecycleViewAdapter(List<MainItem> list) {
        mList = list;
    }

    //创建ViewHolder并返回，后续item布局里控件都是从ViewHolder中取出，即加载布局
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //将自定义的item布局转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_main_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.itemView.setOnTouchListener((v, event) -> {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).start();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                    break;
                case MotionEvent.ACTION_UP:
                    Intent intent = new Intent(parent.getContext(), ProcessActivity.class);
                    int position = viewHolder.getAdapterPosition();
                    switch (position + 1) {
                        case Type.BEAUTY:
                            intent.putExtra("function", Type.BEAUTY);
                            break;
                        case Type.FIX:
                            intent.putExtra("function", Type.FIX);
                            break;
                        case Type.BILATERAL:
                            intent.putExtra("function", Type.BILATERAL);
                            break;
                        case Type.MEDIAN:
                            intent.putExtra("function", Type.MEDIAN);
                            break;
                        case Type.GRAY:
                            intent.putExtra("function", Type.GRAY);
                            break;
                        default:
                            intent = new Intent(parent.getContext(), AboutActivity.class);
                            break;
                    }


                    //v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();

                    ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f);
                    ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f);

                    AnimatorSet animSet = new AnimatorSet();
                    animSet.play(scaleX).with(scaleY);
                    animSet.setDuration(150);
                    animSet.start();

                    Intent finalIntent = intent;
                    animSet.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            parent.getContext().startActivity(finalIntent);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

                    break;
            }
            if (onItemTouchListener != null) {
                onItemTouchListener.onItemTouch(v, viewHolder.getAdapterPosition());
            }
            //v.onTouchEvent(event);
            return true;
        });
        //将view传递给我们自定义的ViewHolder，即返回这个ViewHolder实体
        return viewHolder;
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
    public void addData(int position, Bitmap CardImage, String CardTextTitle, String CardTextContent) {
        mList.add(position, new MainItem(CardImage, CardTextTitle, CardTextContent));
        notifyItemInserted(position);
    }

    //更改某个位置的数据
    public void changeData(int position, Bitmap CardImage, String CardTextTitle, String CardTextContent) {
        mList.set(position, new MainItem(CardImage, CardTextTitle, CardTextContent));
        notifyItemChanged(position);
    }
}

class MainItem {
    //Item对应图片的资源id，在drawable中
    private final Bitmap CardImage;

    //Item的TextView内容
    private final String CardTextTitle;
    private final String CardTextContent;

    //构造函数
    public MainItem(Bitmap CardImage, String CardTextTitle, String CardTextContent) {
        this.CardImage = CardImage;
        this.CardTextTitle = CardTextTitle;
        this.CardTextContent = CardTextContent;
    }

    //getter
    public Bitmap getCardImage() {
        return CardImage;
    }

    public String getCardTextTitle() {
        return CardTextTitle;
    }

    public String getCardTextContent() {
        return CardTextContent;
    }
}