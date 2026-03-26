package com.sbms.busbookingsystem.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sbms.busbookingsystem.entity.BusBooking;

@Repository
public interface BusBookingRepo extends JpaRepository<BusBooking, Long> {
}

