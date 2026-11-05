package com.accenture.meetspace.bootstrap.app;

import com.accenture.meetspace.reservation.ReservationApplication;
import com.accenture.meetspace.room.RoomApplication;
import com.accenture.meetspace.user.UserApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({RoomApplication.class, ReservationApplication.class, UserApplication.class})
public class MeetspaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetspaceApplication.class, args);
    }

}
