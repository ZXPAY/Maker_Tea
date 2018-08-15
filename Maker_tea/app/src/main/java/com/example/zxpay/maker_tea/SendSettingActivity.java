package com.example.zxpay.maker_tea;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SendSettingActivity extends AppCompatActivity implements Button.OnClickListener, SeekBar.OnSeekBarChangeListener,
        AdapterView.OnItemSelectedListener, NumberPicker.OnValueChangeListener {

    int ImgID[] = {R.id.IMG_BACK};
    int BtID[] = {R.id.BT_SEND};
    Toast toast;
    final int DEFAULT_BALCK_TEA_COLOR = 130;
    final int DEFAULT_GREEN_TEA_COLOR = 140;
    final int DEFAULT_OOLONG_TEA_COLOR = 150;
    final int DEFAULT_SEEKBAR_PROGRESS = 5;
    int SETTING_MODE = 0;
    int TESTING_MODE = 1;
    int CHOOSE_BLACK = 0;
    int CHOOSE_GREEN = 1;
    int CHOOSE_OOLONG = 2;
    int ChoosingMode = CHOOSE_BLACK;
    boolean runOneTimeFlag = true;
    TextView TXV_COLOR;
    SeekBar mySeekbar;
    Spinner spinnerTea, spinnerFlavor;
    ArrayAdapter<CharSequence> AdapterFlavor, AdapterTea;
    String sendIntentFlavor = "5";

    String sendColor = "";
    String it_teaString = null;
    String it_flavorString = null;

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_setting);
        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitleAndImage("茶參數設定");

        if(!isConnectToNetwork()){
            if(toast != null){
                toast.cancel();
            }
            toast = Toast.makeText(getApplicationContext(), "Please check the Internet ...", Toast.LENGTH_SHORT);
            toast.show();
        }

        TXV_COLOR = findViewById(R.id.TXV_COLORSHOW);
        setTextViewBold(TXV_COLOR);

        for(int id:ImgID){
            ImageView imgBt = (ImageView) findViewById(id);
            imgBt.setOnClickListener(this);
        }
        for(int id:BtID){
            Button Bt = (Button) findViewById(id);
            Bt.setOnClickListener(this);
        }

        setSpinnerTea();
        setSpinnerFlavor();
        mySeekbar = (SeekBar) findViewById(R.id.SEEKBAR);
        mySeekbar.setOnSeekBarChangeListener(this);

        try {
            Intent it_getIntent = getIntent();
            it_teaString = it_getIntent.getStringExtra("tea");
            it_flavorString = it_getIntent.getStringExtra("flavor");
            Log.e("SettingItent", it_teaString+","+it_flavorString);
            if(it_teaString.equals(getString(R.string.KindsOfBlackTea))){
                setDefaultBlackTea();
            }
            else if(it_teaString.equals(getString(R.string.KindsOfGreenTea))){
                setDefaultGreenTea();
            }
            else if(it_teaString.equals(getString(R.string.KindsOfOolongTea))){
                setDefaultOolongTea();
            }
            if(it_flavorString==(null)){

            }
            else{
                Log.e("Flavor", "asfsaf");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        setFlavorNumberColor(mySeekbar.getProgress());

    }

    private void setSpinnerTea(){
        spinnerTea = (Spinner) findViewById(R.id.SPINNER_TEA);
        AdapterTea = ArrayAdapter.createFromResource(
                this, R.array.KindsOfTea, R.layout.spinner_item);
        AdapterTea.setDropDownViewResource(R.layout.spinner_item);
        spinnerTea.setAdapter(AdapterTea);
        spinnerTea.setOnItemSelectedListener(this);
    }

    private void setSpinnerFlavor(){
        spinnerFlavor = (Spinner) findViewById(R.id.SPINNER_FLAVOR);
        AdapterFlavor = ArrayAdapter.createFromResource(
                this, R.array.FlavorNumber, R.layout.spinner_item);
        AdapterFlavor.setDropDownViewResource(R.layout.spinner_item);
        spinnerFlavor.setAdapter(AdapterFlavor);
        spinnerFlavor.setOnItemSelectedListener(this);
        AdapterFlavor.notifyDataSetChanged();
        spinnerFlavor.setSelection(4);
    }

    private boolean isConnectToNetwork(){
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

        //如果未連線的話，mNetworkInfo會等於null
        if(mNetworkInfo != null) {
            //網路是否已連線(true or false)
            mNetworkInfo.isConnected();
            //網路連線方式名稱(WIFI or mobile)
            mNetworkInfo.getTypeName();
            //網路連線狀態
            mNetworkInfo.getState();
            //網路是否可使用
            mNetworkInfo.isAvailable();
            //網路是否已連接or連線中
            mNetworkInfo.isConnectedOrConnecting();
            //網路是否故障有問題
            mNetworkInfo.isFailover();
            //網路是否在漫遊模式
            mNetworkInfo.isRoaming();

            return true;
        }
        else{
            return false;
        }
    }

    private void setTextViewBold(TextView myTXV){
        myTXV.setTypeface(myTXV.getTypeface(), Typeface.BOLD);
    }

    private void setSendDataToFirebase(){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), "Sending ...", Toast.LENGTH_LONG);
        toast.show();

        String colletionString = getString(R.string.SettingName);

        String sendTime = Get_Now();
        String sendTea = getString(R.string.keyKindsOfBlackTea);
        String sendFlavor = String.valueOf(mySeekbar.getProgress());


        if(ChoosingMode == CHOOSE_BLACK){
            colletionString = getString(R.string.SettingBlackTeaA);
            sendTea = getString(R.string.keyKindsOfBlackTea);
        }
        else if(ChoosingMode == CHOOSE_GREEN){
            colletionString = getString(R.string.SettingGreenTeaA);
            sendTea = getString(R.string.keyKindsOfGreenTea);
        }
        else if(ChoosingMode == CHOOSE_OOLONG){
            colletionString = getString(R.string.SettingOolongTeaA);
            sendTea = getString(R.string.keyKindsOfOolongTea);
        }
        else{
            if(toast != null){
                toast.cancel();
            }
            toast = Toast.makeText(getApplicationContext(), "找不到選擇的茶種...", Toast.LENGTH_SHORT);
            toast.show();
        }

        Map<String, Object> sendMap = new HashMap<>();
        sendMap.put(getString(R.string.keySettingtime), sendTime);
        sendMap.put(getString(R.string.keySettingcolor), sendColor);
        sendMap.put(getString(R.string.keySettingtaste), sendFlavor);
        sendMap.put(getString(R.string.keySettingtea), sendTea);

        Log.e("Send", getString(R.string.keySettingtime)+":"+sendTime);
        Log.e("Send", getString(R.string.keySettingcolor)+":"+sendColor);
        Log.e("Send", getString(R.string.keySettingtaste)+":" + sendFlavor);
        Log.e("Send", getString(R.string.keySettingtea)+":" + sendTea);

        CollectionReference ref = db.collection(colletionString);

        ref.add(sendMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.e("Send", "Success");
                if(toast != null){
                    toast.cancel();
                }
                toast = Toast.makeText(getApplicationContext(), "Setting Successful！", Toast.LENGTH_SHORT);
                toast.show();
                goToMainActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Send", "Fail");
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.IMG_BACK:
                goToMainActivity();
                break;
            case R.id.BT_SEND:
                setSendDataToFirebase();
                sendIntentFlavor = String.valueOf(mySeekbar.getProgress());
                Intent it_save = new Intent(this, MainActivity.class);
                it_save.putExtra("flavor", sendIntentFlavor);
                it_save.putExtra("tea", ChoosingMode);
                startActivityForResult(it_save, 321);
                break;
        }
    }

    private void goToMainActivity(){
        Intent intent_back = new Intent();
        intent_back.setClass(SendSettingActivity.this, MainActivity.class);
        finish();
    }

    private void setTitleAndImage(String titleName){
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.kidd_lite);
        getSupportActionBar().setTitle(titleName);

    }

    // On Seekbar change listener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.e("Seekbar", "Change -> " + mySeekbar.getProgress());
        if(mySeekbar.getProgress()==0) mySeekbar.setProgress(1);
        spinnerFlavor.setAdapter(AdapterFlavor);
        AdapterFlavor.notifyDataSetChanged();
        spinnerFlavor.setSelection(mySeekbar.getProgress()-1);

        float magnification = (Float.parseFloat(String.valueOf(mySeekbar.getProgress()))-5)/10;
        Log.e("Setting", String.format("%.2f", magnification));
        setFlavorNumberColor(mySeekbar.getProgress());
        if(ChoosingMode == CHOOSE_BLACK){
            sendColor = String.format("%.0f", DEFAULT_BALCK_TEA_COLOR * (1+magnification));
            TXV_COLOR.setText(sendColor);
        }
        else if(ChoosingMode == CHOOSE_GREEN){
            sendColor = String.format("%.0f", DEFAULT_GREEN_TEA_COLOR * (1+magnification));
            TXV_COLOR.setText(sendColor);
        }
        else if(ChoosingMode == CHOOSE_OOLONG){
            sendColor = String.format("%.0f", DEFAULT_OOLONG_TEA_COLOR * (1+magnification));
            TXV_COLOR.setText(sendColor);
        }
        else{
            if(toast != null){
                toast.cancel();
            }
            toast = Toast.makeText(getApplicationContext(), "找不到選擇的茶種...", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    void setDefaultBlackTea(){
        ChoosingMode = CHOOSE_BLACK;
        AdapterTea.notifyDataSetChanged();
        spinnerTea.setSelection(0);
        TXV_COLOR.setText(String.valueOf(DEFAULT_BALCK_TEA_COLOR));
        mySeekbar.setProgress(DEFAULT_SEEKBAR_PROGRESS);
    }
    void setDefaultGreenTea(){
        ChoosingMode = CHOOSE_GREEN;
        AdapterTea.notifyDataSetChanged();
        spinnerTea.setSelection(1);
        TXV_COLOR.setText(String.valueOf(DEFAULT_GREEN_TEA_COLOR));
        mySeekbar.setProgress(DEFAULT_SEEKBAR_PROGRESS);
    }
    void setDefaultOolongTea(){
        ChoosingMode = CHOOSE_OOLONG;
        AdapterTea.notifyDataSetChanged();
        spinnerTea.setSelection(2);
        TXV_COLOR.setText(String.valueOf(DEFAULT_OOLONG_TEA_COLOR));
        mySeekbar.setProgress(DEFAULT_SEEKBAR_PROGRESS);
    }
    // Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.SPINNER_TEA:
                switch (position){
                    case 0:
                        Log.e("Choose", "Black tea");
                        setDefaultBlackTea();
                        break;
                    case 1:
                        Log.e("Choose", "Green tea");
                        setDefaultGreenTea();
                        break;
                    case 2:
                        Log.e("Choose", "Oolong tea");
                        setDefaultOolongTea();
                        break;
                }

                break;
            case R.id.SPINNER_FLAVOR:
                Log.e("SpinnerFlavor", String.valueOf(position+1));
                mySeekbar.setProgress(position+1);
                try{
                    if(runOneTimeFlag){
                        spinnerFlavor.setSelection(Integer.parseInt(it_flavorString)-1);
                        runOneTimeFlag = false;
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                break;
        }
        // Log.e("Spinner", "Select ...");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Log.e("Spinner", "No Select ...");
    }

    public String Get_Now(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today).toString();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }

    private void setFlavorNumberColor(int flavorLevel){
        int ColorArray[] = {getResources().getColor(R.color.flavorLevel1),
                getResources().getColor(R.color.flavorLevel2),
                getResources().getColor(R.color.flavorLevel3),
                getResources().getColor(R.color.flavorLevel4),
                getResources().getColor(R.color.flavorLevel5),
                getResources().getColor(R.color.flavorLevel6),
                getResources().getColor(R.color.flavorLevel7),
                getResources().getColor(R.color.flavorLevel8),
                getResources().getColor(R.color.flavorLevel9),
                getResources().getColor(R.color.flavorLevel10)};
        TXV_COLOR.setTextColor(ColorArray[flavorLevel-1]);

    }
}
