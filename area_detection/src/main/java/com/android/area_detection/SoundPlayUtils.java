package com.android.area_detection;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


public class SoundPlayUtils {
    private static SoundPool mSoundPlayer;
    private static int lastPlay = -1;
    private static int soundId = -1;

    public static void init(Context context) {
//        int id = switch (ConfigsKt.getSoundTone()) {
//            case 1 -> R.raw.voice1;
//            case 2 -> R.raw.voice2;
//            default -> R.raw.voice0;
//        };
        int id =  R.raw.voice0;
        if (id != lastPlay) {
            lastPlay = id;
            soundId = mSoundPlayer.load(context, lastPlay, 1);
        }
    }

    public static void play(Context context) {
        init(context);
        mSoundPlayer.play(soundId, 1, 1, 0, 0, 1);
    }

    public static void release() {
        if (mSoundPlayer != null) mSoundPlayer.unload(soundId);
        lastPlay = -1;
    }

    public static void setupPlayer() {
        mSoundPlayer = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
    }
}
