import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHelper {

    public static int readLastEpisode(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            return Integer.parseInt(line.trim());
        } catch (IOException | NumberFormatException e) {
            return 0; // اگر فایل خالی بود یا عدد نبود، از صفر شروع کن
        }
    }

    public static void writeLastEpisode(File file, int episodeNumber) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(episodeNumber));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}