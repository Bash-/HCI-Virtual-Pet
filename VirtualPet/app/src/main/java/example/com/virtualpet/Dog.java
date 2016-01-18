package example.com.virtualpet;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import example.com.virtualpet.Util.ResourceManager;


/**
 * Created by reneb_000 on 3-12-2015.
 * Last updated by gijsbeernink on 04-Jan-2015.
 */
public class Dog {

    private DogView view;
    private int x, y;

    public static final long THIRTYMINUTES = 1800000; // thirty minutes in milliseconds
    private long lastRefreshed;

    private final ArrayList<DogMood> moodOrder = new ArrayList(Arrays.asList(new DogMood[]{DogMood.DEAD, DogMood.HUNGRY, DogMood.PLAYFULL, DogMood.SAD, DogMood.DIRTY, DogMood.BARKING, DogMood.HAPPY}));
    private DogMood currentMood = DogMood.HAPPY;

    public Dog(Context c, DogView view) {
        this.view = view;
        x = ResourceManager.INSTANCE.getScreenWidth()/2;
        y = (int) (ResourceManager.INSTANCE.getScreenHeight()-ResourceManager.INSTANCE.getPercentageLength(20, true));
        view.setXY(x, y);
        setLastRefreshed(getTime());
    }

    public void update() {
        view.setXY(x, y);
        view.setBackgroundColor();
        checkUpdates();
        randomBark();
    }



    public void checkUpdates() {
        DogService service = DogService.INSTANCE;
        for(DogMood m:moodOrder) {
            if(service.getMood(m)){
                if(currentMood!=m){
                    setView(m);
                    currentMood = m;
                }
                return;
            }
        }
    }

    public void randomBark() {
        if (Math.random() >= 0.9955 && !DogService.INSTANCE.getBarking()) {
            DogService.INSTANCE.setBarking(true);
        }
    }

//    Start activity:

    




//      Actions from other activities:

    public void playedWithDog(boolean gettingDirty) {
        if((getTime() - getTimeLastEaten()) > THIRTYMINUTES) {
            DogService.INSTANCE.updateSatisfaction(15);
        } else {
            DogService.INSTANCE.updateSatisfaction(-5, 60);
        }
        if(Math.random() < 0.75 && gettingDirty) {
            setDirty(true);
//            setView(DogMood.DIRTY);
        }
//        setView(DogMood.HAPPY);
        DogService.INSTANCE.setWantsToPlay(false);
        setTimeLastPlayed(new Date(getTime()));
    }

    public void cleanedDog() {
        DogService.INSTANCE.updateSatisfaction(10);
        DogService.INSTANCE.setDirty(false);
        setDirty(false);
    }

    public void walkedWithDog(boolean gettingDirty) {
        if((getTime() - DogService.INSTANCE.getTimeLastWalked()) > THIRTYMINUTES) {
            DogService.INSTANCE.updateSatisfaction(10, 75);
//            setView(DogMood.HAPPY);
        } else {
            DogService.INSTANCE.updateSatisfaction(-5, 60);
//            setView(DogMood.SAD);
        }
        if(Math.random() < 0.75 && gettingDirty) {
            setDirty(true);
//            setView(DogMood.DIRTY);
        }
        DogService.INSTANCE.setWantsToWalk(false);
    }

    public void hasEaten() {
        DogService.INSTANCE.setHungry(false);
//        setView(DogMood.HAPPY);
        setTimeLastEaten(new Date(getTime()));
    }

    // Getters & Setters
    public long getTimeLastPlayed() {
        return DogService.INSTANCE.getTimeLastPlayed();
    }

    public long getTimeLastEaten() {
        return DogService.INSTANCE.getTimeLastEaten();
    }

    public void setTimeLastEaten(Date date) {
        DogService.INSTANCE.setTimeLastEaten(date);
    }

    public void setTimeLastPlayed(Date date) {
        DogService.INSTANCE.setTimeLastPlayed(date);
    }

    public boolean getDirty() {
        return DogService.INSTANCE.getDirty();
    }

    public void setDirty(boolean dirty) {
        DogService.INSTANCE.setDirty(dirty);
    }

    public long getTime(){
        return DogService.INSTANCE.getTime();
    }

    public DogView getView() {
        return view;
    }

    public void setLastRefreshed(long time) {
        lastRefreshed = time;
    }

    public void setView(DogMood mood) {
        if (mood.equals(DogMood.DIRTY)) {
            view.setDirty();
            view.setSprite(DogMood.HAPPY);
        } else if (mood.equals(DogMood.BARKING)) {
            view.setSprite(mood);
            Log.e("BARKING", "IS NOW " + DogService.INSTANCE.getBarking());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DogService.INSTANCE.playMusic(R.raw.hondschors, true);
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            Log.e("InterruptedException", "setBarking innerclass has thrown an InterruptedException: " + e.getMessage());
                        }
                        DogService.INSTANCE.setBarking(false);
                        DogService.INSTANCE.stopMusic();
                    }
                }).start();
        } else {
            view.setSprite(mood);
        }
    }

    public long getTimeLastRefreshed() {
        return lastRefreshed;
    }

    public void setDirty(){
        view.setDirty();
    }

    public enum DogMood {
        BARKING, HAPPY, PLAYFULL, SAD, DEAD, DIRTY, HUNGRY, WALKFULL;

        public int getRes(){
            switch (this){
                case BARKING:
                    return R.drawable.dog_barking_30frames;
                case HAPPY:
                    return R.drawable.dog_happy30frames;
                case PLAYFULL:
                    return R.drawable.dog_playful30frames;
                case SAD:
                    return R.drawable.sad_dog30frames;
                case DEAD:
                    return R.drawable.dog_died30frames;
                case HUNGRY:
                    return R.drawable.dog_hungry30frames;
                case DIRTY:
                    return -1;
                case WALKFULL:
                    return -1;//TODO implement animation
                default:
                    return -1;
            }
        }

        public int getFrames(){
            switch (this){
                case BARKING:
                    return 30;
                case HAPPY:
                    return 30;
                case PLAYFULL:
                    return 30;
                case SAD:
                    return 30;
                case DIRTY:
                    return 30;
                case HUNGRY:
                    return 30;
                case DEAD:
                    return 30;
                case WALKFULL:
                    return -1; //TODO
                default:
                    return -1;
            }
        }
    }


}
