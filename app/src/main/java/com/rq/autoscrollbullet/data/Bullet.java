package com.rq.autoscrollbullet.data;

public class Bullet {

    /**
     * 头像链接
     */
    private int headPortrait;

    /**
     * 弹幕评论者的昵称
     */
    private String commentNickName;

    /**
     * 弹幕回复者的昵称
     */
    private String replyNickName;

    /**
     * 评论内容
     */
    private String comment;

    /**
     * 回复内容
     */
    private String reply;

    public int getHeadPortrait() {
        return headPortrait;
    }

    public void setHeadPortrait(int headPortrait) {
        this.headPortrait = headPortrait;
    }

    public String getCommentNickName() {
        return commentNickName;
    }

    public void setCommentNickName(String commentNickName) {
        this.commentNickName = commentNickName;
    }

    public String getReplyNickName() {
        return replyNickName;
    }

    public void setReplyNickName(String replyNickName) {
        this.replyNickName = replyNickName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
