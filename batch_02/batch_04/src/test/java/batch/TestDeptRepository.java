package batch;


import com.com.jeoluspug.domain.Dept;
import com.com.jeoluspug.domain.DeptRepository;
import com.com.jeoluspug.parallelBatchApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Commit
@SpringBootTest(classes = parallelBatchApplication.class)
public class TestDeptRepository {

    @Autowired
    DeptRepository deptRepository;

    @Test
    @Transactional
    void deptSave() {
        List<Dept> deptList = new ArrayList<Dept>();
        for(int i = 0; i<10000; i++){
            deptList.add(new Dept(i,String.valueOf(i), String.valueOf(i)));
        }
        deptRepository.saveAll(deptList);
    }

    @Test
    @Transactional
    void deptDel(){
        deptRepository.deleteAll();
    }
}
