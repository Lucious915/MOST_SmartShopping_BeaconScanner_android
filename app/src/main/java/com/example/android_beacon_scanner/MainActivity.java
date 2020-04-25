package com.example.android_beacon_scanner;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.bluetooth.BluetoothAdapter;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.android_beacon_scanner.btlescan.adapters.LeDeviceListAdapter;
import com.example.android_beacon_scanner.btlescan.containers.BluetoothLeDeviceStore;
import com.example.android_beacon_scanner.btlescan.util.BluetoothLeScanner;
import com.example.android_beacon_scanner.btlescan.util.BluetoothUtils;
import com.example.android_beacon_scanner.btlescan.util.Calculation;
import com.example.android_beacon_scanner.btlescan.util.CountObj;
import com.example.android_beacon_scanner.btlescan.util.TimeFormatter;
import com.example.android_beacon_scanner.btlescan.util.WriteFileData;
import com.example.android_beacon_scanner.libs.bluetoothlelib.device.BluetoothLeDevice;
import com.example.android_beacon_scanner.libs.bluetoothlelib.device.beacon.BeaconType;
import com.example.android_beacon_scanner.libs.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;
import com.example.android_beacon_scanner.libs.bluetoothlelib.device.beacon.BeaconUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.SharedPreferences;

import uk.co.alt236.easycursor.objectcursor.EasyObjectCursor;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btn_finish) protected Button btn_finish;
    @BindView(R.id.btn_save) protected Button btn_save;
    @BindView(R.id.empty) protected View mEmpty;
    @BindView(R.id.list) protected ListView mList;
    @BindView(R.id.list_file) protected ListView mList_file;
    @BindView(R.id.tvItemCount) protected TextView mTvItemCount;
    @BindView(R.id.etSetTime) protected EditText mSetTime;
    @BindView(R.id.receive_bt) protected ToggleButton receive;
    @BindView(R.id.checkBox_loop) protected CheckBox loopChk;
    @BindView(R.id.checkBox_writefile) protected CheckBox Ifwrite;
    @BindView(R.id.edt_X) protected EditText edtX;
    @BindView(R.id.edt_Y) protected EditText edtY;
    @BindView(R.id.edt_label) protected EditText edtLbl;
    @BindView(R.id.btn_scan) protected Button btn_scan;
    @BindView(R.id.tv_save_dir_to) protected TextView tv_save_dir_to;
    @BindView(R.id.edt_major) protected EditText edt_major;
    @BindColor(R.color.colorAccent) int Color_accent;
    @BindColor(R.color.colorPrimary) int Color_primary;

    final int INPUT_NUM = 26;

    Calculation calculation;
    private boolean running = false;

    private NotificationManager manger;
    private Notification notification;

    private BluetoothUtils mBluetoothUtils;
    private BluetoothLeScanner mScanner;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothLeDeviceStore mDeviceStore;

    private Handler handler;
    private HandlerThread handlerThread;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_EXTERNEL_STORAGE = 1;

    File file;
    FileOutputStream fOut;
    OutputStreamWriter myOutWriter;


    CountObj[] CObj = new CountObj[INPUT_NUM];

    double[] beforearr = new double[27];
    boolean exec_1st = true;

    private int TIME_COUNT = 500;
    private int AMOUNT_COUNT = 1000;
    private int lognum = 0;
    int temp = 0;
    int label = 0;
    int major_filter=0;

    public WriteFileData mWriteFileData = new WriteFileData();
    public List<WriteFileData> wtdArray = new ArrayList<>();

    public static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH-mm-ss", Locale.TAIWAN);
    private final static String FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/positioning/";
    public static String FILENAME = sdf.format(System.currentTimeMillis());

    ToneGenerator toneG;

    //定義使用手機Sensor---------------------------------------------------------------------------------------------------------------------
