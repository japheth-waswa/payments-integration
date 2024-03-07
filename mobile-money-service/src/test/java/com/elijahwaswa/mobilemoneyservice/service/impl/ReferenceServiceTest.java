package com.elijahwaswa.mobilemoneyservice.service.impl;

import com.elijahwaswa.basedomains.utils.Helpers;
import com.elijahwaswa.mobilemoneyservice.entity.Reference;
import com.elijahwaswa.mobilemoneyservice.service.reference.ReferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ReferenceServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReferenceService referenceService;

    @Value("${reference.default}")
    private String defaultReference;


    private void truncateH2DB() {
        //truncate or delete data from all tables
        String[] tables = {"references"};
        for (String table : tables) {
            String sql = "TRUNCATE TABLE " + table;
            jdbcTemplate.execute(sql);
        }
    }

    @BeforeEach
    void setup() {
        //truncate
        truncateH2DB();
    }

    @Test
    void generateReference() {
        Reference reference1 = referenceService.generateReference();
        System.out.println(reference1);
        assertEquals(defaultReference, reference1.getReference());

        Reference reference2 = referenceService.generateReference();
        System.out.println(reference2);
        String increment1 = Helpers.incrementString(defaultReference);
        assertEquals(increment1, reference2.getReference());

        Reference reference3 = referenceService.generateReference();
        System.out.println(reference3);
        String increment2 = Helpers.incrementString(increment1);
        assertEquals(increment2, reference3.getReference());
    }

//    @Test
//    void generateReference_002() {
//        Reference reference1=referenceService.generateReference();
//        System.out.println(reference1);
//        assertEquals(defaultReference,reference1.getReference());
//
//        Reference reference2=referenceService.generateReference();
//        System.out.println(reference2);
//        String increment1  = Helpers.incrementString(defaultReference);
//        assertEquals(increment1,reference2.getReference());
//
//        Reference reference3=referenceService.generateReference();
//        System.out.println(reference3);
//        String increment2  = Helpers.incrementString(increment1);
//        assertEquals(increment2,reference3.getReference());
//    }

//    @Test
//    void generateReference_multi_threading() throws InterruptedException, ExecutionException {
//       int numberOfThreads =1; //todo change with 5 here to see the error
//        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
//        CompletionService<Reference> completionService = new ExecutorCompletionService<>(executorService);
//
//        //submit tasks for generating references concurrently
//        List<Future<Reference>> futures =new ArrayList<>();
//        for(int i =0;i<numberOfThreads;i++){
//            futures.add(completionService.submit((()->referenceService.generateReference())));
//        }
//
//        //wait for all tasks to complete
//        executorService.shutdown();
//       boolean terminatedSuccessfully = executorService.awaitTermination(10, TimeUnit.SECONDS);
//        executorService.close();
//        System.out.println(terminatedSuccessfully);
//
//        //validate generated references
//        for(Future<Reference> future:futures){
//            Reference reference = future.get();
//            System.out.println("-".repeat(50));
//            System.out.println(reference);
//        }
//    }
}