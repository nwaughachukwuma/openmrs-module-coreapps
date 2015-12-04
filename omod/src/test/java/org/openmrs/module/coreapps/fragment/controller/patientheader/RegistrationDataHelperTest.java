package org.openmrs.module.coreapps.fragment.controller.patientheader;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.RegistrationFieldData;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.RegistrationQuestionData;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.RegistrationSectionData;

public class RegistrationDataHelperTest {

	private Patient patient;
	private ObsService obsService;
	private PersonService personService;
	
	private RegistrationDataHelper dataHelper;
	private Log log;
	
	// Test helper: Resource as ObjectNode
	private ObjectNode getConfigFromResource(String resPath) throws IOException {
		String json = getResourceAsString(resPath); 
		ObjectNode regConfig = (ObjectNode) (new ObjectMapper()).readTree(json);		
		return regConfig;
	}
	
	// Test helper: Resource as String
	private String getResourceAsString(String resPath) throws IOException {
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(resPath);
		String str = IOUtils.toString(inStream, "UTF-8"); 
		return str;
	}
	
	@Before
	public void before() {
		
		dataHelper = new RegistrationDataHelper();
		log = spy(LogFactory.getLog(RegistrationDataHelper.class));
		dataHelper.setLog(log);
	}

	@Test
	public void should_unmarshallField() throws IOException {
		
		String json = getResourceAsString("field.json");
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		RegistrationFieldData field = mapper.readValue(json, new TypeReference<RegistrationFieldData>(){});
		
		assertEquals(field.getLabel(), "registrationapp.patient.address.question");
		assertEquals(field.getType(), "personAddress");
	}
	
	@Test
	public void should_unmarshallQuestion() throws IOException {
		
		String json = getResourceAsString("question.json");
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		RegistrationQuestionData question = mapper.readValue(json, new TypeReference<RegistrationQuestionData>(){});
		
		assertEquals(question.getFields().size(), 2);
		assertEquals(question.getFields().get(0).getLabel(), "My first label");
	}
	
	@Test
	public void should_unmarshallSection() throws IOException {
		
		String json = getResourceAsString("section.json");
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		RegistrationSectionData section = mapper.readValue(json, new TypeReference<RegistrationSectionData>(){});
		
		assertEquals(section.getQuestions().size(), 2);
	}
	
	@Test
	public void shouldLogNoSectionsFound() throws IOException {
		
		// This simulates appDescriptor.getConfig()
		ObjectNode config = getConfigFromResource("regapp_config_noSections.json");

		List<RegistrationSectionData> sections = dataHelper.getSectionsFromConfig(config);		
		
		verify(log, times(1)).error(anyString());
	}
	
	@Test
	public void shouldReturnSectionsData() throws IOException {
		
		// This simulates appDescriptor.getConfig()
		ObjectNode config = getConfigFromResource("regapp_config.json");

		List<RegistrationSectionData> sections = dataHelper.getSectionsFromConfig(config);		
		
		assertEquals(sections.size(), 4);
	}
}