//    private SensorManager sensorManager;
//    private Sensor g_sensor,m_sensor,gyroscope;
//    private float[] mGravity = new float[3];
//    private float[] mGeomagnetic = new float[3];
//    private float[] R_matrix = new float[9];
//    private float[] I_matrix = new float[9];
//    private float azimuth;
//    private float azimuthFix=0;
//    private float azimuth_normalize;


    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis());
            if (BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON) {
                try {
                    final IBeaconDevice beacon = new IBeaconDevice(deviceLe);

                    mDeviceStore.addDevice(deviceLe);

                    mWriteFileData = new WriteFileData();
                    mWriteFileData.TimeStamp = TimeFormatter.getIsoDateTime(beacon.getTimestamp());
                    mWriteFileData.Minor = beacon.getMinor();
                    mWriteFileData.RSSI = beacon.getRssi();
                    mWriteFileData.Major = beacon.getMajor();
                    wtdArray.add(mWriteFileData);
                    if ((mWriteFileData.Major == major_filter)) {
                        if (mWriteFileData.Minor < INPUT_NUM) {
                            CObj[mWriteFileData.Minor].inputArray(mWriteFileData.RSSI);
                        }
                    }
                    mWriteFileData = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            final EasyObjectCursor<BluetoothLeDevice> c = mDeviceStore.getDeviceCursor();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.swapCursor(c);
                    updateItemCount(mLeDeviceListAdapter.getCount());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

        ButterKnife.bind(this);

        manger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification();
        notification.defaults = Notification.DEFAULT_SOUND;


        mList.setEmptyView(mEmpty);
        mDeviceStore = new BluetoothLeDeviceStore();
        mBluetoothUtils = new BluetoothUtils(this);
        mScanner = new BluetoothLeScanner(mLeScanCallback, mBluetoothUtils);

        TIME_COUNT = Integer.valueOf(mSetTime.getText().toString());
        lognum = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNEL_STORAGE);
            }

        }

        for(int i=0;i<INPUT_NUM;i++){
            CObj[i] = new CountObj();
        }

        //===========================================================================================
//        sensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
//        g_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        m_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                try {
                    myOutWriter.close();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                Toast.makeText(getParent(),"write", Toast.LENGTH_SHORT).show();
                if (handlerThread != null) {
                    handlerThread.quit();
                }
                if (handler != null) {
                    handler.removeCallbacks(r1);
                }
                finish();
                startActivity(intent);
//                finish();
//                System.exit(0);
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                try {
                    myOutWriter.close();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                Toast.makeText(getParent(),"write", Toast.LENGTH_SHORT).show();
                if (handlerThread != null) {
                    handlerThread.quit();
                }
                if (handler != null) {
                    handler.removeCallbacks(r1);
                }
                finish();
                startActivity(intent);
//                finish();
//                System.exit(0);
            }
        });

        receive.setOnClickListener(new ToggleButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (receive.isChecked()) {
                    wtdArray = null;
                    wtdArray = new ArrayList<>();
                    calculation = new Calculation();

                    if (!running) {
                        running = true;

                        handlerThread = new HandlerThread("positionworker");
                        handlerThread.start();
                        handler = new Handler(handlerThread.getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                switch (msg.what) {
                                    case 1:
                                        handler.removeCallbacks(r1);
                                        break;
                                    case 2:
                                        handler.removeCallbacks(r2);
                                        break;
                                }
                            }
                        };

                        TIME_COUNT = Integer.parseInt(mSetTime.getText().toString());

                        if (loopChk.isChecked()) {//持續定位
                            handler.post(r1);
                        } else {//一次定位
                            handler.postDelayed(r2, 100);
                        }
                    }
                } else {
                    if (loopChk.isChecked()) {
                        handler.sendEmptyMessage(1);
                    }
                    handlerThread.quit();
                    receive.setChecked(false);

                    running = false;
                    temp = 0;
                    receive.setTextOff("START RECORD");
                    receive.setText("START RECORD");
                }
            }
        });

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btn_scan.getText() == "SCAN"){
                    btn_scan.setText("STOP");
                    btn_scan.setTextColor(Color_accent);
                    startScan();
                }
                else {
                    btn_scan.setText("SCAN");
                    btn_scan.setTextColor(Color_primary);
                    mScanner.scanLeDevice(-1, false);
                }
            }
        });

        SharedPreferences settings = getSharedPreferences("Preference", 0);
        String major_str = settings.getString("Major","");
        if (major_str.equals("")){
            edt_major.setText("3");
            major_filter = 3;
        }else{
            edt_major.setText(major_str);
            major_filter = Integer.parseInt(major_str);
        }

        edt_major.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!"".equals(edt_major.getText().toString())) {
                    SharedPreferences settings = getSharedPreferences("Preference", 0);
                    settings.edit().putString("Major", edt_major.getText().toString()).commit();
                    major_filter = Integer.parseInt(edt_major.getText().toString());
                }
            }
        });


        String Location_str = settings.getString("Location","");
        if (Location_str.equals("")){
            edtX.setText("0");
        }else{
            edtX.setText(Location_str);
        }

        edtX.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!"".equals(edtX.getText().toString()))
                {
//                    label = (Integer.parseInt(edtX.getText().toString()));
                    label = (Integer.parseInt(edtX.getText().toString())*4 + Integer.parseInt(edtY.getText().toString()));
                    edtLbl.setText(""+label);
                    SharedPreferences settings = getSharedPreferences("Preference", 0);
                    settings.edit().putString("Location", edtX.getText().toString()).commit();
                    settings.edit().putString("Label", edtLbl.getText().toString()).commit();
                }
            }
        });

        String Direction_str = settings.getString("Direction","");
        if (Direction_str.equals("")){
            edtY.setText("0");
        }else{
            edtY.setText(Direction_str);
        }

        edtY.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!"".equals(edtY.getText().toString()))
                {
                    label = (Integer.parseInt(edtX.getText().toString())*4 + Integer.parseInt(edtY.getText().toString()));
                    edtLbl.setText(""+label);
                    SharedPreferences settings = getSharedPreferences("Preference", 0);
                    settings.edit().putString("Direction", edtY.getText().toString()).commit();
                    settings.edit().putString("Label", edtLbl.getText().toString()).commit();
                }
            }
        });

        String Label_str = settings.getString("Label","");
        if (Label_str.equals("")){
            edtLbl.setText("0");
        }else{
            edtLbl.setText(Label_str);
        }

        updateItemCount(0);
        startScan();

        getFileList();
        //先開啟掃描beacon，callback function會將掃描到的beacon存在array裡
        createFile(); //創檔案，開始寫入相關beacon格式

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);





    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanner.scanLeDevice(-1, false);

