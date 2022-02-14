package org.techtown.BT_SendNumber;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

// 블루투스 관련
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


public class MainActivity extends AppCompatActivity {

    public static final int DisConColor_FloorBtnColor = 0xFF989FA5; // 블루투스 연결상태 false 일때 버튼색
    public static final int ConColor_FloorBtnColor = 0xFFADC2D3; // 블루투스 연결상태 true 일때 버튼색

    private TextView BtnSendData;

    private BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
        
        //버튼 가져오기 및 Tag 등록
        BtnSendData = findViewById(R.id.BtnSendNum4);
        BtnSendData.setTag("a");
        
        //버튼 클릭 리스너 생성
        //데이터 송신부
        Button.OnClickListener BtnClickListener = new View.OnClickListener(){
            public void onClick(View v){
                String NumData = (String)v.getTag();
                bt.send(NumData,false);
            }
        };
        
        //리스너 등록 및 버튼 색 변경
        BtnSendData.setOnClickListener(BtnClickListener);
        BtnSendData.setBackgroundColor(DisConColor_FloorBtnColor);

        // 블루투스 연결 리스터
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            ImageButton imageButton = findViewById(R.id.imageButton);
            //연결
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();

                //색 변경
                BtnSendData.setBackgroundColor(ConColor_FloorBtnColor);
                imageButton.setImageResource(R.drawable.icon_bluetoothgreen);  
                
                //버튼 클릭 활성화W
                BtnSendData.setClickable(true);

            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
                
                //색 변경
                BtnSendData.setBackgroundColor(DisConColor_FloorBtnColor);
                imageButton.setImageResource(R.drawable.icon_bluetoothgray); 
                
                //버튼 클릭 비활성화
                BtnSendData.setClickable(false);
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "연결 실패", Toast.LENGTH_SHORT).show();
                
                //색 변경
                BtnSendData.setBackgroundColor(DisConColor_FloorBtnColor);
                imageButton.setImageResource(R.drawable.icon_bluetoothgray);  
                
                //버튼 클릭 비활성화
                BtnSendData.setClickable(false);
            }
        });

        // 데이터 수신 이벤트
        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            int iLastIndex = 0, iStartIndex = 0, iPlace_1 = 0, iPlace_2 = 0, iPlace_3 = 0, iPlace_4 = 0;

            ///******************************************************************************/
            ///* Data Receive Function*/
            ///******************************************************************************/
            public void onDataReceived(byte[] data, String message) {

                // 데이터 파싱 부분 프로토콜 : "#,data1,data2,data3,data4,@" -------------------
                iStartIndex = message.indexOf('#');                 // 수신된 데이터의 '#' 가 있는 인덱스를 iStartIndex에 저장

                if (iStartIndex != 0) return;                       // iStartIndex가 0이 아니면 리턴하여 재수신까지 대기
                String[] array = message.substring(iStartIndex + 2).split(",");     // 첫 번째 문자부터 ',' 를 제거하고 각 데이터를 array 변수 배열에 저장

                iLastIndex = array.length;                                                  // iLastIndex의 값을 array 배열의 길이로 저장  = 5 = data1data2data3data4'@'\0
                if (!array[iLastIndex - 1].equals("@")) {// 배열의 마지막 인덱스 값 - 1에 해당하는 데이터가 "@"이 아니면 리턴하여 데이터 재수신
                    return;
                }
                //  Debug용 ...Read 확인
               // Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                message = "";
            }
        });



        // 블루투스 연결 버튼 이벤트
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {//연결시도
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    intent.putExtra("layout_list", R.layout.device_layout_list);        // 레이아웃 정보를 다른 클래스로 넘김
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
