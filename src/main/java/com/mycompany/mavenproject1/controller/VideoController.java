package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.model.VideoInfo;
import com.mycompany.mavenproject1.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    // API để lấy danh sách video cùng thông tin (thumbnail, thời lượng,...)
    @GetMapping("/list")
    public List<VideoInfo> getVideoList() {
        return videoService.getVideoList();
    }

    // API để phát video dựa trên tên video được chọn
    @GetMapping("/play/{fileName}")
    public void streamVideo(@PathVariable String fileName, HttpServletRequest request, HttpServletResponse response) {
        videoService.getVideo(fileName, request, response);
    }
}