//        if(sensorManager!=null){
//
////            sensorManager.unregisterListener(g_sensor_listener);
////            sensorManager.unregisterListener(m_sensor_listener);
//
//        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        sensorManager.registerListener(g_sensor_listener,g_sensor,sensorManager.SENSOR_DELAY_NORMAL);
//        sensorManager.registerListener(m_sensor_listener,m_sensor,sensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "write", Toast.LENGTH_SHORT).show();
        if (handlerThread != null) {
            handlerThread.quit();
        }
        if (handler != null) {
            handler.removeCallbacks(r1);
        }
    }

    public void getFileList(){
        File directory = new File(FILE_PATH);
        File[] files = directory.listFiles();
        //依創建時間排序
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
            }

            public boolean equals(Object obj) {
                return true;
            }

        });

        ArrayList<String> m_filelist = new ArrayList<String>();
        for (int i = (files.length-1); i >= 0 ; i--)
        {
            Log.d("Files", "FileName:" + files[i].getName());
            long sizeKB = files[i].length() / 1024;
            if(sizeKB == 0){
                files[i].delete();
                Toast.makeText(this, "delete "+files[i].getName(), Toast.LENGTH_SHORT).show();
            }
            else {
                m_filelist.add(files[i].getName() + "      " + sizeKB + "KB");
            }

        }
        ListAdapter adapter = new ArrayAdapter<>(this , android.R.layout.simple_list_item_1 ,m_filelist);
        mList_file.setAdapter(adapter);
    }



    private Runnable r1 = new Runnable() {
        @Override
        public void run() {//連續
            if (temp == 0) {
                calculation = new Calculation();
                for(int i=0;i<CObj.length;i++){
                    CObj[i].initial();
                }
            }
            if (temp < TIME_COUNT) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        receive.setText("" + temp + "ms  (log amount:" + lognum +")");
                    }
                });
                temp = temp + 10;

            } else {
                temp = 0;
                lognum++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        receive.setText("" + temp + "ms  (log amount:" + lognum +")");
                    }
                });
//                separateBeacon();//初始化
                if (Ifwrite.isChecked()) {//是否寫檔
                    writeFile();
                }
