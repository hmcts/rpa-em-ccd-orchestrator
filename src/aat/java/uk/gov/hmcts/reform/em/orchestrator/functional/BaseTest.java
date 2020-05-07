package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.em.EmTestConfig;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

@SpringBootTest(classes = {TestUtil.class, EmTestConfig.class, ExtendedCcdHelper.class})
@PropertySource(value = "classpath:application.yml")
@RunWith(SpringIntegrationSerenityRunner.class)
public abstract class BaseTest {

    @Autowired
    protected TestUtil testUtil;

    @Autowired
    protected ExtendedCcdHelper extendedCcdHelper;

    ObjectMapper mapper = new ObjectMapper();

}
