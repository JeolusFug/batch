// 주는 쪽의 DB

package com.com.zealousfug.db1;

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
@Table(name = "dept1")
public class Dept1 {
    @Id
    private Integer deptno;
    private String dname;
    private String loc;
}
