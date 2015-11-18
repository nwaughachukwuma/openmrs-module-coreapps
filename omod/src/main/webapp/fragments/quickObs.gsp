<% if(config.patientWeight!="") { %>
	<div><small>
		<span>
		<span>${config.patientWeight}</span><span>${ui.message("coreapps.units.kilograms")}&nbsp;</span>
		<em>${config.weightText}</em>
		</span>
	</small></div>
<% } %>