// 받을 쪽의 Db

package com.com.zealousfug.db2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "dept2")
public class Dept2 {
    @Id
    private Integer deptno;
    private String dname;
    private String loc;
}
