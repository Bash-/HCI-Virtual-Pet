package example.com.virtualpet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by reneb_000 on 30-12-2015.
 * Last updated by Gijs on 07-Jan-2016.
 */
public class DogService extends Service implements Runnable {

    public static DogService INSTANCE;

    private int satisfaction;
    public static final int MAXSATISFACTION = 100;
    public static final int MINSATISFACTION = 0;
    public static final long THIRTYMINUTES = 1800000; // thirty minutes in milliseconds
    public static final long GetTimeFASTER = 1; // increase to make getTime(), which is used in DogService.java & Dog.java, return higher values.

    protected ArrayList<Calendar> eatTimes = new ArrayList<Calendar>();
    protected ArrayList<Calendar> walkTimes = new ArrayList<Calendar>();
    private Calendar now = Calendar.getInstance();
    private long lastPlayed;
    private long lastWalked;
    private long lastEaten;
    private boolean dirty;
    private boolean hungry;
    private boolean wantsToWalk;
    private boolean wantsToPlay;
    private int daysAlive = 0;
    private boolean dead = false;
    private boolean barking;
    MediaPlayer mMediaPlayer = new MediaPlayer();


    private Date startDay;

    NotificationManager mNotificationManager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        INSTANCE = this;
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        new Thread(this).start();
        initialize();
        super.onCreate();
//        playMusic(R.raw.who_let_the_dogs_out_mp3cutnet, false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        //intent.addCategory("DogService");
        //startService(intent);

        //onBind(intent);

        while (true){
            try {
                Thread.sleep(5000);
                //showNotification("DogService", "Still running!");

                update();

            } catch (InterruptedException e) {
                Log.e("DogService", "InterruptedException: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void initialize() {
        startDay = new Date(getTime());
        setTimeLastEaten(new Date(getTime()));
        setTimeLastPlayed(new Date(getTime()));
        setTimeLastWalked(new Date(getTime()));
        setDirty(false);
        setHungry(false);
        setWantsToPlay(false);
        setWantsToWalk(false);
        setSatisfaction(65);
        setEatTimes();
        setWalkTimes();
        setDead(false);
    }

    public void update() {
        checkForEatTime();
        checkForWalkTime();
        checkStatus();
        checkForDead();
        updateMoney();
    }

    public void checkForDead() {
        if (getSatisfaction() < 10) {
            setDead(true);
            Log.e("Dog", "Dog has died with satisfaction of " + getSatisfaction());
        }
    }

    public void checkStatus() {
        if ((getTime() - getTimeLastPlayed()) > (5*THIRTYMINUTES) && (getTime() - getTimeLastEaten()) > THIRTYMINUTES) {
            showNotification("Woef, Waf!", "Ik verveel me!");
            updateSatisfaction(-5);
            setWantsToPlay(true);
            setWantsToWalk(false);
        }
        if ((getTime() - getTimeLastWalked()) > (5*THIRTYMINUTES)) {
            showNotification("Woef, Waf!", "Ik wil een rondje lopen!");
            updateSatisfaction(-10);
            setWantsToWalk(true);
            setWantsToPlay(true);
        }
    }

    // Eating
    public void setEatTimes() {
        Calendar eatMorning = Calendar.getInstance();
        Calendar eatAfternoon = Calendar.getInstance();
        Calendar eatEvening = Calendar.getInstance();
        eatMorning.set(Calendar.HOUR_OF_DAY, 8);
        eatAfternoon.set(Calendar.HOUR_OF_DAY, 13);
        eatEvening.set(Calendar.HOUR_OF_DAY, 19);
        eatTimes.add(eatMorning);
        eatTimes.add(eatAfternoon);
        eatTimes.add(eatEvening);
    }

    public void checkForEatTime() {
        for (int i = 0; i < eatTimes.size(); i++) {
            if (eatTimes.get(i).get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) && getTime() - getTimeLastEaten() > 2 * THIRTYMINUTES) {
                showNotification("Woef, Waf!", "Ik heb honger!");
                updateSatisfaction(-5);
                setHungry(true);
            }
        }
    }

    // Walking
    public void setWalkTimes() {
        Calendar walkMorning = Calendar.getInstance();
        Calendar walkNoon = Calendar.getInstance();
        Calendar walkAfternoon = Calendar.getInstance();
        Calendar walkEvening = Calendar.getInstance();
        walkMorning.set(Calendar.HOUR_OF_DAY, 7);
        walkNoon.set(Calendar.HOUR_OF_DAY, 12);
        walkAfternoon.set(Calendar.HOUR_OF_DAY, 17);
        walkEvening.set(Calendar.HOUR_OF_DAY, 21);
        walkTimes.add(walkMorning);
        walkTimes.add(walkNoon);
        walkTimes.add(walkAfternoon);
        walkTimes.add(walkEvening);
    }

    public void checkForWalkTime() {
        for (int i = 0; i < walkTimes.size(); i++) {
            if (walkTimes.get(i).get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) && getTime() - getTimeLastWalked() > 2 * THIRTYMINUTES) {
                showNotification("Woef Waf!", "Ik wil buiten een rondje lopen!");
                setWantsToWalk(true);
            }
        }
    }

