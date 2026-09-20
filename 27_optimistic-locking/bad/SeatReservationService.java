package com.example.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatReservationService {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final MailService mailService;

    // イベントの空いている座席を1つ取って予約する
    public String reserve(Long eventId, Long userId) {
        try {
            // 1. このイベントの全座席から空席を探す
            List<Seat> seats = seatRepository.findByEventId(eventId);
            Seat target = null;
            for (Seat seat : seats) {
                if (seat.getStatus().equals("AVAILABLE")) {
                    target = seat;
                    break;
                }
            }

            // 2. 空席を「予約済み」に更新する
            target.setStatus("RESERVED");
            
            target.setReservedUserId(userId);
            seatRepository.save(target);

            // 3. 予約レコードを作る
            Reservation reservation = new Reservation();
            reservation.setEventId(eventId);
            reservation.setSeatId(target.getId());
            reservation.setUserId(userId);
            reservationRepository.save(reservation);

            // 4. 確認メールを送る
            mailService.sendReservationMail(userId, target.getId());

            return "予約が完了しました（座席ID: " + target.getId() + "）";
        } catch (Exception e) {
            e.printStackTrace();
            return "予約に失敗しました";
        }
    }

    // 座席番号を指定して予約する（座席選択画面から）
    public String reserveSeat(Long seatId, Long userId) {
        try {
            Seat seat = seatRepository.findById(seatId).get();
            seat.setStatus("RESERVED");
            seat.setReservedUserId(userId);
            seatRepository.save(seat);

            Reservation reservation = new Reservation();
            reservation.setEventId(seat.getEventId());
            reservation.setSeatId(seatId);
            reservation.setUserId(userId);
            reservationRepository.save(reservation);

            return "予約が完了しました";
        } catch (Exception e) {
            e.printStackTrace();
            return "予約に失敗しました";
        }
    }
}
