package com.com.jeoluspug.domain;

import lombok.*;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;

@Getter
@Setter
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Dept {

    @javax.persistence.Id
    @Id
    Integer deptNo;
    String dName;
    String loc;

}
