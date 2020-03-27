package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.AsyncCcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@AutoConfigureMockMvc
public class CcdStitchBundleCallbackControllerTest extends BaseTest {

    @MockBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @Test
    public void shouldCallCcdCallbackHandlerService() throws Exception {

        mockMvc
                .perform(post("/api/stitch-ccd-bundles")
                        .content("[]")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        Mockito
                .verify(defaultUpdateCaller, Mockito.times(1))
                .executeUpdate(Mockito.any(CcdBundleStitchingService.class), Mockito.any(HttpServletRequest.class));
    }

    @Test
    public void shouldCallCcdCallbackHandlerServiceAsync() throws Exception {

        mockMvc
                .perform(post("/api/async-stitch-ccd-bundles")
                        .content("[]")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        Mockito
                .verify(defaultUpdateCaller, Mockito.times(1))
                .executeUpdate(Mockito.any(AsyncCcdBundleStitchingService.class), Mockito.any(HttpServletRequest.class));
    }

}
