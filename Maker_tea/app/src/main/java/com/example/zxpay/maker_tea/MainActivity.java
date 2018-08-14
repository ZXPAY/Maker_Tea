package com.example.zxpay.maker_tea;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/*
   SettingData:
        Black_Tea:
                RawDataA:
                        POST:
                            taste:
                            tea:
                            time



   TestingData:
        Black_Tea:
                RawDataC
*/



public class MainActivity extends AppCompatActivity implements Button.OnClickListener, Button.OnLongClickListener{
    int IMGClickID[] = {R.id.IMG_SETTING};
    int IMGLongClickID[] = {R.id.IMG_TITLE};
    int SETTING_MODE = 0;
    int TESTING_MODE = 1;
    int QueryMode = TESTING_MODE;
    int CHOOSE_BLACK = 0;
    int CHOOSE_GREEN = 1;
    int CHOOSE_OOLONG = 2;
    int ChoosingMode = CHOOSE_BLACK;
    boolean AutoFlag = false;
    int TempCool = 40;
    int TempHot = 55;
    final int DEFAULT_BALCK_TEA_COLOR = 130;
    final int DEFAULT_GREEN_TEA_COLOR = 140;
    final int DEFAULT_OOLONG_TEA_COLOR = 150;
    int DEFAULT_POLLING_TIME = 1500;

    boolean switchTimerTestingOrSetting = true;
    Button AutoButton;
    String NewestSettingTea = "";
    TextView TXV_FinalUpdate, TXV_FinalRecord, TXV_Temperature, TXV_Color, TXV_Tea, TXV_Flavor;
    ImageView ImgTea;

    // Newest data will be saved.
    Map<String, String> DataSettingTaste = new HashMap<String, String>();
    Map<String, String> DataSettingTea = new HashMap<String, String>();

    Map<String, String> DataTestingColor = new HashMap<String, String>();
    Map<String, String> DataTestingColorB = new HashMap<String, String>();
    Map<String, String> DataTestingColorG = new HashMap<String, String>();
    Map<String, String> DataTestingColorR = new HashMap<String, String>();
    Map<String, String> DataTestingTea = new HashMap<String, String>();
    Map<String, String> DataTestingTemp = new HashMap<String, String>();

    Toast toast;

    String LastSettingTea = "QOO";

    ArrayList<String> timeRecord = new ArrayList();
    String getStringFlavorUsingIntent = "5";

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private long timei = 0;

    Timer timer = new Timer();

