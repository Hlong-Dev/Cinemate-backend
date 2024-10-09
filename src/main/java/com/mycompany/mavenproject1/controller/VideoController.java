package com.mycompany.mavenproject1.controller;

import com.mycompany.mavenproject1.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/play")
    public void streamVideo(HttpServletRequest request, HttpServletResponse response) {
        videoService.getVideo("a.mp4", request, response);
    }
}
