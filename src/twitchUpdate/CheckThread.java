package twitchUpdate;

import java.util.ArrayList;

import twitchlsgui.GenericStream;
import twitchlsgui.TwitchStream;

class CheckThread implements Runnable {
    private int index = -1;
    private ArrayList<GenericStream> streamList;

    public CheckThread(int i, ArrayList<GenericStream> streamList) {
	index = i;
	this.streamList = streamList;
    }

    @Override
    public void run() {
	((TwitchStream) streamList.get(index)).refresh();
    }
}