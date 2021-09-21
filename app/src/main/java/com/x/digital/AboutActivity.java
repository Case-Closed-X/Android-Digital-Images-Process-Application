package com.x.digital;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.x.digital.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String urlGithub = "https://github.com/Case-Closed-X/Android-Digital-Images-Process-Application";
        String urlOutlook = "mailto:CaseClosedX@outlook.com";
        String urlOpenCV = "https://opencv.org";

        binding.cardViewAbout.setOnClickListener(v -> Toast.makeText(this,"当前版本：v1.0",Toast.LENGTH_SHORT).show());

        binding.cardViewGithub.setOnClickListener(v -> {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlGithub));
            startActivity(intent);
        });

        binding.cardViewEmail.setOnClickListener(v -> {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlOutlook));
            startActivity(intent);
        });

        binding.cardViewOpenCV.setOnClickListener(v -> {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlOpenCV));
            startActivity(intent);
        });

        /*//借助SpannableString类实现超链接文字
        binding.cardTextGithub.setText(getClickableSpan());
        //设置超链接可点击
        binding.cardTextGithub.setMovementMethod(LinkMovementMethod.getInstance());*/
    }

    /**
     * 获取可点击的SpannableString
     * @return
     */
    /*private SpannableString getClickableSpan() {
        String url = "https://github.com/Case-Closed-X/Android-Digital-Images-Process-Application";
        SpannableString spannableString = new SpannableString(getString(R.string.about_github));
        //设置下划线文字
        spannableString.setSpan(new UnderlineSpan(), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字的单击事件
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Uri uri=Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        }, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字的前景色
        spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.white)), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }*/
}