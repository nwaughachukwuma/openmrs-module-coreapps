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
package org.openmrs.module.coreapps.fragment.controller;

import java.util.List;
import org.jfree.util.Log;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * Ideally you pass in a PatientDomainWrapper as the "patient" config parameter. But if you pass in
 * a Patient, then this controller will wrap that for you.
 */
public class LatestWeightFragmentController {
	
	public void controller(FragmentConfiguration config, 
	                       @InjectBeans PatientDomainWrapper wrapper,
	                       @SpringBean("conceptService") ConceptService conceptService,
	                       @SpringBean("obsService")	  ObsService obsService,
	                       @RequestParam("patientId") Patient patient
	                       ) {
		
		if (patient instanceof Patient) {
			wrapper.setPatient((Patient) patient);
		} 
		Concept conceptWeight = conceptService.getConceptByMapping("5089", "CIEL");
		/*
		Concept conceptHeight = conceptService.getConceptByMapping("xxxx", "CIEL");
		Concept conceptTemperature = conceptService.getConceptByMapping("xxxx", "CIEL");
		Concept conceptBMI = conceptService.getConceptByMapping("xxxx", "CIEL");
		Concept conceptBloodOxygen = conceptService.getConceptByMapping("xxxx", "CIEL");
		Concept conceptPulse = conceptService.getConceptByMapping("xxxx", "CIEL");
		Concept conceptBloodPressure = conceptService.getConceptByMapping("xxxx", "CIEL");
		Concept conceptRespiratoryRate = conceptService.getConceptByMapping("xxxx", "CIEL");
		*/

		if(wrapper.getPatient()==null)
			Log.warn("PATIENT Object is NULL");
		/*if(conceptWeight==null)
			Log.warn("Weight Concept is NULL");
		if(conceptHeight==null)
			Log.warn("Height Concept is NULL");
		if(conceptTemperature==null)
			Log.warn("Temperature Concept is NULL");
		if(conceptBMI==null)
			Log.warn("BMI Concept is NULL");
		if(conceptBloodOxygen==null)
			Log.warn("Blood Oxygen Concept is NULL");
		if(conceptPulse==null)
			Log.warn("Pulse Concept is NULL");
		if(conceptBloodPressure==null)
			Log.warn("Blood Pressure Concept is NULL");*/
			
        config.addAttribute("patientWeight", getWeight(obsService,wrapper.getPatient(),conceptWeight));
        
        /*
         
        config.addAttribute("patientTemperature", getTemperature(obsService,wrapper.getPatient(),conceptTemperature));
        config.addAttribute("patientBMI", getBMI(obsService,wrapper.getPatient(),conceptBMI));
        config.addAttribute("patientBloodOxygen", getBloodOxygenSaturation(obsService,wrapper.getPatient(),conceptBloodOxygen));
        config.addAttribute("patientPulse", getPulse(obsService,wrapper.getPatient(),conceptPulse));
        config.addAttribute("patientBloodPressure", getBloodPressure(obsService,wrapper.getPatient(),conceptBloodPressure));
        config.addAttribute("patientRespiratoryRate", getRespiratoryRate(obsService,wrapper.getPatient(),conceptRespiratoryRate));
        */
        
        config.addAttribute("weightText", "Latest weight");
        /*
        config.addAttribute("HeightText", "Latest Height");
        config.addAttribute("TemperatureText", "Temperature");
        config.addAttribute("BMIText", "BMI");
        config.addAttribute("BloodOxygenText", "Blood Oxygen Level");
        config.addAttribute("PulseText", "Pulse");
        config.addAttribute("BloodPressureText", "Blood Pressure");
        config.addAttribute("RespiratoryRateText", "Respiratory Rate");
        */
	}
	private String getWeight(ObsService obsService, Patient patient, Concept concept) {
		
		Double weightField=0.0;			
		Obs obs = null;
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient,concept);
		if(!obsList.isEmpty())
			obs = obsList.get(0);
		else return "";
		weightField=obs.getValueNumeric();
		return String.valueOf(weightField);
	}
	/*
	private String getHeight(ObsService obsService, Patient patient, Concept concept) {
		
		Double heightField=0.0;			
		Obs obs = null;
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient,concept);
		if(!obsList.isEmpty())
			obs = obsList.get(0);
		else return "";
		heightField=obs.getValueNumeric();
		return String.valueOf(heightField);
	}
	private String getTemperature(ObsService obsService, Patient patient, Concept concept) {
		
		Double temperatureField=0.0;			
		Obs obs = null;
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient,concept);
		if(!obsList.isEmpty())
			obs = obsList.get(0);
		else return "";
		temperatureField=obs.getValueNumeric();
		return String.valueOf(temperatureField);
	}
	private String getBMI(ObsService obsService, Patient patient, Concept concept) {
		
		Double BMIField=0.0;			
		Obs obs = null;
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient,concept);
		if(!obsList.isEmpty())
			obs = obsList.get(0);
		else return "";
		BMIField=obs.getValueNumeric();
		return String.valueOf(BMIField);
	}
	private String getPulse(ObsService obsService, Patient patient, Concept concept) {
		
		Double pulseField=0.0;			
		Obs obs = null;
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient,concept);
		if(!obsList.isEmpty())
			obs = obsList.get(0);
		else return "";
		pulseField=obs.getValueNumeric();
		return String.valueOf(pulseField);
	}
	private String getRespiratoryRate(ObsService obsService, Patient patient, Concept concept) {
		
		Double resporatoryRateField=0.0;			
		Obs obs = null;
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient,concept);
		if(!obsList.isEmpty())
			obs = obsList.get(0);
		else return "";
		resporatoryRateField=obs.getValueNumeric();
		return String.valueOf(resporatoryRateField);
	}
	private String getBloodPressure(ObsService obsService, Patient patient, Concept concept) {
		
		Double bloodPressureField=0.0;			
		Obs obs = null;
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient,concept);
		if(!obsList.isEmpty())
			obs = obsList.get(0);
		else return "";
		bloodPressureField=obs.getValueNumeric();
		return String.valueOf(bloodPressureField);
	}
	private String getBloodOxygenSaturation(ObsService obsService, Patient patient, Concept concept) {
		
		Double bloodOxygenField=0.0;			
		Obs obs = null;
		List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient,concept);
		if(!obsList.isEmpty())
			obs = obsList.get(0);
		else return "";
		bloodOxygenField=obs.getValueNumeric();
		return String.valueOf(bloodOxygenField);
	}
	*/
		
}