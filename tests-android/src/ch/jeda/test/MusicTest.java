package ch.jeda.test;

import ch.jeda.*;
import ch.jeda.event.*;
import ch.jeda.ui.*;

public class MusicTest extends Program implements TickListener, ActionListener {

    View view;
    Music mp3Music;
    Music oggMusic;
    Text mp3State;
    Text oggState;
    double x;
    double y;

    @Override
    public void run() {
        view = new View();
        mp3Music = new Music("res:raw/move_forward.mp3");
        oggMusic = new Music("res:raw/evil_laugh.mp3");
        x = 0.5;
        y = view.getHeightDp() - 1.5;

        addButton("Play MP3");
        addButton("Pause MP3");
        addButton("Stop MP3");
        mp3State = new Text(10, 10, null);
        oggState = new Text(200, 10, null);
        view.add(mp3State);
        view.add(oggState);
        x = 4.5;
        y = view.getHeightDp() - 1.5;
        addButton("Play OGG");
        addButton("Pause OGG");
        addButton("Stop OGG");
        view.addEventListener(this);
    }

    private void addButton(String text) {
        view.add(new TextButton(x, y, text));
        y = y - 1.5;
    }

    public void onAction(ActionEvent event) {
        if ("Play MP3".equals(event.getName())) {
            mp3Music.play();
        }
        else if ("Pause MP3".equals(event.getName())) {
            mp3Music.pause();
        }
        else if ("Stop MP3".equals(event.getName())) {
            mp3Music.stop();
        }
        else if ("Play OGG".equals(event.getName())) {
            oggMusic.play();
        }
        else if ("Pause OGG".equals(event.getName())) {
            oggMusic.pause();
        }
        else if ("Stop OGG".equals(event.getName())) {
            oggMusic.stop();
        }
    }

    public void onTick(TickEvent te) {
        mp3State.setText(mp3Music.getPlaybackState().toString());
        oggState.setText(oggMusic.getPlaybackState().toString());
    }
}
