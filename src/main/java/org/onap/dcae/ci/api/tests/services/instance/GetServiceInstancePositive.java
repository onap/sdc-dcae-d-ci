/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.dcae.ci.api.tests.services.instance;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.assertj.core.api.SoftAssertions;
import org.onap.dcae.ci.api.tests.DcaeRestBaseTest;
import org.onap.dcae.ci.entities.RestResponse;
import org.onap.dcae.ci.utilities.DcaeRestClient;
import org.onap.dcae.ci.report.Report;
import org.onap.dcae.ci.utilities.StringUtils;
import org.onap.sdc.dcae.composition.restmodels.sdc.ServiceDetailed;
import org.onap.sdc.dcae.composition.services.Resource;
import org.onap.sdc.dcae.composition.vfcmt.Vfcmt;
import org.onap.sdc.dcae.composition.restmodels.DcaeMinimizedService;
import org.springframework.util.CollectionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class GetServiceInstancePositive extends DcaeRestBaseTest {
	
	private RestResponse response;
	
	@BeforeClass
	public void executeApiCall() throws Exception {
		// arrange
		DcaeMinimizedService service = arrangeService();
		try {
			// act
			Report.log(Status.INFO, "Get all VFIs for service [" + service.getUuid() + "]");
			response = DcaeRestClient.getServicesInstance(service.getUuid());
			Report.log(Status.DEBUG, "Response: " + StringUtils.truncate(response));
		} catch (Exception err) {
			Report.log(Status.FAIL, "Unable to execute api call: " + err.toString());
			err.printStackTrace();
		}
	}

	private DcaeMinimizedService arrangeService() throws Exception {
		DcaeMinimizedService service = null;
		try {
			Predicate<DcaeMinimizedService> hasVfi = p -> !CollectionUtils.isEmpty(getService(p.getUuid()).getResources());
			Vfcmt vfcmt = client.createCheckedoutVfcmt();
			Report.log(Status.INFO, "Created vfcmt [" + vfcmt.getUuid() + "]");
			Report.log(Status.INFO, "Get all services for vfcmt [" + vfcmt.getUuid() + "]");
			RestResponse responseServices =  DcaeRestClient.getServices(vfcmt.getUuid(), vfcmt.getLastUpdaterUserId());
			Report.log(Status.DEBUG, "Response: " + StringUtils.truncate(responseServices));
			DcaeMinimizedService[] servicesList = gson.fromJson(responseServices.getResponse(), DcaeMinimizedService[].class);
			// TODO: create a service instead of picking a random one
			// find a service with a vfi
			service = Arrays.stream(servicesList).filter(hasVfi).findAny().orElse(null);
		} catch (Exception err) {
			Report.log(Status.ERROR, "Could not arrange test: " + err.toString());
		}
		return service;
	}

	private ServiceDetailed getService(String serviceId) {
		ServiceDetailed service = null;
		try {
			service = gson.fromJson(DcaeRestClient.getServicesInstance(serviceId).getResponse(), ServiceDetailed.class);
		} catch (Exception e) {
			Report.log(Status.ERROR, "Could not arrange test: " + e.toString());
		}
		return service;
	}
	
	@Test
	public void test_responseStatusOk() throws IOException{
		// assert
		Report.log(Status.INFO, "Verifing response status is 200");
		assertThat(response.getStatusCode()).as("response status").isEqualTo(200);
	}
	
	@Test 
	public void test_atLeastOneOrMoreResources() throws IOException{
		// assert
		Report.log(Status.INFO, "Parsing response to a one service instance");
		List<Resource> resourceList = getResourceListFromJsonResponse();
		Report.log(Status.INFO, "validating parsed response structure");
		assertThat(resourceList).size().isGreaterThanOrEqualTo(1); // TODO: create a VFI for the service instead of picking a random one
	}
	
	@Test 
	public void  test_responseIsValidStructure() throws IOException{
		// assert
		Report.log(Status.INFO, "Parsing response to a one service instance");
		
		List<Resource> resourceList = getResourceListFromJsonResponse();

		Report.log(Status.INFO, "validating parsed response structure");
		
		SoftAssertions.assertSoftly(softly -> {
			softly.assertThat(resourceList.get(0).getResourceInstanceName()).isNotEmpty();
			softly.assertThat(resourceList.get(0).getResourceInvariantUUID()).isNotEmpty();
		});
	}
	
	
	/*** private method ***/
	private List<Resource> getResourceListFromJsonResponse() {
		JsonParser jsonParser = new JsonParser();
		JsonObject responseJson = (JsonObject)jsonParser.parse(response.getResponse());
		JsonArray resources = responseJson.getAsJsonArray("resources");
		Type listType = new TypeToken<List<Resource>>(){}.getType();
		List<Resource> resourceList = gson.fromJson(resources, listType);
		return resourceList;
	}

}
