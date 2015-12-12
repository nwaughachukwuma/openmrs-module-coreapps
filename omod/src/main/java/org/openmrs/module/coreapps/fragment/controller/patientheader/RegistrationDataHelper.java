/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.coreapps.fragment.controller.patientheader;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.ObsService;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

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

		protected Log log = LogFactory.getLog(getClass());
		
		private Locale locale;
		private ConceptService conceptService;
		private ObsService obsService;
		private LocationService locationService;
		private PatientDomainWrapper patientWrapper;
		private Map<String, PersonAttribute> attrMap = new HashMap<String, PersonAttribute>();
		
		public DataContextWrapper(Locale locale, PatientDomainWrapper patientWrapper, ConceptService conceptService, ObsService obsService, LocationService locationService) {
			super();
			this.locale = locale;
			this.conceptService = conceptService;
			this.obsService = obsService;
			this.locationService = locationService;
			this.patientWrapper = patientWrapper;
			for(PersonAttribute attr : patientWrapper.getPatient().getActiveAttributes()) {
				attrMap.put(attr.getAttributeType().getUuid(), attr);
			}
		}
		
		protected void setLog(Log log) {
			this.log = log;
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
		
		public LocationService getLocationService() {
			return locationService;
		}
		
		public PatientDomainWrapper getPatientWrapper() {
			return patientWrapper;
		}
		
		public Map<String, PersonAttribute> getAttributes() {
			return attrMap;
		}
		
		/**
		 * @param formFieldName Typically a string such as "obs.CIEL:12345"
		 * @return null when no Concept could be found
		 */
		public Concept getConceptFromFormFieldName(String formFieldName) {
			
			Concept concept = null;
			
			String conceptSource = null;
			String conceptCode = null;
			
			String warnMsg = "A concept mapping could not be infered from \"formFieldName\":\"" + formFieldName + "\".";
			if (!StringUtils.isEmpty(formFieldName)) {
				
				String prefix = "obs.";
				if (formFieldName.startsWith(prefix)) {
					String mapping = formFieldName.substring(prefix.length());
					
					String[] split = mapping.split(":");
					if (split.length == 2) {
						conceptSource = split[0];
						conceptCode = split[1];
						concept = conceptService.getConceptByMapping(conceptCode, conceptSource);
					}
					else {
						log.warn(warnMsg);
					}
				}
				else {
					log.warn(warnMsg);
				}
			}
			
			return concept;
		}
	}
	
	/*
	 * For Jackson unmarshalling of registration app's questions's fields
	 */
	public static class RegistrationFieldData {
		
		protected Log log = LogFactory.getLog(getClass());
		
		protected static String ADDR_TMPL_NAMEMAPPINGS = "nameMappings";
		
		/*
		 * Atomic field label and field value pair.
		 */
		protected class Data {
			private String label = "";
			private String value = "";
			public Data(String label, String value) {
				super();
				this.label = label;
				this.value = value;
			}
			public String getLabel() {
				return label;
			}
			public String getValue() {
				return value;
			}
		}
		
		/*
		 * XStream converter for unmarshalling the Address Template XML
		 * 		into a Map<String, String> of what is inside <nameMappings/>. 
		 */
		protected static class AddressTemplateConverter implements Converter {

	        public boolean canConvert(Class clazz) {
	            return AbstractMap.class.isAssignableFrom(clazz);
	        }

	        @Override
			public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context) {}

	        @Override
	        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

	            Map<String, String> map = new LinkedHashMap<String, String>();	// LinkedHashMap to keep the Address Template XML order. 
	            reader.moveDown();	// Get down the nameMappings, sizeMappings... etc level

	            while(reader.hasMoreChildren()) {
	            	if(reader.getNodeName().equals(ADDR_TMPL_NAMEMAPPINGS)) {
	            		while(reader.hasMoreChildren()) {
	            			reader.moveDown();
	            			String key = reader.getAttribute("name");
	            			String value = reader.getAttribute("value");
	            			map.put(key, value);
	            			reader.moveUp();
	            		}
	            	}
	            }
	            return map;
	        }
	    }
		
		// Fields mapped by Jackson straight from the config JSON
		private String uuid;
		private String type;
		private String label;
		private String formFieldName;
		
		// Output fields
		private List<Data> data = new ArrayList<Data>();
		private boolean withObs = false;
		
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
		
		public List<Data> getData() {
			return data;
		}
		
		public boolean isWithObs() {
			return withObs;
		}

		public void setWithObs(boolean withObs) {
			this.withObs = withObs;
		}

		/*
		 * 'Output' getters
		 */
		public String getFieldLabel() {
			return (data.size() == 1) ? data.get(0).getLabel() : "";
		}
		
		public String getFieldValue() {
			return (data.size() == 1) ? data.get(0).getValue() : "";
		}
		
		/**
		 * Based on the field type, fetch the relevant data into {@link #fieldLabels} and {@link #fieldValues}
		 * @param dataContext Wrapper around possible needed data sources.
		 * @return
		 */
		public boolean fetchData(final DataContextWrapper dataContext) {
			boolean success = false;
			
			if(getType().equals("obs")) {
				setWithObs(true);
				success = fetchObsData(dataContext);
			}
			else if(getType().equals("personAttribute")) {
				success = fetchPersonAttrData(dataContext);
			}
			else if(getType().equals("personAddress")) {
				success = fetchAddressData(dataContext);
			}
			else {
				log.error("There is no implementation to handle fields of type: " + getType() + " for field labelled: " + getLabel());
            	success = false;
			}
			return success;
		}
		
		protected boolean fetchObsData(final DataContextWrapper dataContext) {

			Concept conceptQuestion = dataContext.getConceptFromFormFieldName(formFieldName);
			if(conceptQuestion == null) {
				log.error("The concept-question could not be fetched for field labelled: " + getLabel() + ", this was the \"obs\"'s \"formFieldName\" provided: " + formFieldName);
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
					data.add(new Data(question, answer));
					return true;
				}
				else {
					log.error("The concept coded answer could not be fetched for field labelled: " + getLabel() + ", this was the \"obs\"'s \"formFieldName\" provided: " + formFieldName);
					return false;
				}
			}
			else if (dataType.equals("Text")) {
				// TODO: To be implemented
				return false;
			}
			else {
				return false;
			}
		}
		
		protected boolean fetchPersonAttrData(final DataContextWrapper dataContext) {
			
			PersonAttribute attr = dataContext.getAttributes().get(getUuid());
			if(attr != null) {
				data.add(new Data(attr.getAttributeType().getName(), attr.getValue())); // TODO: Check i18n on PersonAttributeType's name
				return true;
			}
			else {
				log.error("The registration PersonAttribute could not be fetched for field labelled: " + getLabel() + ".");
				data.add(new Data(getLabel(), ""));
				return false;
			}
		}
		
		protected Map<String, String> getAddressTemplateNameMappings(final LocationService locationService) {
			
			XStream xstream = new XStream();
			xstream.alias("org.openmrs.layout.web.address.AddressTemplate", java.util.Map.class);
			xstream.registerConverter(new AddressTemplateConverter());
			
			String addressTemplateXml = locationService.getAddressTemplate();
			@SuppressWarnings("unchecked")
			Map<String, String> nameMappings = (Map<String, String>) xstream.fromXML(addressTemplateXml);
			
			return nameMappings;
		}
		
		protected boolean fetchAddressData(final DataContextWrapper dataContext) {
			
			Patient patient = dataContext.getPatientWrapper().getPatient();
			PersonAddress address = patient.getPersonAddress();
			
			Map<String, String> nameMappings = getAddressTemplateNameMappings(dataContext.getLocationService());	// The Address Template list of name mappings is used as the ref.
			if(CollectionUtils.isEmpty(nameMappings.keySet())) {
				log.error("The name mappings could not be retrieved when reading the node <" + ADDR_TMPL_NAMEMAPPINGS + "> of the Address Template XML.");
				return false;
			}
			
			Map<String, String> addressAsMap = null;
			try {
				addressAsMap = BeanUtils.describe(address);
			} catch (Exception e) {
				log.error("There was an error exploring the PersonAddress bean for patient id: " + patient.getId() + ", no address information will be displayed on the patient header.", e);
				return false;
			}

			for(String addressLevelName : nameMappings.keySet()) {
				String addressLevelValue = addressAsMap.get(addressLevelName);
				if(StringUtils.isEmpty(addressLevelValue)) {
					log.error("The address level '" + addressLevelName + "' was not found in the PersonAddress for patient id: " + patient.getId() + ", this level will be missing on the patient header.");
					addressLevelValue = "";
				}
				data.add(new Data(nameMappings.get(addressLevelName), addressLevelValue));
			}
			
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
		
		protected Log log = LogFactory.getLog(getClass());
		
		// Fields mapped by Jackson straight from the config JSON
		private String id;
		private String label;
		private List<RegistrationQuestionData> questions;
		private String onPatientHeader = "left";
		
		// Other
		private Extension linkExtension = new Extension();
		private boolean withObs = false;
		
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
		
		public String getOnPatientHeader() {
			return onPatientHeader;
		}

		public void setOnPatientHeader(String onPatientHeader) {
			this.onPatientHeader = onPatientHeader;
		}

		public Extension getLinkExtension() {
			return linkExtension;
		}

		public void setLinkExtension(Extension linkExtension) {
			this.linkExtension = linkExtension;
		}
		
		public boolean isWithObs() {
			return withObs;
		}

		public void setWithObs(boolean withObs) {
			this.withObs = withObs;
		}

		public boolean fetchData(final DataContextWrapper dataContext) {
			
			if (CollectionUtils.isEmpty(questions)) {
				return false;
			}
			
			for (RegistrationQuestionData question : questions) {
				if(CollectionUtils.isEmpty(question.getFields())) {
					return false;
				}
				for (RegistrationFieldData field : question.getFields()) {
					field.fetchData(dataContext);
					if(field.isWithObs()) {
						setWithObs(true);
					}
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