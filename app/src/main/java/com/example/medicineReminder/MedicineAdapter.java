package com.example.medicineReminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MedicineAdapter extends BaseAdapter {

    private Context context;
    private List<MedicineItem> medicineList;

    public MedicineAdapter(Context context, List<MedicineItem> medicineList) {
        this.context = context;
        this.medicineList = medicineList;
    }

    @Override
    public int getCount() {
        return medicineList.size();
    }

    @Override
    public Object getItem(int position) {
        return medicineList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        MedicineItem item = medicineList.get(position);
        text1.setText(item.getName());
        String dosageStr = (item.getDosage() != null && !item.getDosage().isEmpty()) ? item.getDosage() : "Без дозировки";
        text2.setText("Время: " + item.getTime().getTime().toString() + " | " + dosageStr);

        return convertView;
    }
}