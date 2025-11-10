package com.example.sharedistribution.repository;

import com.example.sharedistribution.entity.Trade;
import com.example.sharedistribution.entity.TradeDistribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeDistributionRepository extends JpaRepository<TradeDistribution, Long> {
    List<TradeDistribution> findByTrade(Trade trade);
}
