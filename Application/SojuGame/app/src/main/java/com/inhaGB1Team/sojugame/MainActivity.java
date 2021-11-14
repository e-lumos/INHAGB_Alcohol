package com.inhaGB1Team.sojugame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    ToggleButton tgBtn_Bluetooth;
    ToggleButton tgBtn_Connect;

    Button btn_SendNum;
    Button btn_Main;

    EditText et_PeopleNum;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;

    int fsrValue = 0;   // 압력 센서 데이터

    //
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 블루투스, 연결 버튼 정의
        tgBtn_Bluetooth = (ToggleButton) findViewById(R.id.tgbtn_Bluetooth);
        tgBtn_Connect = (ToggleButton) findViewById(R.id.tgbtn_Connect);

        et_PeopleNum = (EditText) findViewById(R.id.et_Number);

        btn_SendNum = (Button) findViewById(R.id.btn_SendNum);
        btn_Main = (Button) findViewById(R.id.btn_Main);

        // 장치 블루투스 지원 여부 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 블루투스가 켜져 있으면 Toggle Button 상태 바꾸기
        if (mBluetoothAdapter.isEnabled()) {
            tgBtn_Bluetooth.setChecked(true);
        }
        // 연결 버튼은 꺼진 상태로 시작
        tgBtn_Connect.setChecked(false);

        // 버튼 클릭 이벤트
        tgBtn_Bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bluetoothOn();
                } else {
                    bluetoothOff();
                }
            }
        });

        tgBtn_Connect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    listPairedDevices();
                } else {
                    // Button Image Change
                }
            }
        });

        et_PeopleNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    if (Integer.parseInt(s.toString()) > 24 | Integer.parseInt(s.toString()) < 1) {
                        et_PeopleNum.setText(null);
                        Toast.makeText(getApplicationContext(), "1부터 24까지만 입력해주세요", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        btn_SendNum.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (!tgBtn_Bluetooth.isChecked()){
                    Toast.makeText(getApplicationContext(), "블루투스가 비활성화되어 있음", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!tgBtn_Connect.isChecked()){
                    Toast.makeText(getApplicationContext(), "연결이 비활성화되어 있음", Toast.LENGTH_LONG).show();
                    return;
                }
                String msg = "AND_PNUM_" + et_PeopleNum.getText().toString();
                mThreadConnectedBluetooth.write(msg);
            }
        });

        btn_Main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tgBtn_Bluetooth.isChecked()){
                    Toast.makeText(getApplicationContext(), "블루투스가 비활성화되어 있음", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!tgBtn_Connect.isChecked()) {
                    Toast.makeText(getApplicationContext(), "연결이 비활성화되어 있음", Toast.LENGTH_LONG).show();
                    return;
                }
                String msg = "AND_MAIN_START";
                mThreadConnectedBluetooth.write(msg);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mThreadConnectedBluetooth.cancel();
    }

    void bluetoothOn() {
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        }
        else {
            Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);

            if (mBluetoothAdapter.isEnabled()) {
                // Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
            }
            else {
            }
        }
    }

    // 블루투스 비활성화 메소드, disable()메소드 -> 비활성화
    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스 비활성화", Toast.LENGTH_SHORT).show();
        }
        else {
            // Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 블루투스 활성화 결과를 위한 메소드
    // 블루투스 ON 메소드에서 Intent로 받은 결과를 처리하는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화 취소", Toast.LENGTH_LONG).show();
                    tgBtn_Bluetooth.setChecked(false);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 블루투스 페어링 장치목록 가져오는 메소드
    void listPairedDevices() {
        // 먼저 블루투스가 활성화 상태인지 확인
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            // 페어링된 장치가 존재할때
            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");
                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }

                // 페어링된 장치수를 얻어와서 각 장치를 누르면 장치명을 매개변수로 사용하여
                // connectSelectedDevice 메소드로 전달해주는 클릭 이벤트 추가
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });

                // 위에서 리스트로 추가된 알람창을 실제로 띄워줌.
                AlertDialog alert = builder.create();
                alert.show();

                // 173줄, 176줄에 대응하여 페어링된 장치가 없는 조건과 블루투스가 비활성화된
                // 조건에 대해 메세지를 띄어주는 코드
            } else {
                tgBtn_Connect.setChecked(false);
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            tgBtn_Connect.setChecked(false);
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 블루투스 연결하는 메소드(실제 블루투스 장치와 연결)
    void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mThreadConnectedBluetooth.run();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    // 쓰레드 시작 - 쓰레드에서 사용할 전역 객체들을 선언
    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        // 쓰레드 초기화 과정
        // 데이터 전송 및 수신하는 길을 만들어주는 작업
        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        // 수신받은 데이터는 언제들어올 지 모르니 항상 확인
        public void run() {
            byte[] buffer = new byte[1024];
            // String buffer = "";
            int bytes;

            // 처리된 데이터가 존재하면 데이터를 읽어오는 작업
            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                        // Buffer 처리 함수 추가
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        // 데이터 전송을 위한 메소드
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        // 블루투스 소켓을 닫는 메소드
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                // Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void readBuffer(){

    }
}