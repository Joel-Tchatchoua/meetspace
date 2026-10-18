package com.accenture.meetspace;

import org.springframework.boot.SpringApplication;

public class TestMeetspaceApplication {

    public static void main(String[] args) {
        SpringApplication.from(MeetspaceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
