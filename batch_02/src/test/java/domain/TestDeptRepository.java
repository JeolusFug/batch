package domain;

import com.com.jealouspug.domain.Dept;
import com.com.jealouspug.domain.DeptRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBatchTest
public class TestDeptRepository {

    @Autowired
    DeptRepository deptRepository;

    @Test
    @Commit
    public void dept01() {
        for (int i = 1; i<101; i++) {
            deptRepository.save(new Dept(i,"dName_" + String.valueOf(i), "loc_" + String.valueOf(i)));
        }
    }
}
