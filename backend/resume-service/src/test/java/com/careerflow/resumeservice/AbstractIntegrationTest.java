package com.careerflow.resumeservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestJwtDecoderConfig.class)
public abstract class AbstractIntegrationTest {
}
