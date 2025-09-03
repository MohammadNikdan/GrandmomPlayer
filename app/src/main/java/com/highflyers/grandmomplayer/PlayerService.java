import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.FileObserver;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.File;

public class PlayerService extends Service {

    public static boolean isVideoPlaying = false;
    private FileObserver observer;
    private String usbDrivePath;

    @Override
    public void onCreate() {
        super.onCreate();
        // اجرای سرویس به صورت Foreground برای جلوگیری از توقف توسط سیستم
        startForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // پیدا کردن مسیر فلش USB
        findUsbDrive();
        // اگر فلش پیدا شد، شروع به بررسی کن
        if (usbDrivePath != null) {
            startVideoPlaybackLogic();
        }
        // اگر سرویس توسط سیستم کشته شد، دوباره راه‌اندازی شود
        return START_STICKY;
    }

    private void findUsbDrive() {
        // مسیرهای معمول برای حافظه‌های جانبی
        File[] externalStorageVolumes = getExternalFilesDirs(null);
        for (File volume : externalStorageVolumes) {
            if (volume != null) {
                // معمولا حافظه جانبی در مسیرهایی غیر از emulated قرار دارد
                if (!volume.getAbsolutePath().contains("emulated")) {
                    usbDrivePath = volume.getAbsolutePath().split("/Android")[0];
                    break;
                }
            }
        }
    }


    private void startVideoPlaybackLogic() {
        if (isVideoPlaying || usbDrivePath == null) {
            return; // اگر ویدئویی در حال پخش است یا فلش وجود ندارد، کاری نکن
        }

        File textFile = new File(usbDrivePath, "lastepisode.txt");
        if (!textFile.exists()) {
            return; // اگر فایل تکست وجود ندارد، کاری نکن
        }

        int lastEpisode = FileHelper.readLastEpisode(textFile);
        int nextEpisode = lastEpisode + 1;

        File nextVideoFile = new File(usbDrivePath, nextEpisode + ".mp4"); // فرض بر پسوند mp4

        if (nextVideoFile.exists()) {
            playVideo(nextVideoFile.getAbsolutePath());
        } else {
            // اگر ویدئوی بعدی وجود نداشت، یعنی تمام شده. از اول شروع کن
            FileHelper.writeLastEpisode(textFile, 0);
            File firstVideoFile = new File(usbDrivePath, "1.mp4");
            if (firstVideoFile.exists()) {
                playVideo(firstVideoFile.getAbsolutePath());
            }
        }
    }

    private void playVideo(String videoPath) {
        isVideoPlaying = true;
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("VIDEO_PATH", videoPath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void startForegroundService() {
        String CHANNEL_ID = "PlayerServiceChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Player Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("سرویس پخش فعال است")
                .setContentText("در حال بررسی برای پخش ویدئو...")
                .setSmallIcon(R.mipmap.ic_launcher) // آیکون اپلیکیشن
                .build();

        startForeground(1, notification);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}