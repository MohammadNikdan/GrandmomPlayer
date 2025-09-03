import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class PlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // تنظیمات تمام صفحه کردن
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        videoView = findViewById(R.id.videoView);
        videoPath = getIntent().getStringExtra("VIDEO_PATH");

        if (videoPath != null) {
            Uri videoUri = Uri.parse(videoPath);
            videoView.setVideoURI(videoUri);
            // حذف کنترل‌های پیش‌فرض
            videoView.setMediaController(null);

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // ویدئو تا انتها پخش شد
                    updateLastEpisodeFile();
                    PlayerService.isVideoPlaying = false;
                    finish(); // این اکتیویتی را ببند
                    // سرویس را دوباره صدا بزن تا ویدئوی بعدی را پخش کند
                    startService(new Intent(PlayerActivity.this, PlayerService.class));
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // اگر در پخش خطایی رخ داد
                    PlayerService.isVideoPlaying = false;
                    finish(); // اکتیویتی را ببند
                    return true;
                }
            });


            videoView.start();
        }
    }

    private void updateLastEpisodeFile() {
        File videoFile = new File(videoPath);
        String fileName = videoFile.getName(); // مثلا "1.mp4"
        String episodeNumberStr = fileName.substring(0, fileName.lastIndexOf('.'));
        try {
            int episodeNumber = Integer.parseInt(episodeNumberStr);
            File usbDrive = videoFile.getParentFile();
            File textFile = new File(usbDrive, "lastepisode.txt");
            FileHelper.writeLastEpisode(textFile, episodeNumber);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // اگر کاربر یا سیستم برنامه را متوقف کرد (مثلا دستگاه خاموش شد)
        // اطمینان حاصل کن که isVideoPlaying false شود تا پخش بعدی ممکن باشد
        if(videoView.isPlaying()){
            PlayerService.isVideoPlaying = false;
        }
    }
}