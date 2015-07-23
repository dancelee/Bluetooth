package giso.com.qf.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private BluetoothAdapter defaultAdapter;
    private ListView list;

    private DeviceAdapter adapter;
    public static final String uuid= "2700abef-3062-44b9-b1dc-28910cf4940f";

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    BluetoothDevice device=msg.getData().getParcelable(BluetoothDevice.EXTRA_DEVICE);
                    adapter.add(device);
                    break;
                case 1:
                    Toast.makeText(MainActivity.this,((String)msg.obj),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

   /* private Handler handler=new Handler() {
       // super.handleMessage(msg);
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    BluetoothDevice device=msg.getData().getParcelable(BluetoothDevice.EXTRA_DEVICE);
                    adapter.add(device);
                    break;
                case 1:
                    Toast.makeText(MainActivity.this,"msg",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };*/
    private MyReceiver receiver;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = ((ListView) findViewById(R.id.list));
        //获取默认的蓝牙适配器

        adapter = new DeviceAdapter(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if(defaultAdapter==null)
        {
            Toast.makeText(this,"本设备不支持lanya",Toast.LENGTH_SHORT).show();
            finish();
        }else {
            if (!defaultAdapter.isEnabled()) {
                Toast.makeText(this, "蓝牙适配器没有开启", Toast.LENGTH_SHORT).show();
               // finish();//没有蓝牙直接退出
                //静默开启,不友好
                //defaultAdapter.enable();
                //询问启动
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),0);

            }else{//蓝牙设备的扫描
                startScan();

            }
        }
    }
//yi:请求码，er:返回码，先判断 返回码,RESUlt_ok不能用
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK)
        {
            Toast.makeText(this,"开启成功",Toast.LENGTH_LONG).show();
            startScan();
        }else{
            Toast.makeText(this,"开启失败",Toast.LENGTH_LONG).show();
            finish();

        }
        //super.onActivityResult(requestCode, resultCode, data);
    }
    public void startScan(){
        //开始扫描周围的设备，开启可见状态
        defaultAdapter.startDiscovery();
        //获得已绑定的蓝牙状态，
        Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
        adapter.addAll(bondedDevices);
        receiver=new MyReceiver(handler);//这句话的作用通知接收到的消息
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);
        UUID uuid1=UUID.fromString(uuid);
        try {
            serverSocket=defaultAdapter.listenUsingInsecureRfcommWithServiceRecord(null,uuid1);
            new Thread(){
                @Override
                public void run()
                {
                    super.run();
                    BluetoothSocket socket;
                    try {
                        while((socket=serverSocket.accept())!=null)
                        {//获取连接的蓝牙设备
                            BluetoothDevice device=socket.getRemoteDevice();
                            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                            String s=dataInputStream.readUTF();
                            handler.obtainMessage(1,device.getName()+":"+s).sendToTarget();
                            socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if(serverSocket!=null)
        {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device=(BluetoothDevice) adapter.getItem(position);
        new Thread(){
            public void run()
            {
                BluetoothSocket socket=null;
                try {//发起连接
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                    socket.connect();
                    DataOutputStream stream=new DataOutputStream(socket.getOutputStream());
                    stream.writeUTF("测试");
                    //这里不要关闭
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
