<% if(config.patientWeight!="") { %>
	<div class="contact-info-inline">
		<span>
		<span>${config.patientWeight}</span><span>${ui.message("coreapps.units.kilograms")}&nbsp;</span>
		<em>${config.weightText}</em>
		</span>
	</div>
<% } %>