    MyQuery myQuery = new MyQuery();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(
                new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/SentyTEA.ttf");
        setContentView(R.layout.activity_main);

        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setTitleAndImage(getString(R.string.app_name));

        for(int id:IMGClickID){
            ImageView imgView = (ImageView) findViewById(id);
            imgView.setOnClickListener(this);
        }
        for(int id:IMGLongClickID){
            ImageView imgView = (ImageView) findViewById(id);
            imgView.setOnLongClickListener(this);
        }
        if(!isConnectToNetwork()){
            if(toast != null){
                toast.cancel();
            }
            toast = Toast.makeText(getApplicationContext(), "Please check the Internet ...", Toast.LENGTH_SHORT);
            toast.show();
        }

        ImgTea = (ImageView) findViewById(R.id.IMG_TEA);
        TXV_FinalUpdate = (TextView) findViewById(R.id.TXV_FINAL_UPDATE);
        TXV_FinalRecord = (TextView) findViewById(R.id.TXV_DATA_RECORD);
        TXV_Temperature = (TextView) findViewById(R.id.TXV_TEMPERATURE);
        TXV_Color = (TextView) findViewById(R.id.TXV_COLOR);
        TXV_Tea = (TextView) findViewById(R.id.CHOOSE_TEA);
        TXV_Flavor = (TextView) findViewById(R.id.TXV_FLAVOR);

//        AutoButton = (Button) findViewById(R.id.BT_AUTO);

        ArrayAdapter<CharSequence> myAdapter = ArrayAdapter.createFromResource(
                this, R.array.KindsOfTea, android.R.layout.simple_spinner_item );
        myAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        queryNewestTesting();

        int sendTeaNum = -1;
        try {
            Intent it_get = getIntent();
            getStringFlavorUsingIntent = it_get.getStringExtra("flavor");
            sendTeaNum = it_get.getIntExtra("tea", -1);
            closeTimer();
            DEFAULT_POLLING_TIME = it_get.getIntExtra("period", 1500);
            Log.e("Polling", String.valueOf(DEFAULT_POLLING_TIME));
            openTimer();
            switch (sendTeaNum){
                case 0:
                    TXV_Tea.setText("選擇茶葉：紅茶 Black Tea");
                    break;
                case 1:
                    TXV_Tea.setText("選擇茶葉：綠茶 Green Tea");
                    break;
                case 2:
                    TXV_Tea.setText("選擇茶葉：烏龍茶 Oolong Tea");
                    break;
            }
            Log.e("Flavor", getStringFlavorUsingIntent);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.MENU_ID:
                Log.e("Menu", "Setting");
                Intent itToParameter = new Intent(this, ParamterSettingActivity.class);
                startActivity(itToParameter);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }

    private void setTitleAndImage(String titleName){
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.kidd_lite);
        getSupportActionBar().setTitle(titleName);

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

    private void openTimer(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(switchTimerTestingOrSetting){
                    queryNewestSetting();
                    switchTimerTestingOrSetting = false;
                }
                else{
                    queryNewestTesting();
                    switchTimerTestingOrSetting = true;
                }
                if(!isConnectToNetwork()){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if(toast != null){
                                toast.cancel();
                            }
                            toast = Toast.makeText(getApplicationContext(), "Please check the Internet ...", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
            }
        },DEFAULT_POLLING_TIME/2, DEFAULT_POLLING_TIME);
    }

    private void closeTimer(){
        timer.cancel();
    }

