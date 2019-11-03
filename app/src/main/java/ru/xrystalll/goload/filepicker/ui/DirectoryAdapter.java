package ru.xrystalll.goload.filepicker.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.xrystalll.goload.R;
import ru.xrystalll.goload.filepicker.utils.FileTypeUtils;

import java.io.File;
import java.util.List;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class DirectoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mFileImage;
        private final TextView mFileTitle;
        private final TextView mFileSubtitle;

        DirectoryViewHolder(View itemView, final OnItemClickListener clickListener) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(v, getAdapterPosition());
                }
            });

            mFileImage = itemView.findViewById(R.id.item_file_image);
            mFileTitle = itemView.findViewById(R.id.item_file_title);
            mFileSubtitle = itemView.findViewById(R.id.item_file_subtitle);
        }
    }

    private final List<File> mFiles;
    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;

    DirectoryAdapter(Context context, List<File> files) {
        mContext = context;
        mFiles = files;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public DirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_path, parent, false);

        return new DirectoryViewHolder(view, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final DirectoryViewHolder holder, int position) {
        File currentFile = mFiles.get(position);

        FileTypeUtils.FileType fileType = FileTypeUtils.getFileType(currentFile);

        String format = FileTypeUtils.getExtension(currentFile.getName());

        if (format.equals("png") || format.equals("jpg") ||
                format.equals("jpeg") || format.equals("gif") || format.equals("ico")) {

            Picasso.get()
                    .load(currentFile)
                    .placeholder(fileType.getIcon())
                    .error(fileType.getIcon())
                    .resize(112, 112)
                    .centerCrop()
                    .into(holder.mFileImage);
        } else {
            holder.mFileImage.setImageResource(fileType.getIcon());
        }

        holder.mFileSubtitle.setText(fileType.getDescription());
        holder.mFileTitle.setText(currentFile.getName());
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    File getModel(int index) {
        return mFiles.get(index);
    }
}