package com.com.jealouspug.domain;

import lombok.*;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;

@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Dept2 {

    @javax.persistence.Id
    @Id
    Integer deptNo;
    String dName;
    String loc;

}
