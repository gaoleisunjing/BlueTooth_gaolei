package practice.gaolei.testbuletooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class ServerBlueTooth extends AppCompatActivity {

    private TextView textView;
    private EditText editText;
    private Button button;
    private BufferedReader in;
    private PrintStream out;
    private BluetoothSocket socket;
    private BluetoothServerSocket serversocket;
    private BluetoothAdapter defaultAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_blue_tooth);

        editText = (EditText) findViewById(R.id.editId);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button6);
        init();
    }


    MyHandler handler = new MyHandler(this);

    private class MyHandler extends Handler {
        WeakReference<ServerBlueTooth> weak;

        public MyHandler(ServerBlueTooth activity) {
            weak = new WeakReference<ServerBlueTooth>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101:
                    //成功
                    setInfo("连接成功\n");
                    button.setEnabled(true);
                    new Thread(new ReceiveInfoThread()).start();
                    break;
                case 102:
                    //失败
                    setInfo("连接失败\n");
                    setInfo(msg.obj.toString()+"\n");
                    break;
                case 103:
                    //接收客户端的数据
                    setInfo(msg.obj.toString()+"\n");
                    Log.d("gl","handler===="+msg.obj.toString());
                    break;
                case 104:
                    textView.setText(editText.getText().toString());
                    editText.setText("");
                    break;

            }

        }
    }

    private void init() {
        button.setEnabled(false);
        textView.setText("服务器已经启动,正在等待连接。。。\n");
        new Thread(new Runnable() {

            @Override
            public void run() {
                //得到本地设备
                defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                try {
                    //UUID   蓝牙串口 8-4-4-4-12
                    serversocket = defaultAdapter.listenUsingRfcommWithServiceRecord(
                            "gg", UUID.fromString("00000000-1234-1234-1234-1111222233334444")
                    );
                    socket = serversocket.accept();
                    if (socket != null) {
                        out = new PrintStream(socket.getOutputStream());
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    }
                    handler.sendEmptyMessage(101);//成功使得what
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage(102, e.getLocalizedMessage());//失败时候的what
                    handler.sendMessage(msg);
                }

            }
        }).start();


    }

    public void connect(View view) {
        final String content = editText.getText().toString();
        Log.d("gl","====="+content);
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "输入不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                out.println(content);
                out.flush();
                Message m=Message.obtain();
                m.what=104;
                handler.sendMessage(m);//把前面的清空
            }
        }).start();


    }

    private boolean isReceive = true;

    class ReceiveInfoThread implements Runnable {
        @Override
        public void run() {
            String info = null;
            while (isReceive) {
                try {
                    info = in.readLine();
                    Message msg = handler.obtainMessage(103, info);//接收客户端的数据
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void setInfo(String info){
        StringBuffer sb=new StringBuffer();
        sb.append(textView.getText());
        sb.append(info);
        textView.setText(sb);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            serversocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
