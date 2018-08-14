package com.example.zxpay.maker_tea;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ParamterSettingActivity extends AppCompatActivity implements Button.OnClickListener{
    int ClickButtonId[] = {R.id.BT_BACK, R.id.BT_OK};

    EditText EditPiroid;

    Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paramter_setting);
        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitleAndImage("基本參數設定");

        for(int id:ClickButtonId){
            Button bt = (Button)findViewById(id);
            bt.setOnClickListener(this);
        }

        EditPiroid = (EditText) findViewById(R.id.EDITTEXT_PERIORD);
    }

    private void goToMainActivity(){
        Intent intent_back = new Intent();
        intent_back.setClass(this, MainActivity.class);
        finish();
    }

    private void setTitleAndImage(String titleName){
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.kidd_lite);
        getSupportActionBar().setTitle(titleName);

    }

    private void setToastAndShow(String Words){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), Words, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BT_BACK:
                goToMainActivity();
                break;
            case R.id.BT_OK:
                String numPiriodString = EditPiroid.getText().toString();
                int piriod = 3000;
                boolean flag = false;
                try {
                    setToastAndShow("Setting Period ...");
                    piriod = Integer.parseInt(numPiriodString)*1000;
                    flag = true;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                if(flag){
                    Intent it_save = new Intent(this, MainActivity.class);
                    it_save.putExtra("period", piriod/2);
                    setToastAndShow("OK");
                    startActivityForResult(it_save, 123);
                }
                else{
                    setToastAndShow("請輸入數字 ...");
                }
                break;
        }
    }
}
