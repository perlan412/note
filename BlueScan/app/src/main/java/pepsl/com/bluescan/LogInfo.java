package pepsl.com.bluescan;

import android.os.Parcel;
import android.widget.TextView;

public class LogInfo {
    private final static String TAG="LogInfo";
    private String log_record;
    protected LogInfo(Parcel in) {
    }

    public LogInfo(String log_record) {
        this.log_record = log_record;
    }

    public String getlogRecord(){
        return log_record;
    }

}
