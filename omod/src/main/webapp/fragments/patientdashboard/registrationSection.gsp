<%
    config.contextModel.put("returnUrl", ui.thisUrl())
%>

<div id="section-${config.section.id}" class="contact-info-inline">

    <h2>${ ui.message(config.section.label) }</h2>
    <% config.section.questions.each { %>
        <% it.fields.each { %>
            <% if(it.data.size == 1) { %>
                <span>
                    <span>${ ui.format(it.fieldValue) }&nbsp;</span>
                    <em>${ ui.message(it.fieldLabel) }</em>
                </span>
            <% } else { %>
                <% it.data.each { %>
                    <span>
                        <span>${ ui.format(it.value) }&nbsp;</span>
                        <em>${ ui.message(it.label) }</em>
                    </span>
                <% } %>
            <% } %>
        <% } %>
    <% } %>

    <% if(!config.hideEditDemographicsButton) { %>
        <small id="section-edit-${config.section.id}" class="edit-info" class="left-margin">
            <%= ui.includeFragment("uicommons", "extension", [ extension: config.section.linkExtension, contextModel: config.contextModel ]) %>
        </small>
    <% } %>
</div>