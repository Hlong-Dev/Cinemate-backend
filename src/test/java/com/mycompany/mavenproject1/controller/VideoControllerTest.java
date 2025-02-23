package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.VideoInfo;
import com.mycompany.mavenproject1.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoControllerTest {

    @Mock
    private VideoService videoService;

    @InjectMocks
    private VideoController videoController;

    private VideoInfo testVideo;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        // Khởi tạo test data với constructor của VideoInfo
        testVideo = new VideoInfo(
                "Test Video",
                "http://example.com/thumbnail.jpg",
                "10:00",
                "http://example.com/video.mp4"
        );

        // Khởi tạo mock request và response
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void testGetVideoList() {
        // Arrange
        List<VideoInfo> expectedVideos = Arrays.asList(testVideo);
        when(videoService.getVideoList()).thenReturn(expectedVideos);

        // Act
        List<VideoInfo> actualVideos = videoController.getVideoList();

        // Assert
        assertNotNull(actualVideos, "Video list should not be null");
        assertEquals(1, actualVideos.size(), "Should return one video");
        VideoInfo resultVideo = actualVideos.get(0);
        assertEquals(testVideo.getTitle(), resultVideo.getTitle(), "Title should match");
        assertEquals(testVideo.getThumbnail(), resultVideo.getThumbnail(), "Thumbnail should match");
        assertEquals(testVideo.getDuration(), resultVideo.getDuration(), "Duration should match");
        assertEquals(testVideo.getUrl(), resultVideo.getUrl(), "URL should match");
        verify(videoService).getVideoList();
    }

    @Test
    void testGetVideoList_EmptyList() {
        // Arrange
        when(videoService.getVideoList()).thenReturn(List.of());

        // Act
        List<VideoInfo> result = videoController.getVideoList();

        // Assert
        assertNotNull(result, "Should return empty list, not null");
        assertTrue(result.isEmpty(), "List should be empty");
        verify(videoService).getVideoList();
    }

    @Test
    void testStreamVideo() {
        // Arrange
        String fileName = "test-video.mp4";
        doNothing().when(videoService).getVideo(anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));

        // Act
        videoController.streamVideo(fileName, request, response);

        // Assert
        verify(videoService).getVideo(eq(fileName), eq(request), eq(response));
    }

    @Test
    void testStreamVideo_WithRangeHeader() {
        // Arrange
        String fileName = "test-video.mp4";
        request.addHeader("Range", "bytes=0-1000");
        doNothing().when(videoService).getVideo(anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));

        // Act
        videoController.streamVideo(fileName, request, response);

        // Assert
        verify(videoService).getVideo(eq(fileName), eq(request), eq(response));
        assertEquals("bytes=0-1000", request.getHeader("Range"), "Range header should be preserved");
    }

    @Test
    void testStreamVideo_VerifyResponseType() {
        // Arrange
        String fileName = "test-video.mp4";
        doAnswer(invocation -> {
            HttpServletResponse resp = invocation.getArgument(2);
            resp.setContentType("video/mp4");
            return null;
        }).when(videoService).getVideo(anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));

        // Act
        videoController.streamVideo(fileName, request, response);

        // Assert
        verify(videoService).getVideo(eq(fileName), eq(request), eq(response));
        assertEquals("video/mp4", response.getContentType(), "Content type should be video/mp4");
    }

    @Test
    void testGetVideoList_ServiceException() {
        // Arrange
        when(videoService.getVideoList()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            videoController.getVideoList();
        }, "Should throw exception when service fails");
        verify(videoService).getVideoList();
    }

    @Test
    void testStreamVideo_ServiceException() {
        // Arrange
        String fileName = "test-video.mp4";
        doThrow(new RuntimeException("Streaming error"))
                .when(videoService)
                .getVideo(anyString(), any(HttpServletRequest.class), any(HttpServletResponse.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            videoController.streamVideo(fileName, request, response);
        }, "Should throw exception when streaming fails");
        verify(videoService).getVideo(eq(fileName), eq(request), eq(response));
    }
}