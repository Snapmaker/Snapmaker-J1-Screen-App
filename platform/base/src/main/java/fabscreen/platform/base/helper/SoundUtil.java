package fabscreen.platform.base.helper;

import android.media.SoundPool;

import com.orhanobut.logger.Logger;

public class SoundUtil {
    public static void playSound(SoundPool soundPool, int soundId) {
        soundPool.play(soundId, 1, 1, 1, 0, 1);
    }

    public static int playSoundHasId(SoundPool soundPool, int soundId) {
        return soundPool.play(soundId, 1, 1, 1, 0, 1);
    }

    public static int playSoundLoop(SoundPool soundPool, int soundId) {
        return soundPool.play(soundId, 1, 1, 1, -1, 1);
    }

    public static void stopSound(SoundPool soundPool, int streamId) {
        soundPool.stop(streamId);
    }
}
