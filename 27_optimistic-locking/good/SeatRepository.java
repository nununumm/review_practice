package com.example.reservation;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * 空席「だけ」をDBで絞り込んで、必要な1件だけ取得する。
     *
     * bad/ では findByEventId で全座席をロードし、アプリ側の for ループで空席を探していた。
     * それだと1万席のイベントで空席1つ探すのに1万件メモリに載せることになる。
     * WHERE で status を絞り、LIMIT 1（PageRequest.of(0, 1)）で1件だけ取れば、
     * DBが得意な仕事をDBに任せられて速いし省メモリ。
     */
    @Query("SELECT s FROM Seat s WHERE s.eventId = :eventId AND s.status = com.example.reservation.SeatStatus.AVAILABLE")
    List<Seat> findAvailableSeats(@Param("eventId") Long eventId, PageRequest pageable);

    /**
     * ▼参考：悲観的ロック版（争奪が激しい席指定予約で使うならこちら）。
     *
     * @Lock(PESSIMISTIC_WRITE) を付けると "SELECT ... FOR UPDATE" が発行され、
     * この行を読んだ瞬間にDBがロックをかける。他のトランザクションは
     * コミットされるまでこの行の取得で「待たされる」ので、そもそも同時更新が起きない。
     * 楽観ロック（@Version）は衝突後にリトライが要るが、悲観ロックは待たせて確実に捌く。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);
}
