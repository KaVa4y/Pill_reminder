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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
        }

        TextView textName = convertView.findViewById(R.id.textName);
        TextView textTime = convertView.findViewById(R.id.textTime);
        TextView textDosage = convertView.findViewById(R.id.textDosage);

        MedicineItem item = medicineList.get(position);
        textName.setText(item.getName());
        textTime.setText("Время: " + item.getTime().getTime().toString());
        String dosageStr = (item.getDosage() != null && !item.getDosage().isEmpty()) ? item.getDosage() : "Без дозировки";
        textDosage.setText("Дозировка: " + dosageStr);

        return convertView;
    }
}