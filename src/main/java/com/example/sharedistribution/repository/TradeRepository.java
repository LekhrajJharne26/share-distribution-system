package com.example.sharedistribution.repository;

import com.example.sharedistribution.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
}
