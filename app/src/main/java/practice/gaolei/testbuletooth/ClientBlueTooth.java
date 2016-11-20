package practice.gaolei.testbuletooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

public class ClientBlueTooth extends AppCompatActivity {

    private TextView textView;
    private EditText editText;
    private Button button;
    private BufferedReader in;
    private PrintStream printStream;
    private BluetoothSocket socket;
    private BluetoothDevice remoteDevice;
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
        WeakReference<ClientBlueTooth> weak;

        public MyHandler(ClientBlueTooth activity) {
            weak = new WeakReference<ClientBlueTooth>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 201:
                    //成功
                    setInfo("连接成功\n");
                    button.setEnabled(true);
                    Log.d("gl","name==="+remoteDevice.getName());
                    Log.d("gl","getUUID==="+remoteDevice.getUuids());
                    Log.d("gl","getAddress==="+remoteDevice.getAddress());
                    new Thread(new ReceiveInfoThread()).start();
                    break;
                case 202:
                    //失败
                    setInfo("连接失败\n");
                    setInfo(msg.obj.toString()+"\n");
                    break;
                case 203:
                    //接收客户端的数据
                    setInfo(msg.obj.toString()+"\n");
                    Log.d("gl",msg.obj+"=======");
                    break;
                case 204:
                    textView.setText(editText.getText().toString());
                    editText.setText("");
                    break;


            }
        }
    }

    private void init() {
        textView.setText("正在与服务器连接。。。\n");
        new Thread(new Runnable() {


            @Override
            public void run() {
                defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                remoteDevice = defaultAdapter.getRemoteDevice("24:1F:A0:67:94:E4");
                try {
                    socket = remoteDevice.createInsecureRfcommSocketToServiceRecord(
                            UUID.fromString("00000000-1234-1234-1234-1111222233334444")
                    );
                    if (socket != null) {
                        //连接
                        socket.connect();
                        printStream = new PrintStream(socket.getOutputStream());
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    }
                    handler.sendEmptyMessage(201);//成功
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = handler.obtainMessage(202, e.getLocalizedMessage());//失败时候的what
                    handler.sendMessage(msg);
                }
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
                    Message msg = handler.obtainMessage(203, info);//接收服务端的数据
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void setInfo(String info) {
        StringBuffer sb = new StringBuffer();
        sb.append(textView.getText());
        sb.append(info);
        textView.setText(sb);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
            in.close();
            printStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                printStream.println(content);
                printStream.flush();
               // handler.sendEmptyMessage(204);//把前面的清空
                Message m=Message.obtain();
                m.what=204;
                handler.sendMessage(m);
            }
        }).start();


    }
}
