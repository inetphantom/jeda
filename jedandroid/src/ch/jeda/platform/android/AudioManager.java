/*
 * Copyright (C) 2013 by Stefan Rothe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.jeda.platform.android;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import ch.jeda.Log;
import ch.jeda.platform.MusicImp;
import ch.jeda.platform.SoundImp;

class AudioManager extends Fragment {

    private static final String RES_PREFIX = "res:";
    private static final int DEFAULT_PRIORITY = 0;
    private android.media.AudioManager imp;
    private SoundPool soundPool;

    public AudioManager() {
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        this.imp = (android.media.AudioManager) activity.getSystemService(Activity.AUDIO_SERVICE);
        this.soundPool = new SoundPool(10, android.media.AudioManager.STREAM_MUSIC, 0);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        return null;
    }

    SoundImp createSoundImp(final String path) {
        if (path.startsWith(RES_PREFIX)) {
            final int resId = this.getResourceId(path);
            if (resId == 0) {
                return null;
            }
            else {
                return new AndroidSoundImp(this, this.soundPool.load(this.getActivity(), resId, DEFAULT_PRIORITY));
            }
        }
        else {
            return new AndroidSoundImp(this, this.soundPool.load(path, DEFAULT_PRIORITY));
        }
    }

    MusicImp createMusicImp(final String path) {
        if (path.startsWith(RES_PREFIX)) {
            final int resId = this.getResourceId(path);
            if (resId == 0) {
                return null;
            }
            else {
                return new AndroidMusicImp(path, MediaPlayer.create(this.getActivity(), resId));
            }
        }
        else {
            return null;
        }
    }

    void play(final int soundId) {
        final float volume = this.getVolume();
        this.soundPool.play(soundId, volume, volume, 0, 0, 1.0f);
    }

    private int getResourceId(final String path) {
        final int slashPos = path.indexOf('/');
        final int dotPos = path.lastIndexOf('.');
        if (slashPos == -1 || dotPos == -1) {
            Log.err("jeda.audio.error.invalid-resource-name", path);
            return 0;
        }

        final String type = path.substring(RES_PREFIX.length(), slashPos);
        final String name = path.substring(slashPos + 1, dotPos);
        return this.getResources().getIdentifier(name, type, getActivity().getApplicationContext().getPackageName());
    }

    private float getVolume() {
        return (float) this.imp.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) /
               (float) this.imp.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
    }
}