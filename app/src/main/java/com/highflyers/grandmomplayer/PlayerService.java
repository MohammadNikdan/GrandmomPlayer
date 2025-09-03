import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.File;

public class PlayerService extends Service {

    public static boolean isVideoPlaying = false;
    private String usbDrivePath;
    private static final String CHANNEL_ID = "PlayerServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        findUsbDrive();
        if (usbDrivePath != null) {
            startVideoPlaybackLogic();
        }
        return START_STICKY;
    }

    private void findUsbDrive() {
        File[] externalStorageVolumes = getExternalFilesDirs(null);
        if (externalStorageVolumes == null) return;

        for (File volume : externalStorageVolumes) {
            if (volume != null && !volume.getAbsolutePath().contains("emulated")) {
                String path = volume.getAbsolutePath();
                usbDrivePath = path.substring(0, path.indexOf("/Android"));
                return;
            }
        }
        // Fallback for some devices
        File storageDir = new File("/storage");
        if (storageDir.exists()) {
            for (File file : storageDir.listFiles()) {
                if (file.isDirectory() && file.canRead() && !file.getName().equalsIgnoreCase("self") && !file.getName().equalsIgnoreCase("emulated")) {
                    usbDrivePath = file.getAbsolutePath();
                    return;
                }
            }
        }
    }

    private void startVideoPlaybackLogic() {
        if (isVideoPlaying || usbDrivePath == null) {
            return;
        }

        File textFile = new File(usbDrivePath, "lastepisode.txt");
        if (!textFile.exists()) {
            return;
        }

        int lastEpisode = FileHelper.readLastEpisode(textFile);
        int nextEpisode = lastEpisode + 1;

        File nextVideoFile = findNextVideoFile(nextEpisode);

        if (nextVideoFile != null && nextVideoFile.exists()) {
            playVideo(nextVideoFile.getAbsolutePath());
        } else {
            FileHelper.writeLastEpisode(textFile, 0);
            File firstVideoFile = findNextVideoFile(1);
            if (firstVideoFile != null && firstVideoFile.exists()) {
                playVideo(firstVideoFile.getAbsolutePath());
            }
        }
    }

    private File findNextVideoFile(int episodeNumber) {
        // Common video extensions
        String[] extensions = {".mp4", ".mkv", ".avi", ".mov"};
        for (String ext : extensions) {
            File videoFile = new File(usbDrivePath, episodeNumber + ext);
            if (videoFile.exists()) {
                return videoFile;
            }
        }
        return null;
    }

    private void playVideo(String videoPath) {
        isVideoPlaying = true;
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("VIDEO_PATH", videoPath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Player Service Channel",
                    NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto Player Service")
                .setContentText("Service is active.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
