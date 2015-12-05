package org.openmrs.module.coreapps.fragment.controller.patientheader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.UiUtils;

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
	public static class DataContextWrapper {
		
		private Locale locale;
		private UiUtils uiUtils;
		private ConceptService conceptService;
		private ObsService obsService;
		private PatientDomainWrapper patientWrapper;
		private Map<String, PersonAttribute> attrMap = new HashMap<String, PersonAttribute>();
		
		public DataContextWrapper(Locale locale, UiUtils uiUtils, PatientDomainWrapper patientWrapper, ConceptService conceptService, ObsService obsService) {
			super();
			this.locale = locale;
			this.uiUtils = uiUtils;
			this.conceptService = conceptService;
			this.obsService = obsService;
			this.patientWrapper = patientWrapper;
			for(PersonAttribute attr : patientWrapper.getPatient().getAttributes()) {
				attrMap.put(attr.getAttributeType().getUuid(), attr);
			}
		}

		public UiUtils getUiUtils() {
			return uiUtils;
		}

		public Map<String, PersonAttribute> getAttrMap() {
			return attrMap;
		}

		public Locale getLocale() {
			return locale;
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
		
		public Map<String, PersonAttribute> getAttributes() {
			return attrMap;
		}
		
		/**
		 * @param formFieldName Typically a string such as "obs.CIEL.12345"
		 * @return null when no Concept could be found
		 */
		public Concept getConceptFromFormFieldName(String formFieldName) {
			
			String conceptSource = null;
			if(formFieldName != null) {
				conceptSource=formFieldName.substring( formFieldName.indexOf('.') + 1, formFieldName.indexOf(':') );
			}
		
			String conceptCode = null;
			if(formFieldName != null) {
				conceptCode=formFieldName.substring( formFieldName.indexOf(':') + 1 );
			}
			
			Concept concept = conceptService.getConceptByMapping(conceptCode, conceptSource);
			return concept;
		}
	}
	
	/*
	 * For Jackson unmarshalling of registration app's questions's fields
	 */
	public static class RegistrationFieldData {
		
		// Fields mapped by Jackson straight from the config JSON
		private String uuid;
		private String type;
		private String label;
		private String formFieldName;
		
		// Output fields
		private List<String> fieldLabels = new ArrayList<String>();
		private List<String> fieldValues = new ArrayList<String>();
		
		public RegistrationFieldData() {
			super();
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
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
		 * @param dataContext Wrapper around possible needed data sources.
		 * @return
		 */
		public boolean fetchData(final DataContextWrapper dataContext) {
			setLabel(dataContext.getUiUtils().message(getLabel())); // i18n the field's label
			
			boolean success = false;
			
			if(getType().equals("obs")) {
				success = fetchObsData(dataContext);
			}
			else if(getType().equals("personAttribute")) {
				success = fetchPersonAttrData(dataContext);
			}
			else if(getType().equals("personAddress")) {
				success = fetchAddressData(dataContext);
			}
			else {
				// TODO Log this somehow
            	success = false;
			}
			return success;
		}
		
		protected boolean fetchObsData(final DataContextWrapper dataContext) {

			Concept conceptQuestion = dataContext.getConceptFromFormFieldName(formFieldName);
			if(conceptQuestion == null) {
				// TODO: Log this properly.
				return false;
			}
			
			String dataType = conceptQuestion.getDatatype().getName(); 
			
			if (dataType.equals("Coded")) {
				List<Obs> obsList = dataContext.getObsService().getObservationsByPersonAndConcept( dataContext.getPatientWrapper().getPatient(), conceptQuestion );
				Obs obs = obsList.get(0);
				Concept conceptAnswer = obs.getValueCoded();
				String question = conceptQuestion.getName(dataContext.getLocale()).toString();
				String answer = conceptAnswer.getName(dataContext.getLocale()).toString();
				if(answer != null) {
					fieldLabels.add(WordUtils.capitalize(question.toLowerCase()));
					fieldValues.add(WordUtils.capitalize(answer.toLowerCase()));
					return true;
				}
				else {
					// TODO: Log this properly
					return false;
				}
			}
			else if (dataType.equals("Text")) {
				// TODO: Shehzad to implement
				return false;
			}
			else {
				return false;
			}
		}
		
		protected boolean fetchPersonAttrData(final DataContextWrapper dataContext) {
			
			PersonAttribute attr = dataContext.getAttributes().get(getUuid());
			if(attr != null) {
				fieldLabels.add(WordUtils.capitalize( attr.getAttributeType().getName() ));	// TODO: Check i18n on PersonAttributeType's name
				fieldValues.add(WordUtils.capitalize(attr.getValue()));
				return true;
			}
			// TODO Log this somehow: reg app attribute not found in patient 'X'
			return false;
		}
		
		/*
		 * TODO: Fetch the needed fields only based on the address mapping configuration
		 */
		protected boolean fetchAddressData(final DataContextWrapper dataContext) {
			
			PersonAddress address = dataContext.getPatientWrapper().getPatient().getPersonAddress();
			
			if (address.getCityVillage() != null) {
				fieldLabels.add("Village");	// TODO I18N
				fieldValues.add(WordUtils.capitalize(address.getCityVillage()));
			}
			if (address.getCountry() != null) {
				fieldLabels.add("Country");	// TODO I18N
				fieldValues.add(WordUtils.capitalize(address.getCountry()));
			}
			if (address.getAddress1() != null) {
				fieldLabels.add("Address 1");	// TODO I18N
				fieldValues.add(WordUtils.capitalize(address.getAddress1()));
			}
			if (address.getAddress2() != null) {
				fieldLabels.add("Address 2");	// TODO I18N
				fieldValues.add(WordUtils.capitalize(address.getAddress2()));
			}
			if (address.getStateProvince() != null) {
				fieldLabels.add("Province");	// TODO I18N
				fieldValues.add(WordUtils.capitalize(address.getStateProvince()));
			}
			
			if (fieldValues.isEmpty()) {
				// TODO: Log this somehow, no relevant address fields found for patient 'X'
				return false;
			}
			else
				return true;
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
		
		public boolean fetchData(final DataContextWrapper dataContext) {
			
			setLabel( dataContext.getUiUtils().message(getLabel()) ); // i18n the section
			
			if(CollectionUtils.isEmpty(questions)) {
				return false;
			}
			
			for(RegistrationQuestionData question : questions) {
				if(CollectionUtils.isEmpty(question.getFields())) {
					return false;
				}
				
				question.setLegend( dataContext.getUiUtils().message(question.getLegend()) ); // i18n the questions
				
				boolean fieldFetched = false;
				for(RegistrationFieldData field : question.getFields()) {
					fieldFetched = field.fetchData(dataContext);
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