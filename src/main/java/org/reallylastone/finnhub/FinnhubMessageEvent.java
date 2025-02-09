package org.reallylastone.finnhub;

import org.reallylastone.trade.domain.Trade;

import java.util.List;

public record FinnhubMessageEvent(List<Trade> data, String type) {
}