package com.mycompany.mavenproject1.service;

import com.mycompany.mavenproject1.model.VideoInfo;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VideoServiceTest {

    private VideoService videoService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @TempDir
    Path tempDir;

    private Path videoDir;
    private Path thumbnailDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // Tạo thư mục tạm cho video và thumbnail
        videoDir = tempDir.resolve("videos");
        thumbnailDir = tempDir.resolve("thumbnails");
        Files.createDirectories(videoDir);
        Files.createDirectories(thumbnailDir);

        // Khởi tạo VideoService với đường dẫn tạm
        videoService = new VideoService();

        // Mock response outputStream
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    void getVideoList_WithValidDirectory_ReturnsVideoList() throws IOException {
        // Arrange
        createMockVideoFile("test1.mp4");
        createMockVideoFile("test2.mp4");
        createMockVideoFile("notVideo.txt");

        // Act
        List<VideoInfo> videos = videoService.getVideoList();

        // Assert
        assertNotNull(videos);
        assertEquals(2, videos.size());
        assertTrue(videos.stream().allMatch(v -> v.getTitle().endsWith(".mp4")));
    }

    @Test
    void getVideoList_WithEmptyDirectory_ReturnsEmptyList() {
        // Act
        List<VideoInfo> videos = videoService.getVideoList();

        // Assert
        assertNotNull(videos);
        assertTrue(videos.isEmpty());
    }

    @Test
    void getVideo_WithValidRequest_StreamsVideo() throws IOException {
        // Arrange
        String fileName = "test.mp4";
        createMockVideoFile(fileName);
        when(request.getHeader("Range")).thenReturn(null);

        // Act
        videoService.getVideo(fileName, request, response);

        // Assert
        verify(response).setContentType("video/mp4");
        verify(response).setHeader(eq("Accept-Ranges"), eq("bytes"));
        verify(outputStream, atLeastOnce()).write(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    void getVideo_WithRangeRequest_StreamsPartialContent() throws IOException {
        // Arrange
        String fileName = "test.mp4";
        createMockVideoFile(fileName);
        when(request.getHeader("Range")).thenReturn("bytes=0-1000");

        // Act
        videoService.getVideo(fileName, request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        verify(response).setHeader(eq("Content-Range"), matches("bytes 0-1000/.*"));
    }

    @Test
    void getVideo_WithInvalidFile_Returns404() {
        // Arrange
        String fileName = "nonexistent.mp4";

        // Act
        videoService.getVideo(fileName, request, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void generateThumbnail_CreatesNewThumbnail() throws IOException {
        // Arrange
        String videoFileName = "test.mp4";
        createMockVideoFile(videoFileName);

        // Act
        String thumbnailPath = videoService.generateThumbnail(videoFileName);

        // Assert
        assertNotNull(thumbnailPath);
        assertTrue(thumbnailPath.startsWith("/thumbnails/"));
        assertTrue(thumbnailPath.endsWith(".jpg"));
    }

    private void createMockVideoFile(String fileName) throws IOException {
        Path filePath = videoDir.resolve(fileName);
        Files.write(filePath, "Mock video content".getBytes());
    }
}