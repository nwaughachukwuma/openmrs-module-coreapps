<%
    config.contextModel.put("returnUrl", ui.thisUrl())
%>

<div class="contact-info-inline">

    <% config.section.questions.each { %>
        <% it.fields.each { %>
            <span>
                <span>${ ui.format(it.fieldValue) }&nbsp;</span>
                <em>${ ui.message(it.fieldLabel) }</em>
            </span>
        <% } %>
    <% } %>

    <% if(!config.hideEditDemographicsButton) { %>
        <small class="edit-info" class="left-margin">
            <%= ui.includeFragment("uicommons", "extension", [ extension: config.section.linkExtension, contextModel: config.contextModel ]) %>
        </small>
    <% } %>
</div>