    // Satisfaction
    public void updateSatisfaction(int satisfaction) {
        synchronized (this) {
            //if satisfaction is larger than max satisfaction
            // satisfaction = max
            // else if satisfaaction
            if (this.satisfaction + satisfaction > MAXSATISFACTION ) {
                this.satisfaction = MAXSATISFACTION;
            } else if (this.satisfaction + satisfaction < MINSATISFACTION) {
                this.satisfaction = MINSATISFACTION;
            } else {
                this.satisfaction += satisfaction;
            }
            satisfactionChanged();
        }
    }

    public void updateSatisfaction(int satisfaction, long chance) {
        if (Math.random() * 100 <= chance) {
            synchronized (this) {
                if (this.satisfaction + satisfaction > MAXSATISFACTION) {
                    this.satisfaction = MAXSATISFACTION;
                } else if (this.satisfaction + satisfaction < MINSATISFACTION) {
                    this.satisfaction = MINSATISFACTION;
                } else {
                    this.satisfaction += satisfaction;
                }
                satisfactionChanged();
            }
        }
    }

    private void satisfactionChanged(){
        //TODO observer pattern would be nice here, but hard to implement. Workaround is added
    }

    public void updateMoney() {
        if (daysAlive < getDaysAlive() && !dead) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            int currentMoney = sharedPref.getInt(getString(R.string.moneyString), 20);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.moneyString), (currentMoney + 1));
            editor.apply();
            daysAlive = getDaysAlive();
        }
    }

//    Getters

    public boolean getHungry() {
        synchronized (this) {
            return this.hungry;
        }
    }

    public long getTimeLastWalked() {
        synchronized (this) {
            return lastWalked;
        }
    }

    public boolean getDirty() {
        synchronized (this) {
            return this.dirty;
        }
    }

    public int getSatisfaction() {
        synchronized (this) {
            return satisfaction;
        }
    }

    public long getTimeLastPlayed() {
        synchronized(this) {
            return lastPlayed;
        }
    }

    public long getTimeLastEaten() {
        synchronized(this) {
            return lastEaten;
        }
    }

    public long getTime(){
        synchronized (this) {
            long time = System.currentTimeMillis() * GetTimeFASTER;
            //Log.e("Application Time:", new Date(time).toString());
            return time;
        }
    }

    public boolean getWantsToWalk() {
        synchronized (this) {
            return wantsToWalk;
        }
    }

    public boolean getWantsToPlay() {
        synchronized (this) {
            return wantsToPlay;
        }
    }

    //    Setters

    public void setHungry(boolean hungry) {
        synchronized (this) {
            this.hungry = hungry;
        }
    }

    public void setDirty(boolean dirty) {
        synchronized (this) {
          this.dirty = dirty;
        }
    }

    public void setSatisfaction(int satisfaction) {
        synchronized (this) {
            this.satisfaction = satisfaction;
        }
    }

    public void setTimeLastEaten(Date date) {
        synchronized (this) {
            this.lastEaten = date.getTime();
        }
    }

    public void setTimeLastPlayed(Date date) {
        synchronized (this) {
            this.lastPlayed = date.getTime();
        }
    }

    public void setTimeLastWalked(Date date) {
        synchronized (this) {
            this.lastWalked = date.getTime();
        }
    }

    public void setWantsToWalk(boolean wantsToWalk) {
        synchronized (this) {
            this.wantsToWalk = wantsToWalk;
        }
    }

    public void setWantsToPlay(boolean wantsToPlay) {
        synchronized (this) {
            this.wantsToPlay = wantsToPlay;
        }
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean getDead() {
        return dead;
    }

    public boolean getMood(Dog.DogMood m){
        synchronized (this){
            switch (m){
                case DIRTY:
                    return dirty;
                case HUNGRY:
                    return hungry;
                case PLAYFULL:
                    return wantsToPlay;
                case WALKFULL:
                    return wantsToWalk;
                case DEAD:
                    return dead;
                case HAPPY:
                    return true;
                case SAD:
                    return satisfaction<=30;
                case BARKING:
                    return barking;

            }
        }
        return false;
    }

    public int getDaysAlive(){
        Date now = new Date(getTime());
        return get_days_between_dates(startDay, now);
    }

    private int get_days_between_dates(Date date1, Date date2) {
        //if date2 is more in the future than date1 then the result will be negative
        //if date1 is more in the future than date2 then the result will be positive.

        return (int)((date2.getTime() - date1.getTime()) / (1000*60*60*24l));
    }

    public void printState(){
        Log.e("Dogstate", "Hungry: " + hungry + "\nWantstoPlay: " + wantsToPlay + "\nSad: " + (satisfaction <= 20) + "\nDirty: " + dirty + "\nDead: " + dead + "\nBarking: " + barking);
    }

    private void showNotification(String title, String content){
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification noti = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(resultPendingIntent)
                .build();
        mNotificationManager.notify(0, noti);
    }

    public void setBarking(boolean barking) {
        this.barking = barking;
    }

    public void playMusic(int uri, boolean looping) {
        mMediaPlayer = MediaPlayer.create(this, uri);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(100,100);
        mMediaPlayer.setLooping(looping);
        mMediaPlayer.start();
    }

    public void stopMusic() {
        mMediaPlayer.stop();
    }

    public void playVideo(int uri, boolean looping) {
        mMediaPlayer = MediaPlayer.create(this, uri);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(100, 100);
        mMediaPlayer.setLooping(looping);
        mMediaPlayer.start();
    }

    public void stopVideo() {
        mMediaPlayer.stop();
    }

    public boolean getBarking() {
        return barking;
    }
}
