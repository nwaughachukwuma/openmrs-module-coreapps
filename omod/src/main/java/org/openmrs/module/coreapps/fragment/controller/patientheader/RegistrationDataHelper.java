package org.openmrs.module.coreapps.fragment.controller.patientheader;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;

/**
 * Container classes and data sources to help gather and store all the registration data.
 * @author Mekom Solutions
 */
public class RegistrationDataHelper {	

	protected Log log = LogFactory.getLog(getClass());
	
	protected void setLog(Log log) {
		this.log = log;
	}
	
	/*
	 * Wrapper around the possible data sources needed to fetch a field's data
	 */
	public static class DataSourcesWrapper {
		
		private ConceptService conceptService;
		private ObsService obsService;
		private PatientDomainWrapper patientWrapper;
		
		public DataSourcesWrapper(PatientDomainWrapper patientWrapper, ConceptService conceptService, ObsService obsService) {
			super();
			this.conceptService = conceptService;
			this.obsService = obsService;
			this.patientWrapper = patientWrapper;
		}

		public ConceptService getConceptService() {
			return conceptService;
		}

		public ObsService getObsService() {
			return obsService;
		}

		public PatientDomainWrapper getPatientWrapper() {
			return patientWrapper;
		}
	}
	
	/*
	 * For Jackson unmarshalling of registration app's questions's fields
	 */
	public static class RegistrationFieldData {
		
		// Fields mapped by Jackson straight from the config JSON
		private String type;
		private String label;
		private String formFieldName;
		
		// Output fields
		private List<String> fieldLabels = new ArrayList<String>();
		private List<String> fieldValues = new ArrayList<String>();
		
		public RegistrationFieldData() {
			super();
		}

		public String getType() {
			return (type == null) ? "" : type;
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
		
		public List<String> getFieldLabels() {
			return fieldLabels;
		}

		public List<String> getFieldValues() {
			return fieldValues;
		}

		/*
		 * 'Output' getters
		 */
		public String getFieldLabel() {
			return (fieldLabels.size() == 1) ? fieldLabels.get(0) : "";
		}
		
		public String getFieldValue() {
			return (fieldValues.size() == 1) ? fieldValues.get(0) : "";
		}
		
		/**
		 * Based on the field type, fetch the relevant data into {@link #fieldLabels} and {@link #fieldValues}
		 * @param dataSources Wrapper around possible needed data sources.
		 * @return
		 */
		public boolean fetchData(final DataSourcesWrapper dataSources) {
			boolean success = false;
	        switch(getType()) {
	            case "obs":
	            	success = fetchObsData(dataSources);
	            case "personAttribute":
	            	success = fetchPersonAttrData(dataSources);
	            case "personAddress":
	            	success = fetchAddressData(dataSources);
	            default:
	            	// TODO Log this somehow
	            	success = false;
	        }
			return success;
		}
		
		protected boolean fetchObsData(final DataSourcesWrapper dataSources) {
			return false;
		}
		
		protected boolean fetchPersonAttrData(final DataSourcesWrapper dataSources) {
			return false;
		}
		
		protected boolean fetchAddressData(final DataSourcesWrapper dataSources) {
			return false;
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
		
		public boolean fetchData(DataSourcesWrapper dataSources) {
			
			if(CollectionUtils.isEmpty(questions)) {
				return false;
			}
			
			for(RegistrationQuestionData question : questions) {
				if(CollectionUtils.isEmpty(question.getFields())) {
					return false;
				}
				
				boolean fieldFetched = false;
				for(RegistrationFieldData field : question.getFields()) {
					fieldFetched = field.fetchData(dataSources);
					if(fieldFetched == false)
						// TODO Log this somehow
						return false;
				}
			}
			
			return true;
		}
	}
	
	/*
	 * 
	 */
	
	public RegistrationDataHelper() {
		super();
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
