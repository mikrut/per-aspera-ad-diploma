package ru.mail.park.chat.activities.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.activities.views.TitledPicturedViewHolder;
import ru.mail.park.chat.models.AttachedFile;

/**
 * Created by Михаил on 24.04.2016.
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private final List<AttachedFile> files;

    public FilesAdapter(List<AttachedFile> files) {
        this.files = files;
    }

    @Override
    public FilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View fileView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_file, parent, false);
        return new ViewHolder(fileView);
    }

    @Override
    public void onBindViewHolder(FilesAdapter.ViewHolder holder, int position) {
        holder.initView(files.get(position));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView cancel;
        public final TextView fileName;
        public final TextView fileSize;

        private String chatID;

        public ViewHolder(final View fileView) {
            super(fileView);

            cancel = (ImageView) fileView.findViewById(R.id.cancel_image_view);
            fileName = (TextView) fileView.findViewById(R.id.file_name_text_view);
            fileSize = (TextView) fileView.findViewById(R.id.file_size_text_view);

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    files.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }

        public void initView(AttachedFile file) {
            fileName.setText(file.getFileName());
            fileSize.setText(humanReadableByteCount(file.getFileSize()));
        }

        private String humanReadableByteCount(long bytes) {
            int unit = 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = "KMGTPE".charAt(exp-1) + "i";
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }
    }
}