//                cleanCountObj();
                wtdArray = null;
                wtdArray = new ArrayList<>();
            }

            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(lognum%AMOUNT_COUNT == 0 && lognum!=0 && temp==0){
                handlerThread.quit();
                running = false;
                toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 200);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        receive.setChecked(false);
                        receive.setText("KEEP RECORD   (log amount:" + lognum +")");
                    }
                });
            }
            else{
                handler.postDelayed(this, 10);
            }

        }
    };

    private Runnable r2 = new Runnable() {
        @Override
        public void run() {//一次
            if (TIME_COUNT > 1) {
                TIME_COUNT = TIME_COUNT - 100;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        receive.setText(TIME_COUNT + "");
                    }
                });
            } else {
//                separateBeacon();//初始化

                if (Ifwrite.isChecked()) {//是否寫檔
                    writeFile();
                }

                running = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        receive.setChecked(false);
                    }
                });
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                cleanCountObj();
                handler.sendEmptyMessage(2);
            }
            handler.postDelayed(this, 100);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startScan() {
        final boolean mIsBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean mIsBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();
        mDeviceStore.clear();
        updateItemCount(0);

        mLeDeviceListAdapter = new LeDeviceListAdapter(this, mDeviceStore.getDeviceCursor());
        mList.setAdapter(mLeDeviceListAdapter);

        mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
        if (mIsBluetoothOn && mIsBluetoothLePresent) {
            mScanner.scanLeDevice(-1, true);
//            invalidateOptionsMenu();
        }
        else{
            Toast.makeText(this,"Try Again", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateItemCount(final int count) {//更新搜尋到的裝置數
        mTvItemCount.setText(getString(R.string.formatter_item_count, String.valueOf(count)));
    }

    private void createFile() {
        Log.wtf("FILE_PATH",""+FILE_PATH);
        File dir = new File(FILE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
            Log.wtf("DIR","CREATE");
        }
        FILENAME = sdf.format(System.currentTimeMillis());
        Log.wtf("FILENAME",FILENAME);
        file = new File(FILE_PATH + FILENAME + ".csv");

        if (!file.exists()) {
            Log.wtf("FILE","NOT EXIST");
            try {
                file.createNewFile();
                Toast.makeText(this, file.toString(), Toast.LENGTH_SHORT).show();
                tv_save_dir_to.setText(file.toString());
            } catch (IOException e) {
                Log.wtf("FILE","NOT CREATE:"+e.toString());
                e.printStackTrace();
            }
        }

        try {
            fOut = new FileOutputStream(file);
            myOutWriter = new OutputStreamWriter(fOut);

            myOutWriter.append("Direction,");//minor編號
            for (int i = 0; i < INPUT_NUM; i++) {
                myOutWriter.append(i + ",");
            }
            myOutWriter.append("label,");
            myOutWriter.append("\n");
            Log.wtf("Try","Create File");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFile() {
        float[] calbeacon = new float[INPUT_NUM];
        for(int i=0;i<INPUT_NUM;i++) {
            calbeacon[i] = CObj[i].getAvg();//(CObj[i].getAvg()-50)/30;
        }

        if (wtdArray != null) {
            try {

                    myOutWriter.append("Location: " + edtX.getText().toString() + " - Direction: " + edtY.getText().toString() + ",");
//                    myOutWriter.append(azimuth_normalize + ",");
                    for (int i = 0; i < calbeacon.length; i++) {
                        myOutWriter.append(calbeacon[i] + ",");
                    }
                    myOutWriter.append(label + ",");
                    myOutWriter.append("\n");


            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getApplicationContext(), "NO USBeacon Signal", Toast.LENGTH_SHORT).show();
        }
    }

//    private SensorEventListener g_sensor_listener = new SensorEventListener() {
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            synchronized (this) {
//                final float alpha = 0.50f;
//                mGravity[0] = alpha * mGravity[0] + (1 - alpha)
//                        * event.values[0];
//                mGravity[1] = alpha * mGravity[1] + (1 - alpha)
//                        * event.values[1];
//                mGravity[2] = alpha * mGravity[2] + (1 - alpha)
//                        * event.values[2];
//            }
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//        }
//    };
//    private SensorEventListener m_sensor_listener = new SensorEventListener() {
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            synchronized (this) {
//                final float alpha = 0.50f;
//                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
//                        * event.values[0];
//                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
//                        * event.values[1];
//                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
//                        * event.values[2];
//
//                boolean success = SensorManager.getRotationMatrix(R_matrix, I_matrix, mGravity,
//                        mGeomagnetic);
//                if (success) {
//                    float orientation[] = new float[3];
//                    SensorManager.getOrientation(R_matrix, orientation);
//                    // Log.d(TAG, "azimuth (rad): " + azimuth);
//                    azimuth = (float) Math.toDegrees(orientation[0]); // orientation
//                    azimuth = (azimuth + azimuthFix + 360) % 360 ;
//                    azimuth_normalize = (azimuth/180) - 1f;
//                    edtY.setText(""+azimuth_normalize);
//                    Log.d("degree", "azimuth (deg): " + azimuth_normalize);
//                }
//            }
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//        }
//    };
}
