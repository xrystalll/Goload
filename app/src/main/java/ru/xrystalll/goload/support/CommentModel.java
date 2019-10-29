package ru.xrystalll.goload.support;

public class CommentModel {

    private final String id;
    private final String fileId;
    private final String author;
    private String userPhoto;
    private final String text;
    private final String likeCount;
    private final String time;

    public CommentModel(
            String id, String fileId, String author, String userPhoto, String text, String likeCount, String time) {
        this.id = id;
        this.fileId = fileId;
        this.author = author;
        this.userPhoto = userPhoto;
        this.text = text;
        this.likeCount = likeCount;
        this.time = time;
    }

    String getId() { return id; }
    String getFileId() { return fileId; }
    String getAuthor() { return author; }
    String getUserPhoto() { return userPhoto; }
    String getText() { return text; }
    String getLikeCount() { return likeCount; }
    String getTime() { return time; }

}
