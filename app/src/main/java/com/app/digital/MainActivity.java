package com.app.digital;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.app.digital.databinding.ActivityMainBinding;

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

        OpenCVLoader.initDebug();//OpenCV初始化后才能使用Mat等对象

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
            switch (position + 1) {
                case 1:
                    intent = new Intent(this,ProcessActivity.class);
                    intent.putExtra("function",1);
                    break;
                case 2:
                    intent = new Intent(this,ProcessActivity.class);
                    intent.putExtra("function",2);
                    break;
                case 3:
                    intent = new Intent(this,ProcessActivity.class);
                    intent.putExtra("function",3);
                    break;
                case 4:
                    intent = new Intent(this,ProcessActivity.class);
                    intent.putExtra("function",4);
                    break;
                case 5:
                    intent = new Intent(this,ProcessActivity.class);
                    intent.putExtra("function",5);
                    break;
            }
            startActivity(intent);
        });

        mainRecycleViewAdapter.setOnLongClickListener((view, position) -> {
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

        //一定一定要先创建ChannelID
        createNotificationChannel();
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
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.dog), getString(R.string.functionBeauty),  getString(R.string.functionBeautyContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.awalak), getString(R.string.functionOpen),  getString(R.string.functionOpenContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.dragon), getString(R.string.functionBilateral),  getString(R.string.functionBilateralContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.roach), getString(R.string.functionMedian),  getString(R.string.functionMedianContent))
        );

        mainItems.add(
                new MainItem(BitmapFactory.decodeResource(getResources(), R.drawable.sera2), getString(R.string.functionGraying),  getString(R.string.functionGrayingContent))
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

    private void createNotificationChannel() {
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
    private OnLongClickListener onLongClickListener;//长按监听器

    //设置点击监听
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener){
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

        MainItem list = mList.get(position);
        holder.CardImageView.setImageBitmap(list.getCardImage());
        holder.CardTextTitleView.setText(list.getCardTextTitle());
        holder.CardTextContentView.setText(list.getCardTextContent());

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

    MainRecycleViewAdapter(List<MainItem> list) {
        mList = list;
    }

    //创建ViewHolder并返回，后续item布局里控件都是从ViewHolder中取出，即加载布局
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //将自定义的item布局转换为View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_main_item, parent, false);
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