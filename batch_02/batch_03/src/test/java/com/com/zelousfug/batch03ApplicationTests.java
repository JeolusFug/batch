package com.com.zelousfug;

import com.com.zealousfug.db1.Dept1;
import com.com.zealousfug.db1.Dept1Repository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

@Commit
@SpringBootTest
public class batch03ApplicationTests {

    @Autowired
    Dept1Repository dept1Repository;

    @Test
    public void first() {
        for (int i = 1; i < 101; i++) {
            Dept1 dept1 = new Dept1(i, String.valueOf(i), String.valueOf(i));
            dept1Repository.save(dept1);
        }
    }
}
