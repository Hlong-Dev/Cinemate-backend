package com.mycompany.mavenproject1.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

@Service
public class VideoService {

    private static final String VIDEO_PATH = "C:/Users/hlongday/Downloads/a.mp4";
    private static final int BUFFER_SIZE = 8192; // Tăng kích thước buffer để tăng hiệu suất

    public void getVideo(String fileName, HttpServletRequest request, HttpServletResponse response) {
        File videoFile = new File(VIDEO_PATH);

        // Kiểm tra nếu file không tồn tại
        if (!videoFile.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long length = videoFile.length();
        long rangeStart = 0;
        long rangeEnd = length - 1;
        boolean isPartial = false;

        // Kiểm tra tiêu đề "Range" từ client
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            try {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                isPartial = true;

                // Phân tích đoạn range
                String[] ranges = rangeHeader.replace("bytes=", "").split("-");
                rangeStart = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                } else {
                    rangeEnd = Math.min(rangeStart + 1024 * 1024 * 5, length - 1); // Giới hạn kích thước mỗi chunk
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        long contentLength = rangeEnd - rangeStart + 1;

        // Thiết lập các tiêu đề phản hồi
        response.setContentType("video/mp4");
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Accept-Ranges", "bytes"); // Hỗ trợ yêu cầu range
        if (isPartial) {
            response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + length);
        }

        try (RandomAccessFile file = new RandomAccessFile(videoFile, "r");
             OutputStream outputStream = response.getOutputStream()) {

            file.seek(rangeStart);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long bytesToRead = contentLength;

            // Ghi dữ liệu từ file vào luồng xuất
            while (bytesToRead > 0 && (bytesRead = file.read(buffer, 0, (int) Math.min(BUFFER_SIZE, bytesToRead))) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesToRead -= bytesRead;
            }

            outputStream.flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
