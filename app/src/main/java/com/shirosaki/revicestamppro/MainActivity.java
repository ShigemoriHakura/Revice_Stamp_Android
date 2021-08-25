package com.shirosaki.revicestamppro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ReviceIR";
    private ConsumerIrManager IR_Service;
    protected boolean IR_Available = false;
    private Handler HBHandler;
    private boolean IsRunning = true;
    private boolean IsHBing = false;
    protected long LastSent = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IR_Service = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.Text_Version)).setText("0.1");
        findViewById(R.id.Button_IRTest).setOnClickListener(Button_IRTest_Listener);

        findViewById(R.id.Button_Henshin_Wait).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode(151);
            }
        });

        findViewById(R.id.Button_Henshin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode(152);
            }
        });

        findViewById(R.id.Button_Hissatsu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode(153);
            }
        });

        findViewById(R.id.Button_Hissatsu_Finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode(154);
            }
        });

        findViewById(R.id.Button_Remix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode(155);
            }
        });

        findViewById(R.id.Button_Remix_Finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode(156);
            }
        });

        ((Switch) findViewById(R.id.Switch_HeartBeat)).setOnCheckedChangeListener(Switch_HeartBeat_Listener);

        IR_Available = IR_Service.hasIrEmitter();
        if (IR_Available) {
            ((TextView) findViewById(R.id.Text_Status)).setText(R.string.Text_Status_Available);
            ((Switch) findViewById(R.id.Switch_HeartBeat)).setChecked(true);
        }else{
            ((TextView) findViewById(R.id.Text_Status)).setText(R.string.Text_Status_Unavailable);
        }

        HandlerThread thread = new HandlerThread("ReviceHBHandler");
        thread.start();
        HBHandler = new Handler(thread.getLooper());
        HBHandler.post(HBRunnable);
    }

    Runnable HBRunnable = new Runnable() {
        @Override
        public void run() {
            while(IsRunning){
                if(IsHBing){
                    sendCode(141);
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    View.OnClickListener Button_IRTest_Listener = new View.OnClickListener() {
        public void onClick(View v) {
            if(IR_Available){
                sendCode(155);
            }else{
                Toast.makeText(MainActivity.this, "Unavailable!", Toast.LENGTH_LONG).show();
            }
        }
    };

    CompoundButton.OnCheckedChangeListener Switch_HeartBeat_Listener = new  CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            //Toast.makeText(MainActivity.this, "Monitored switch is " + (arg1 ? "on" : "off"), Toast.LENGTH_SHORT).show();
            IsHBing = arg1;
        }
    };

    public void sendCode(int binary){
        if(System.currentTimeMillis() - LastSent < 200){
            try {
                Thread.sleep(System.currentTimeMillis() - LastSent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int[] command = getCodeFromBinary(binary, false);
        if(IR_Available) {
            sendIRCommand(command);
        }
    }

    public void sendIRCommand(int[] code){
        LastSent = System.currentTimeMillis();
        IR_Service.transmit(38400, code);
    }

    public int[] getCodeFromBinary(int binary, boolean alert){
        int[] code = {6500, 500};
        String str = Integer.toBinaryString(binary);
        int i2 = 2;

        if(str.length() < 8){
            for(int i = 0; i < 8 - str.length(); i++) {
                str = "0" + str;
            }
        }

        for(int i = 0; i < str.length() ; i++){
            String ss = String.valueOf(str.charAt(i));
            if(ss.equals("0")){
                code = addX(i2, code, 1500);
                i2 ++;
                code = addX(i2, code, 500);
                i2 ++;
            }else{
                code = addX(i2, code, 500);
                i2 ++;
                code = addX(i2, code, 1500);
                i2 ++;
            }
        }
        code = addX(i2, code, 1500);

        if(alert) {
            Log.e(TAG, "\nCalced :\n"
                + Arrays.toString(code));
        }

        return code;
    }

    public static int[] addX(int n, int arr[], int x)
    {
        int i;
        int newarr[] = new int[n + 1];

        for (i = 0; i < n; i++)
            newarr[i] = arr[i];

        newarr[n] = x;

        return newarr;
    }
}