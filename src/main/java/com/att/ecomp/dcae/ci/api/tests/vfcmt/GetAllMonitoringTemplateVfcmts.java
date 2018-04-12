package com.att.ecomp.dcae.ci.api.tests.vfcmt;

import com.att.ecomp.dcae.ci.api.tests.DcaeRestBaseTest;
import com.att.ecomp.dcae.ci.utilities.DcaeRestClient;
import com.att.ecomp.dcae.ci.utilities.StringUtils;
import com.aventstack.extentreports.Status;
import org.onap.sdc.dcae.composition.vfcmt.Vfcmt;
import org.openecomp.d2.ci.datatypes.http.RestResponse;
import org.openecomp.d2.ci.report.ExtentTestActions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GetAllMonitoringTemplateVfcmts extends DcaeRestBaseTest {

    private RestResponse response;
    private static final String BASE_MONITORING_TEMPLATE = "Base Monitoring Template";

    @BeforeClass
    public void executeApiCall() {
        try {
            // act
            ExtentTestActions.log(Status.INFO, "Fetching all monitoring templates vfcmt resources");
            response = DcaeRestClient.getAllMonitoringTemplatesVfcmts();
            ExtentTestActions.log(Status.DEBUG, "Response: " + StringUtils.truncate(response));
        } catch (Exception err) {
            ExtentTestActions.log(Status.ERROR, err);
        }
    }

    @Test
    public void testResponseStatusOk() {
        // assert
        ExtentTestActions.log(Status.INFO, "Verifying response status is 200");
        assertThat(response.getStatusCode()).as("response status").isEqualTo(200);
    }

    @Test
    public void testResponseBodyIsListOfVfcmtsOnly() {
        // assert
        ExtentTestActions.log(Status.INFO, "Parsing response to a list of monitoring template VFCMTs");
        Vfcmt[] vfcmtList = gson.fromJson(response.getResponse(), Vfcmt[].class);

        ExtentTestActions.log(Status.INFO, "Verifying we got items");
        assertThat(vfcmtList).isNotEmpty();

        ExtentTestActions.log(Status.INFO, "Verifying all items of the parsed response are VFCMTs");
        assertThat(vfcmtList)
                .as("response data")
                .extracting("resourceType")
                .containsOnly("VFCMT");

        ExtentTestActions.log(Status.INFO, "Verifying that all items of the parsed response are monitoring template VFCMTs");
        assertThat(vfcmtList)
                .as("response data")
                .extracting("subCategory")
                .containsOnly(BASE_MONITORING_TEMPLATE);
    }
}
