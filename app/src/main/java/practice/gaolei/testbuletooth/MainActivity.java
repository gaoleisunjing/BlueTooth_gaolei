package practice.gaolei.testbuletooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Set;

public class MainActivity extends ActionBarActivity {

    private BluetoothAdapter bluetoothAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void open(View view) {
        //有两种方法
//        //第一种 有提示对话框的方式
//        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        //默认为120s 最长时间3600s
//        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 150);
//        startActivity(intent);


        //第二种 直接打开 静默打开
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.enable();

    }

    public void close(View view) {
            bluetoothAdapter.disable();
    }

    public void search(View view){
        //开始扫描附近的蓝牙设备
        bluetoothAdapter.startDiscovery();
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();//配对以后的设备
        for(BluetoothDevice bd:bondedDevices){
            bd.getName();
            Log.d("gl","name==="+bd.getName()+"address======"+bd.getAddress());
        }
    }

    public void client(View view){
        Intent intent=new Intent(this,ClientBlueTooth.class);
        startActivity(intent);
    }

    public void server(View view){
        Intent intent=new Intent(this,ServerBlueTooth.class);
        startActivity(intent);
    }
}
