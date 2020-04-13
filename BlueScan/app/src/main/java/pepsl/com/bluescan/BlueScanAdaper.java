package pepsl.com.bluescan;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BlueScanAdaper extends BaseAdapter {

    private final static String TAG="BlueScanAdaper";
    private final Context mContext;
    private ViewHolder viewHolder;
    private List<BlueInfo> datas = new ArrayList<>();

    public BlueScanAdaper(Context context){
        mContext = context;
    }

    public void add(BlueInfo info){
        datas.add(info);
    }

    public void removAll(){
        datas.clear();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.blueinfoitem, null);
            viewHolder = new BlueScanAdaper.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (BlueScanAdaper.ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(datas.get(position).getName());
        viewHolder.address.setText(datas.get(position).getAddress());
        viewHolder.rssi.setText(String.valueOf(datas.get(position).getRssi()));
        return convertView;
    }

    public static class ViewHolder {
        public View rootView;
        public TextView name;
        public TextView address;
        public TextView rssi;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.name = (TextView) rootView.findViewById(R.id.name);
            this.address = (TextView) rootView.findViewById(R.id.address);
            this.rssi = (TextView) rootView.findViewById(R.id.rssi);
        }
    }
}
