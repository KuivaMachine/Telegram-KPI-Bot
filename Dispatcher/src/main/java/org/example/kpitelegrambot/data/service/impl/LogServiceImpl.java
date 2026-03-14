package org.example.kpitelegrambot.data.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.kpitelegrambot.bot.enums.LogLevel;
import org.example.kpitelegrambot.data.service.LogService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void info(String message) {
        log.info(message);
        jdbcTemplate.update("INSERT INTO logs (message, level) VALUES (?,?)", message, LogLevel.INFO.name());
    }

    @Override
    public void error(String message) {
        log.error(message);
        jdbcTemplate.update("INSERT INTO logs (message, level) VALUES (?,?)", message, LogLevel.ERROR.name());
    }
}
