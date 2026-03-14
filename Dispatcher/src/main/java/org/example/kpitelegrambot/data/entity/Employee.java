package org.example.kpitelegrambot.data.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.kpitelegrambot.bot.enums.DayNight;
import org.example.kpitelegrambot.bot.enums.EmployeePost;
import org.example.kpitelegrambot.bot.enums.EmployeeStatus;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "employees")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {

    @Id
    @Column(name = "chat_id")
    long chatId;

    @Column(name = "FIO")
    String fio;

    @Column(name = "username")
    String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "job")
    EmployeePost job;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_time")
    DayNight workTime;

    @Column(name = "fired")
    LocalDate fired;

    @Override
    public String toString() {
        return "Employee{" +
                "chatId=" + chatId +
                ", fio='" + fio + '\'' +
                ", username='" + username + '\'' +
                ", status=" + status +
                ", job=" + job +
                ", workTime=" + workTime +
                '}';
    }
}
