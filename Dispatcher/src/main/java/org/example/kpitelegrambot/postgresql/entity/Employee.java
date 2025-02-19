package org.example.kpitelegrambot.postgresql.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.kpitelegrambot.postgresql.data.DayNight;
import org.example.kpitelegrambot.postgresql.data.EmployeePost;
import org.example.kpitelegrambot.postgresql.data.EmployeeStatus;

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

}
