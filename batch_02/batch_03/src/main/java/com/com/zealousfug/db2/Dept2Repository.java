// 받을 쪽의 DB

package com.com.zealousfug.db2;

import com.com.zealousfug.db1.Dept1;
import org.springframework.data.repository.CrudRepository;

public interface Dept2Repository extends CrudRepository<Dept2, Integer> {
}
