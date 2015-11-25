package org.openmrs.module.coreapps.fragment.controller.patientheader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.PersonService;

public class RegistrationDataHelper {	

	/**
	 * A simple class to hold a registration section's field details.
	 * To pass it to the view.
	 * @author Mekom Solutions
	 */
	public static class RegistrationFieldData {
		
		private String fieldValue;
		private String fieldLabel;

		public RegistrationFieldData(String fieldValue, String fieldLabel) {
			super();
			this.fieldValue = fieldValue;
			this.fieldLabel = fieldLabel;
		}
		
		public String getFieldValue() {
			return fieldValue;
		}
		
		public String getFieldLabel() {
			return fieldLabel;
		}
	}
	
	public static class RegistrationSectionData {
		
		private String label;
		private List<RegistrationFieldData> fields = new ArrayList<RegistrationFieldData>();
		
		public RegistrationSectionData(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public List<RegistrationFieldData> getFields() {
			return fields;
		}

		public void setFields(List<RegistrationFieldData> fields) {
			this.fields = fields;
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
	 * @return A model-ready map section ID-section data.
	 */
	public Map<String, RegistrationSectionData> getSectionsFromConfig(final ObjectNode config) {
		
		HashMap<String, RegistrationSectionData> res = new HashMap<String, RegistrationSectionData>();
		
		return res;
	}
}
