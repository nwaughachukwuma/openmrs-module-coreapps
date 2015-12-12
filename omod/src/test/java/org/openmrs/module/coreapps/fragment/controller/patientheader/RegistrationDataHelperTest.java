package org.openmrs.module.coreapps.fragment.controller.patientheader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.DataContextWrapper;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.RegistrationFieldData;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.RegistrationFieldData.Data;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.RegistrationQuestionData;
import org.openmrs.module.coreapps.fragment.controller.patientheader.RegistrationDataHelper.RegistrationSectionData;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;

public class RegistrationDataHelperTest {

	private RegistrationDataHelper dataHelper;
	private Log log;
	
	private DataContextWrapper dataContext;
	private ConceptService conceptService;
	private LocationService locationService;
	private PatientDomainWrapper patientWrapper;
	private Patient patient;
	
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
	public void before() throws APIException, IOException {
		
		dataHelper = new RegistrationDataHelper();
		conceptService = mock(ConceptService.class);
		locationService = mock(LocationService.class);
		when(locationService.getAddressTemplate()).thenReturn( getResourceAsString("addressTemplate.xml") );
		patientWrapper = mock(PatientDomainWrapper.class);
		patient = mock(Patient.class);
		when(patientWrapper.getPatient()).thenReturn(patient);
		dataContext = new DataContextWrapper(null, patientWrapper, conceptService, null, locationService);
		log = spy(LogFactory.getLog(getClass()));
		dataHelper.setLog(log);
	}

	@Test
	public void should_returnConceptFromFormFieldName() {

		String code = "12345";
		String source = "CIEL";
		String fieldName = "obs." + source + ":" + code;
		when(conceptService.getConceptByMapping(eq(code), eq(source))).thenReturn(mock(Concept.class));

		Concept concept = dataContext.getConceptFromFormFieldName(fieldName);
		
		assertFalse(concept == null);
	}
	
	@Test
	public void should_warnOnFormFieldNameBadlyPrefixed() {

		String fieldName = "foo:bad.guy";

		dataContext.setLog(log);
		Concept concept = dataContext.getConceptFromFormFieldName(fieldName);
		
		verify(log, times(1)).warn(anyString());
		assertTrue(concept == null);
	}

	@Test
	public void should_warnOnFormFieldNameWithBadMapping() {

		String fieldName = "obs.CIEL_12345";

		dataContext.setLog(log);
		Concept concept = dataContext.getConceptFromFormFieldName(fieldName);
		
		verify(log, times(1)).warn(anyString());
		assertTrue(concept == null);
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
	public void should_logNoSectionsFound() throws IOException {
		
		// This simulates appDescriptor.getConfig()
		ObjectNode config = getConfigFromResource("regapp_config_noSections.json");

		@SuppressWarnings("unused")
		List<RegistrationSectionData> sections = dataHelper.getSectionsFromConfig(config);		
		
		verify(log, times(1)).error(anyString());
	}
	
	@Test
	public void should_returnSectionsData() throws IOException {
		
		// This simulates appDescriptor.getConfig()
		ObjectNode config = getConfigFromResource("regapp_config.json");

		List<RegistrationSectionData> sections = dataHelper.getSectionsFromConfig(config);		
		
		assertEquals(sections.size(), 4);
	}
	
	@Test
	public void should_getAddressTemplateNameMappings() {
		
		RegistrationFieldData field = new RegistrationFieldData(); 
		
		Map<String, String> nameMappings = field.getAddressTemplateNameMappings(locationService);
		
		assertEquals(nameMappings.get("countyDistrict"), "Location.district");
		assertEquals(nameMappings.get("address1"), "Location.address1");
		assertEquals(nameMappings.get("country"), "Location.country");
		assertEquals(nameMappings.get("stateProvince"), "Location.stateProvince");
		assertEquals(nameMappings.get("cityVillage"), "Location.cityVillage");
	}
	
	@Test
	public void should_getAddressDataInOrder() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		String address1 = "Main Street, 1";
		String cityVillage = "Towney";
		String countyDistrict = "The County";
		String stateProvince = "Sunshine State";
		String country = "Neverland";
		
		PersonAddress address = new PersonAddress();
		address.setAddress1(address1);
		address.setCityVillage(cityVillage);
		address.setCountyDistrict(countyDistrict);
		address.setStateProvince(stateProvince);
		address.setCountry(country);

		when(patient.getPersonAddress()).thenReturn(address);
		
		RegistrationFieldData field = new RegistrationFieldData();
		field.fetchAddressData(dataContext);
		
		List<Data> addressData = field.getData();
		
		assertEquals(addressData.size(), 5);
		assertEquals(addressData.get(0).getLabel(), "Location.district");
		assertEquals(addressData.get(0).getValue(), countyDistrict);
		assertEquals(addressData.get(1).getLabel(), "Location.address1");
		assertEquals(addressData.get(1).getValue(), address1);
		assertEquals(addressData.get(2).getLabel(), "Location.country");
		assertEquals(addressData.get(2).getValue(), country);
		assertEquals(addressData.get(3).getLabel(), "Location.stateProvince");
		assertEquals(addressData.get(3).getValue(), stateProvince);
		assertEquals(addressData.get(4).getLabel(), "Location.cityVillage");
		assertEquals(addressData.get(4).getValue(), cityVillage);
	}
	
}
