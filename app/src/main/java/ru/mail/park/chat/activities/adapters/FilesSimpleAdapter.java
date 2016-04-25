package ru.mail.park.chat.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.models.AttachedFile;

/**
 * Created by mikrut on 24.04.16.
 */
public class FilesSimpleAdapter extends ArrayAdapter<AttachedFile> {
    public FilesSimpleAdapter(Context context,  List<AttachedFile> objects) {
        super(context, R.layout.element_file_simple, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.element_file_simple, null);
        }

        AttachedFile file = getItem(position);
        if (file != null) {
            TextView fileName = (TextView) v.findViewById(R.id.file_name_text_view);
            TextView fileSize = (TextView) v.findViewById(R.id.file_size_text_view);

            fileName.setText(file.getFileName());
            fileSize.setText(null);
        }

        return v;
    }
}
