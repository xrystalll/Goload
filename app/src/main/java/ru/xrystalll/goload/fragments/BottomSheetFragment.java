package ru.xrystalll.goload.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.xrystalll.goload.R;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private String fileSize, timeRemove, fullLink;

    public BottomSheetFragment(String fileSize, String timeRemove, String fullLink) {
        this.fileSize = fileSize;
        this.timeRemove = timeRemove;
        this.fullLink = fullLink;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_info, container, false);

        TextView file_size = view.findViewById(R.id.file_size);
        TextView time_del = view.findViewById(R.id.time_del);
        TextView full_link = view.findViewById(R.id.full_link);

        file_size.setText(fileSize);
        time_del.setText(timeRemove);
        full_link.setText(fullLink);

        return view;
    }
}
