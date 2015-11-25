package org.openmrs.module.coreapps.fragment.controller.patientheader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.RegistrationSectionData;

public class RegistrationDataHelperTest {

	private Patient patient;
	private ObsService obsService;
	private PersonService personService;
	
	private RegistrationDataHelper dataHelper;
	
	@Before
	public void before() throws IOException {
		
		dataHelper = new RegistrationDataHelper(patient, obsService, personService);
	}

	@Test
	public void shouldReturnSectionData() throws IOException {
		
		ObjectNode config = getConfigFromResource("registration_app_config.json");
		
		Map<String, RegistrationSectionData> sections = dataHelper.getSectionsFromConfig(config);
	}
	
	// Resource helper
	private ObjectNode getConfigFromResource(String resPath) throws IOException {
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(resPath);
		String json = IOUtils.toString(inStream, "UTF-8"); 
		
		ObjectNode regConfig = (ObjectNode) (new ObjectMapper()).readTree(json);		
		return regConfig;
	}
}