    private void setNiceTeaImg(){
        // 40 ~ 55
        ImgTea.setImageResource(R.drawable.nice_lite);
    }
    private void setHotTeaImg(){
        // Temperature >= 55
        ImgTea.setImageResource(R.drawable.hot_lite);
    }
    private void setCoolTeaImg(){
        // Temperature <= 40
        ImgTea.setImageResource(R.drawable.cool_lite);
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.IMG_TITLE:
                Log.e("Button", "Title long click");
                break;
        }
        return false;
    }

    private class MyQuery{
        /*
        Example below:
        CollectionReference ref = db.collection("/OutDoor/0000000012000007/CO2");
        MyQuery myquery = new MyQuery();
        myquery.mode = 1;
        myquery.Query(ref, "time", "<=", "2018-06-17 15:00:00", 10, "DESCENDING");
         */
        ArrayList<String> Data_Array = new ArrayList<>();
        int mode = 0;
        int query_numbers = 0;
        int VERBOSE = 1;

        /*
        This method can query data from firebase.
        key:parameter you want to query.
        operator:>,<,>=,== and so on, you can use these operator to find the data you want to query.
        object:key operator than what, for example temperature is key, > is operator, and value is object.
        num:how manny data you want to get
        direction:Ascending or Descending you can choose.
     */
        private void Query(CollectionReference ref, String key, String operator, String object, final int num, String direct){
            Data_Array = new ArrayList<>();
            query_numbers = num;
            // Default Query
            Query myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);;
            if(operator.equals(">")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }
            else if(operator.equals(">=")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereGreaterThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereGreaterThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }
            else if(operator.equals("<")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereLessThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereLessThan(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }
            else if(operator.equals("<=")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereLessThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereLessThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }
            else if(operator.equals("==")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }

            myQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        Log.e("Query", "Success");
                        QuerySnapshot QSnap = task.getResult();
                        if(!QSnap.isEmpty()){
                            for(int CNT=0;CNT<num;CNT++){
                                try {
                                    //Log.e("Query Data", String.valueOf(task.getResult().getDocuments().get(CNT).getData()));
                                    String Data = String.valueOf(task.getResult().getDocuments().get(CNT).getData());
                                    Data_Array.add(Data);
                                }
                                catch (Exception e){
                                    Log.e("Query Data", "Error");
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Run_Command();
                            }
                            catch (Exception e){
                                e.printStackTrace();
                                setToastAndShow("Run Command Error ...");
                            }
                            Data_Array = new ArrayList<>();
                        }
                        else{
                            Log.e("Query", "QSnap is Empty ...");
                        }
                    }
                    else{
                        Log.e("Query", "Failure");
                    }
                }
            });
        }

        /*
            This method will run when query is finished.
            You can set mode to run the code.
         */
        private void Run_Command(){
            switch (mode){
                case 0:
                    Log.e("Run", "Start~~~");
                    break;
                case 1:
                    /*
                        Setting:
                            taste
                            tea
                            time
                        Testing:
                            color
                            colorB
                            colorG
                            tea
                            temp
                            time
                     */
                    ArrayList<String> taste = new ArrayList<>();
                    ArrayList<String> tea = new ArrayList<>();
                    ArrayList<String> time = new ArrayList<>();
                    ArrayList<String> color = new ArrayList<>();
                    ArrayList<String> colorB = new ArrayList<>();
                    ArrayList<String> colorG = new ArrayList<>();
                    ArrayList<String> colorR = new ArrayList<>();
                    ArrayList<String> temp = new ArrayList<>();
                    for(String data:Data_Array){
                        //Log.e("Data", data);
                        String data_split[] = data.substring(1,data.length()-1).split(",");
                        for(String d:data_split){
                            String each_data[] = d.split("=");
                            for(int i=0;i<each_data.length;i++){
                                try {
                                    //Log.e("data", each_data[i]);
                                    if(each_data[i].replaceAll("\\s", "").equals(getString(R.string.keyTaste))){
                                        if(VERBOSE==1) Log.e("taste:", each_data[i+1]);
                                        taste.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals(getString(R.string.keyTea))){
                                        if(VERBOSE==1) Log.e("tea:", each_data[i+1]);
                                        tea.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals(getString(R.string.keyTime))){
                                        if(VERBOSE==1) Log.e("time:", each_data[i+1]);
                                        time.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals(getString(R.string.keyColor))){
                                        if(VERBOSE==1) Log.e("color:", each_data[i+1]);
                                        color.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals(getString(R.string.keyColorB))){
                                        if(VERBOSE==1) Log.e("colorB:", each_data[i+1]);
                                        colorB.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals(getString(R.string.keyColorG))){
                                        if(VERBOSE==1) Log.e("colorG:", each_data[i+1]);
                                        colorG.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals(getString(R.string.keyColorR))){
                                        if(VERBOSE==1) Log.e("colorR:", each_data[i+1]);
                                        colorR.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals(getString(R.string.keyTemp))){
                                        if(VERBOSE==1) Log.e("colorR:", each_data[i+1]);
                                        temp.add(each_data[i+1]);
                                    }
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if(!time.isEmpty()){
                        if(QueryMode==SETTING_MODE){
                            timeRecord.add(time.get(0));
                        }
                        else if(QueryMode==TESTING_MODE) {
                            for (int pos = 0; pos < time.size(); pos++) {
                                TXV_FinalRecord.setText("數據紀錄時間：" + time.get(pos));
                            }
                        }
                    }
                    if(!taste.isEmpty()){
                        if(QueryMode==SETTING_MODE){
                            for(int pos=0;pos<taste.size();pos++){
                                if(VERBOSE==2) Log.e(time.get(pos), "Taste:"+taste.get(pos));
                                DataSettingTaste.put(time.get(pos), taste.get(pos));
                            }
                        }
                        else if(QueryMode==TESTING_MODE){
                            for(int pos=0;pos<taste.size();pos++){
                                // No data in this database
                            }
                        }
                    }
                    if(!tea.isEmpty()){
                        if(QueryMode==SETTING_MODE){
                            for(int pos=0;pos<tea.size();pos++){
                                if(VERBOSE==2) Log.e(time.get(pos), "T"+tea.get(pos));
                                DataSettingTea.put(time.get(pos), tea.get(pos));
                                LastSettingTea = tea.get(pos);
                                setChooseMode(tea.get(pos));
                            }
                        }
                        else if(QueryMode==TESTING_MODE){
                            for(int pos=0;pos<tea.size();pos++){
                                if(VERBOSE==2) Log.e(time.get(pos), "T"+tea.get(pos));
                                DataTestingTea.put(time.get(pos), tea.get(pos));

                            }
                        }
                    }
                    if(!color.isEmpty()){
                        if(QueryMode==SETTING_MODE){
                            for(int pos=0;pos<color.size();pos++){
                                // No data in this database
                            }
                        }
                        else if(QueryMode==TESTING_MODE){
                            for(int pos=0;pos<color.size();pos++){
                                if(VERBOSE==2) Log.e(time.get(pos), "C"+color.get(pos));
                                DataTestingColor.put(time.get(pos), color.get(pos));
                                TXV_Color.setText(String.format("%.1f", Float.parseFloat(color.get(pos))));
                                setColorDataColor(Float.parseFloat(color.get(pos)));
                            }
                        }
                    }
                    if(!colorB.isEmpty()){
                        if(QueryMode==SETTING_MODE){
                            for(int pos=0;pos<colorB.size();pos++){
                                // No data in this database
                            }
                        }
                        else if(QueryMode==TESTING_MODE){
                            for(int pos=0;pos<colorB.size();pos++){
                                if(VERBOSE==2) Log.e(time.get(pos), "B"+colorB.get(pos));
                                DataTestingColorB.put(time.get(pos), colorB.get(pos));
                            }
                        }
                    }
                    if(!colorG.isEmpty()){
                        if(QueryMode==SETTING_MODE){
                            for(int pos=0;pos<colorG.size();pos++){
                                // No data in this database
                            }
                        }
                        else if(QueryMode==TESTING_MODE){
                            for(int pos=0;pos<colorG.size();pos++){
                                if(VERBOSE==2) Log.e(time.get(pos), "G"+colorG.get(pos));
                                DataTestingColorG.put(time.get(pos), colorG.get(pos));
                            }
                        }
                    }
                    if(!colorR.isEmpty()){
                        if(QueryMode==SETTING_MODE){
                            for(int pos=0;pos<colorR.size();pos++){
                                // No data in this database
                            }
                        }
                        else if(QueryMode==TESTING_MODE){
                            for(int pos=0;pos<colorR.size();pos++){
                                if(VERBOSE==2) Log.e(time.get(pos), "R"+colorR.get(pos));
                                DataTestingColorR.put(time.get(pos), colorR.get(pos));
                            }
                        }
                    }
                    if(!temp.isEmpty()){
                        if(QueryMode==SETTING_MODE){
                            for(int pos=0;pos<temp.size();pos++){
                                // No data in this database
                            }
                        }
                        else if(QueryMode==TESTING_MODE){
                            for(int pos=0;pos<temp.size();pos++){
                                if(VERBOSE==2) Log.e(time.get(pos), "Tp"+temp.get(pos));
                                DataTestingTemp.put(time.get(pos), temp.get(pos));
                                TXV_Temperature.setText(String.format("%.1f", Float.parseFloat(temp.get(pos)))+
                                        " " + getString(R.string.temperature_unit));
                                if(Float.parseFloat(temp.get(pos)) > TempHot){
                                    setHotTeaImg();
                                    TXV_Temperature.setTextColor(getResources().getColor(R.color.HotTeaColor));
                                }
                                else if(Float.parseFloat(temp.get(pos)) > TempCool){
                                    setNiceTeaImg();
                                    TXV_Temperature.setTextColor(getResources().getColor(R.color.NiceTeaColor));
                                }
                                else{
                                    setCoolTeaImg();
                                    TXV_Temperature.setTextColor(getResources().getColor(R.color.CoolTeaColor));
                                }
                            }
                        }
                    }
                    break;
                case 2: // testing
                    for(String data:Data_Array){
                        Log.e("Data", data);
                    }
                    break;
            }

        }
    }

    private void setColorDataColor(float colorData){
        float calculateToFlavor = 100;
        if(ChoosingMode == CHOOSE_BLACK){
            calculateToFlavor = (colorData/DEFAULT_BALCK_TEA_COLOR-1) * 10 + 5;
        }
        else if(ChoosingMode == CHOOSE_GREEN){
            calculateToFlavor = (colorData/DEFAULT_GREEN_TEA_COLOR-1) * 10 + 5;
        }
        else if(ChoosingMode == CHOOSE_OOLONG){
            calculateToFlavor = (colorData/DEFAULT_OOLONG_TEA_COLOR-1) * 10 + 5;
        }
        Log.e("ColorDataColor", String.format("%.2f", calculateToFlavor));

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
        int num = Integer.parseInt(String.format("%.0f", calculateToFlavor));
        TXV_Color.setTextColor(ColorArray[num]);
    }

    private void setChooseMode(String ChooseString){
        if(ChooseString.equals(getString(R.string.keyKindsOfBlackTea))){
            ChoosingMode = CHOOSE_BLACK;
        }
        else if(ChooseString.equals(getString(R.string.keyKindsOfGreenTea))){
            ChoosingMode = CHOOSE_GREEN;
        }
        else if(ChooseString.equals(getString(R.string.keyKindsOfOolongTea))){
            ChoosingMode = CHOOSE_OOLONG;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
                //Image View click
            case R.id.IMG_SETTING:
                Log.e("ImageView", "Setting");
                if(toast != null){
                    toast.cancel();
                }
                toast = Toast.makeText(getApplicationContext(), "Go to send setting data.", Toast.LENGTH_SHORT);
                toast.show();
                Intent it_save = new Intent(this, SendSettingActivity.class);
                it_save.putExtra("tea", NewestSettingTea);
                it_save.putExtra("flavor", getStringFlavorUsingIntent);
                Log.e("SettingTea", NewestSettingTea);
                startActivityForResult(it_save, 123);
                break;
        }
    }

    public String Get_Now(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today).toString();
    }

    public void queryNewestSetting(){
        QueryMode = SETTING_MODE;
        myQuery.mode = 1;
        CollectionReference ref = db.collection(getString(R.string.SettingBlackTeaA));
        myQuery.Query(ref, "time", "<=", Get_Now(), 1, "DESCENDING");

        myQuery.mode = 1;
        ref = db.collection(getString(R.string.SettingGreenTeaA));
        myQuery.Query(ref, "time", "<=", Get_Now(), 1, "DESCENDING");

        myQuery.mode = 1;
        ref = db.collection(getString(R.string.SettingOolongTeaA));
        myQuery.Query(ref, "time", "<=", Get_Now(), 1, "DESCENDING");

        final String[] StringTeaArray = getResources().getStringArray(R.array.KindsOfTea);
        if(LastSettingTea.equals(getString(R.string.keyKindsOfBlackTea))){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TXV_Tea.setText("選擇茶葉：" + StringTeaArray[0]);
                    if(getStringFlavorUsingIntent!=null){
                        TXV_Flavor.setText(getStringFlavorUsingIntent);
                        setFlavorNumberColor(Integer.parseInt(getStringFlavorUsingIntent));
                    }
                }
            });
            Log.e("Setting", "選擇茶葉：" + StringTeaArray[0]);
            NewestSettingTea = StringTeaArray[0];
            ChoosingMode = CHOOSE_BLACK;
        }
        else if(LastSettingTea.equals(getString(R.string.keyKindsOfGreenTea))){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TXV_Tea.setText("選擇茶葉：" + StringTeaArray[1]);
                    if(getStringFlavorUsingIntent!=null){
                        TXV_Flavor.setText(getStringFlavorUsingIntent);
                        setFlavorNumberColor(Integer.parseInt(getStringFlavorUsingIntent));
                    }
                }
            });
            Log.e("Setting", "選擇茶葉：" + StringTeaArray[1]);
            NewestSettingTea = StringTeaArray[1];
            ChoosingMode = CHOOSE_GREEN;
        }
        else if(LastSettingTea.equals(getString(R.string.keyKindsOfOolongTea))){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TXV_Tea.setText("選擇茶葉：" + StringTeaArray[2]);
                    if(getStringFlavorUsingIntent!=null){
                        TXV_Flavor.setText(getStringFlavorUsingIntent);
                        setFlavorNumberColor(Integer.parseInt(getStringFlavorUsingIntent));
                    }
                }
            });
            Log.e("Setting", "選擇茶葉：" + StringTeaArray[2]);
            NewestSettingTea = StringTeaArray[2];
            ChoosingMode = CHOOSE_OOLONG;
        }
        else{
            Log.e("Setting", "No Choose ..." + LastSettingTea);
        }

        LastSettingTea = "QOO";

    }
    private void setFlavorNumberColor(int flavorLevel){
        Log.e("Flavor", "SettingColor");
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
        TXV_Flavor.setTextColor(ColorArray[flavorLevel-1]);

    }

    public boolean isAfterDate(String time1,String time2) throws ParseException {
        //如果想比较日期则写成"yyyy-MM-dd"就可以了
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //将字符串形式的时间转化为Date类型的时间
        Date a = sdf.parse(time1);
        Date b = sdf.parse(time2);
        //如果 time1 比 time2 還要晚, 回傳true
        if (a.after(b))
            return true;
        else
            return false;
    }

    private int getNewestSettingTea(){
        /*
            Black Tea => Green Tea => Oolong Tea 依序抓取時間，找最新的
            執行次數 : C3取2
         */
        final int MAX_COMPARE_NUM = 2;
        for(int i=0;i<3;i++){
            int counter = 0;
            for(int j=0;j<2;j++){
                int c=i;
                while(c!=i){
                    c++;
                }
                try{
                    if(isAfterDate(timeRecord.get(i), timeRecord.get(c))){
                        counter++;
                    }
                }
                catch (ParseException e){
                    Log.e("CompareDate", e.toString());
                }
                if(counter==2) return i;
            }

        }
        return -1;
    }

    public void queryNewestTesting(){
        QueryMode = TESTING_MODE;
        String colletionString = getString(R.string.TestingBlackTeaC);

        if(ChoosingMode == CHOOSE_BLACK){
            colletionString = getString(R.string.TestingBlackTeaC);
        }
        else if(ChoosingMode == CHOOSE_GREEN){
            colletionString = getString(R.string.TestingGreenTeaC);
        }
        else if(ChoosingMode == CHOOSE_OOLONG){
            colletionString = getString(R.string.TestingOolongTeaC);
        }

        Log.e("Query", String.valueOf(ChoosingMode));
        myQuery.mode = 1;
        CollectionReference ref = db.collection(colletionString);
        myQuery.Query(ref, "time", "<=", Get_Now(), 1, "DESCENDING");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TXV_FinalUpdate.setText("最後更新時間："+Get_Now());
            }
        });
    }

    private void testQueryData(){
        MyQuery testQuery = new MyQuery();
        int showNumber = 5;
        String[] settingStringArray = getResources().getStringArray(R.array.SettingTea);
        String[] testingStringArray = getResources().getStringArray(R.array.TestingTea);

        Log.e("Test", "QuerySetting");
        for(String colletionString:settingStringArray){
            testQuery.mode = 2;
            CollectionReference ref = db.collection(colletionString);
            testQuery.Query(ref, "time", "<=", Get_Now(), showNumber, "DESCENDING");
        }

        Log.e("Test", "QueryTesting");
        for(String colletionString:testingStringArray){
            testQuery.mode = 2;
            CollectionReference ref = db.collection(colletionString);
            testQuery.Query(ref, "time", "<=", Get_Now(), showNumber, "DESCENDING");
        }

    }

    private void setToastAndShow(String Words){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), Words, Toast.LENGTH_SHORT);
        toast.show();
    }

}

