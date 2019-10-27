package ru.xrystalll.goload.filepicker.filter;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;

public class CompositeFilter implements FileFilter, Serializable {

    private final ArrayList<FileFilter> mFilters;

    public CompositeFilter(ArrayList<FileFilter> filters) {
        mFilters = filters;
    }

    @Override
    public boolean accept(File f) {
        for (FileFilter filter : mFilters) {
            if (!filter.accept(f)) {
                return false;
            }
        }

        return true;
    }
}
