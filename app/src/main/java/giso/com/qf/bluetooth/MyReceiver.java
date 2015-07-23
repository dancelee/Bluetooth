package giso.com.qf.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;


/**
 * Created by Administrator on 2015/7/18.
 */
public class MyReceiver extends BroadcastReceiver {
    private Handler handler;
    public MyReceiver (Handler handler){
        this.handler=handler;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND))
        {
            Message message = handler.obtainMessage(0);
            message.setData(intent.getExtras());
            message.sendToTarget();
        }




    }
}
