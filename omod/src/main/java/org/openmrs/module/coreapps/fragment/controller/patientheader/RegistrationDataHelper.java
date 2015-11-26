package org.openmrs.module.coreapps.fragment.controller.patientheader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;

public class RegistrationDataHelper {	

	protected Log log = LogFactory.getLog(getClass());
	
	protected void setLog(Log log) {
		this.log = log;
	}
	
	/*
	 * For Jackson unmarshalling of registration app's questions's fields
	 */
	public static class RegistrationFieldData {
		
		private String type;
		private String label;
		private String formFieldName;
		
		public RegistrationFieldData() {
			super();
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getFormFieldName() {
			return formFieldName;
		}

		public void setFormFieldName(String formFieldName) {
			this.formFieldName = formFieldName;
		}
	}
		
	/*
	 * For Jackson unmarshalling of registration app's sections's questions
	 */
	public static class RegistrationQuestionData {
		
		private String legend;
		private List<RegistrationFieldData> fields;

		public RegistrationQuestionData() {
			super();
		}

		public String getLegend() {
			return legend;
		}

		public void setLegend(String legend) {
			this.legend = legend;
		}

		public List<RegistrationFieldData> getFields() {
			return fields;
		}

		public void setFields(List<RegistrationFieldData> fields) {
			this.fields = fields;
		}
	}
	
	/*
	 * For Jackson unmarshalling of registration app's config's sections
	 */
	public static class RegistrationSectionData {
		
		private String id;
		private String label;
		private List<RegistrationQuestionData> questions;
		
		public RegistrationSectionData() {
			super();
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public List<RegistrationQuestionData> getQuestions() {
			return questions;
		}

		public void setQuestions(List<RegistrationQuestionData> questions) {
			this.questions = questions;
		}
	}

	protected Patient patient;
	protected ObsService obsService;
	protected PersonService personService;
	
	public RegistrationDataHelper(Patient patient, ObsService obsService, PersonService personService) {
		super();
		this.patient = patient;
		this.obsService = obsService;
		this.personService = personService;
	}
	
	/**
	 * Retrieves the Registration App's data following the sections & fields app's structure.
	 * @param config The Registration App's config.
	 * @return A model-ready list of sections data.
	 */
	public List<RegistrationSectionData> getSectionsFromConfig(final ObjectNode config) {

		final String sectionsNodeId = "sections";
		
		List<RegistrationSectionData> sections = new ArrayList<RegistrationSectionData>();
		
		String json = "{}";
		JsonNode node = config.get(sectionsNodeId);
		if(node != null) {
			json = node.toString();
		}
		else {
			log.error("The node id '" + sectionsNodeId + "' could not be found in the registrationapp's JSON config.");
			return sections;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			sections = mapper.readValue(json, new TypeReference<List<RegistrationSectionData>>(){});
		} catch (Exception e) {
			log.error("The the registrationapp's JSON config could not be deserialized:\n" + json);
		} 
		
		return sections;
	}
}
