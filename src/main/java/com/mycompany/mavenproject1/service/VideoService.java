package com.mycompany.mavenproject1.service;

import com.mycompany.mavenproject1.model.VideoInfo;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class VideoService {

    private static final String VIDEO_DIRECTORY = "C:/Users/hlongday/Downloads/";
    private static final int CHUNK_SIZE = 500 * 1024; // 500KB per chunk
    private static final String THUMBNAIL_DIRECTORY = "C:/Users/hlongday/Downloads/thumbnails/";

    // Phương thức để lấy danh sách video với thông tin chi tiết
    public List<VideoInfo> getVideoList() {
        File dir = new File(VIDEO_DIRECTORY);
        List<VideoInfo> videoFiles = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".mp4")) {
                    String title = file.getName();
                    String thumbnail = generateThumbnail(file.getName()); // Tạo thumbnail động từ video
                    String duration = "00:00"; // Bạn có thể cập nhật logic để tính thời lượng thực tế
                    String url = "/video/play/" + file.getName();
                    videoFiles.add(new VideoInfo(title, thumbnail, duration, url));
                }
            }
        }
        return videoFiles;
    }

    // Phương thức để tạo thumbnail từ video
    String generateThumbnail(String videoFileName) {
        String videoPath = VIDEO_DIRECTORY + videoFileName;
        String thumbnailFileName = videoFileName.replace(".mp4", ".jpg");
        String thumbnailPath = THUMBNAIL_DIRECTORY + thumbnailFileName;

        // Kiểm tra và tạo thư mục thumbnails nếu chưa tồn tại
        File thumbnailDir = new File(THUMBNAIL_DIRECTORY);
        if (!thumbnailDir.exists()) {
            thumbnailDir.mkdirs(); // Tạo thư mục nếu không tồn tại
        }

        // Kiểm tra xem thumbnail đã tồn tại chưa, nếu chưa thì tạo mới
        File thumbnailFile = new File(thumbnailPath);
        if (!thumbnailFile.exists()) {
            try {
                // Chỉ định đường dẫn đầy đủ tới ffmpeg.exe
                ProcessBuilder pb = new ProcessBuilder("C:/ffmpeg/ffmpeg-7.1-essentials_build/bin/ffmpeg", "-i", videoPath, "-ss", "00:00:05", "-vframes", "1", thumbnailPath);
                pb.inheritIO();
                Process process = pb.start();
                process.waitFor(); // Đợi FFmpeg hoàn tất việc tạo thumbnail
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return "/thumbnails/default.jpg"; // Trả về ảnh mặc định nếu có lỗi
            }
        }

        // Thay dấu cách bằng %20 mà không mã hóa các ký tự khác
        String encodedFileName = thumbnailFileName.replace(" ", "%20");
        return "/thumbnails/" + encodedFileName;
    }



    // Phương thức phát video
    public void getVideo(String fileName, HttpServletRequest request, HttpServletResponse response) {
        File videoFile = new File(VIDEO_DIRECTORY + fileName);

        if (!videoFile.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long length = videoFile.length();
        long rangeStart = 0;
        long rangeEnd = length - 1;
        boolean isPartial = false;

        String rangeHeader = request.getHeader("Range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            try {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                isPartial = true;
                String[] ranges = rangeHeader.replace("bytes=", "").split("-");
                rangeStart = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                } else {
                    rangeEnd = Math.min(rangeStart + CHUNK_SIZE - 1, length - 1);
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        long contentLength = rangeEnd - rangeStart + 1;
        response.setContentType("video/mp4");
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Accept-Ranges", "bytes");

        if (isPartial) {
            response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + length);
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[CHUNK_SIZE];
            try (RandomAccessFile file = new RandomAccessFile(videoFile, "r")) {
                file.seek(rangeStart);
                long bytesRemaining = contentLength;
                while (bytesRemaining > 0) {
                    int bytesToRead = (int) Math.min(CHUNK_SIZE, bytesRemaining);
                    int bytesRead = file.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, bytesRead);
                    bytesRemaining -= bytesRead;
                }
            }
            outputStream.flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}

