import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {

            Intent serviceIntent = new Intent(context, PlayerService.class);
            // برای نسخه‌های جدید اندروید، باید سرویس را به صورت Foreground اجرا کرد
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}