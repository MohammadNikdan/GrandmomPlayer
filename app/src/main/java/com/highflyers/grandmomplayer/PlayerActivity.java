import android.content.Intent; // <--- ایمپورت مهم اضافه شد
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        videoView = findViewById(R.id.videoView);
        videoPath = getIntent().getStringExtra("VIDEO_PATH");

        if (videoPath != null) {
            Uri videoUri = Uri.parse(videoPath);
            videoView.setVideoURI(videoUri);
            videoView.setMediaController(null);

            videoView.setOnCompletionListener(mp -> {
                updateLastEpisodeFile();
                PlayerService.isVideoPlaying = false;
                startService(new Intent(PlayerActivity.this, PlayerService.class));
                finish();
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                PlayerService.isVideoPlaying = false;
                finish();
                return true;
            });

            videoView.start();
        }
    }

    private void updateLastEpisodeFile() {
        if (videoPath == null) return;
        File videoFile = new File(videoPath);
        String fileName = videoFile.getName();
        String episodeNumberStr = fileName.substring(0, fileName.lastIndexOf('.'));
        try {
            int episodeNumber = Integer.parseInt(episodeNumberStr);
            File usbDrive = videoFile.getParentFile();
            if (usbDrive != null) {
                File textFile = new File(usbDrive, "lastepisode.txt");
                FileHelper.writeLastEpisode(textFile, episodeNumber);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            PlayerService.isVideoPlaying = false;
        }
    }
}
