package com.example.testjni.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.testjni.R;
import com.example.testjni.ZTSystem;


public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);


        Button buttonTX = findViewById(R.id.button_tx_test);
        buttonTX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, ComActivity.class));
            }
        });

        Button buttonUSB = findViewById(R.id.button_usb_test);
        buttonUSB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });
    }

    //数据回传  InforSetActivity-->MainActivity
    //3.调取方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //判断数据的来源
        if (requestCode == 1) {
            //判断结果
            if (resultCode == RESULT_OK) {
                /*//读取数据
                String nickname = data.getStringExtra("nickname");
                String age = data.getStringExtra("age");
                String sex = data.getStringExtra("sex");
                String hobby= data.getStringExtra("hobby");
                String career = data.getStringExtra("career");


                tvShow.setText("昵称:"+nickname+"\n年龄:"+age
                        +"\n性别:"+sex+"\n爱好:"
                        +hobby+"\n职业:"+career);*/

            }
        }


    }

}