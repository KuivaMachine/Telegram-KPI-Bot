package org.example.kpitelegrambot.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.kpitelegrambot.data.DayNight;
import org.example.kpitelegrambot.data.EmployeePost;
import org.example.kpitelegrambot.data.EmployeeStatus;

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
    Long chatId;

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
}
