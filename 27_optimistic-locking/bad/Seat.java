package com.example.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private String seatNumber;   // 例: "A-12"

    private String status;       // "AVAILABLE" / "RESERVED"

    private Long reservedUserId;
}
