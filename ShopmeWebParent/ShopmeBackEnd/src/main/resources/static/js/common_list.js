//below is the code for showing modal dialogue
function showDeleteConfirmModal(link, entityName) {
		entityId = link.attr("entityId");
		
		$("#yesButton").attr("href", link.attr("href"));
		$("#confirmText").text("Are you sure you want to delete this " 
								+ entityName + " with ID " + entityId + " ?");
		$("#confirmModal").modal();
}


//below code is for clearing the filters
function clearFilter() {
	window.location = moduleURL;
}