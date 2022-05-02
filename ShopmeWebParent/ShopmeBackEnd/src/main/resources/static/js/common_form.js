// function to cancel the creation of new user and redirect to users page
$(document).ready(function () {
	$("#buttonCancel").on("click", function () {
		window.location = moduleURL;
	});

	$("#fileImage").change(function () {
		fileSize = this.files[0].size;

		if (fileSize > 1048576) {
			this.setCustomValidity("Image size should be less than 1MB");
			this.reportValidity();
			//alert("Image size should be less than 1MB your's' is:" + fileSize + "KB");
		} else {
			this.setCustomValidity("");
			showImageThumbnail(this);
		}

	});
});

//function to show the selected file for preview
function showImageThumbnail(fileInput) {
	var file = fileInput.files[0];
	var reader = new FileReader();
	reader.onload = function (e) {
		$("#thumbnail").attr("src", e.target.result);
	};

	reader.readAsDataURL(file);

};		