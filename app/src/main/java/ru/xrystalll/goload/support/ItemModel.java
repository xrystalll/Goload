package ru.xrystalll.goload.support;

public class ItemModel {

    private final String id;
    private final String author;
    private final String time;
    private final String fileName;
    private final String filePreview;
    private final String likeCount;
    private final String commentsCount;
    private final String downloadCount;
    private final String viewsCount;
    private final String format;

    public ItemModel(
            String id, String author, String time, String fileName, String filePreview,
            String likeCount, String commentsCount, String downloadCount, String viewsCount, String format) {
        this.id = id;
        this.author = author;
        this.time = time;
        this.fileName = fileName;
        this.filePreview = filePreview;
        this.likeCount = likeCount;
        this.commentsCount = commentsCount;
        this.downloadCount = downloadCount;
        this.viewsCount = viewsCount;
        this.format = format;
    }

    public String getId() { return id; }
    String getAuthor() { return author; }
    String getTime() { return time; }
    String getFileName() { return fileName; }
    String getFilePreview() { return filePreview; }
    String getLikeCount() { return likeCount; }
    String getCommentsCount() { return commentsCount; }
    String getDownloadCount() { return downloadCount; }
    String getViewsCount() { return viewsCount; }
    String getFormat() { return format; }

}
