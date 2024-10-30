package com.coloryr.allmusic.client.hud;

public class MusicMeta
{
    public DrawingState list;
    public DrawingState lyric;
    public DrawingState info;
    public DrawingState pic;
    public int picRotateSpeed;

    public MusicMeta()
    {
        picRotateSpeed = 10;
        list = new DrawingState();
        lyric = new DrawingState();
        info = new DrawingState();
        pic = new DrawingState();
    }
}
