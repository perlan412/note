package cellinfo.pepsl.com.cellinfo;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView tv;
    Button bt;
    public TelephonyManager tm;
    private ConnectivityManager ns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        bt = (Button) this.findViewById(R.id.bt);
        bt.setOnClickListener(mListener);
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        ns = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    Log.d("pepsl"," cellinfo = " + tm.getAllCellInfo() + " \n " + " ns = " + ns.getActiveNetworkInfo());
            }
        }
    };
    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            tm.dial("10086");
        }
    };
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener(){
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
        }
    };
}
