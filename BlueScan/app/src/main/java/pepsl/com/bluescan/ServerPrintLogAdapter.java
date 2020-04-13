package pepsl.com.bluescan;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ServerPrintLogAdapter extends BaseAdapter {

    private final static String TAG="ServerPrintLogAdapter";
    private Context mContext;
    private ViewHolder viewHolder;
    private List<LogInfo> data = new ArrayList<>();

    public ServerPrintLogAdapter(Context context){
        Log.i(TAG,"ServerPrintLogAdapter contruct");
        mContext = context;
    }

    public void add(LogInfo info){
        Log.i(TAG,"add");
        data.add(info);
    }

    public void removeAll(){
        data.clear();
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.layout_print_log, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Log.i(TAG,"data.get(position).getlogRecord() = " + data.get(position).getlogRecord());
        viewHolder.log_record.setText(data.get(position).getlogRecord());
        return convertView;
    }

    public static class ViewHolder {
        public View rootView;
        public TextView log_record;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.log_record = (TextView) rootView.findViewById(R.id.log_record);
        }
    }
}
