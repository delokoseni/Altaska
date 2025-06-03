function togglePasswordVisibility(id) {
    var passwordField = document.getElementById(id);
    var eyeIcon = document.getElementById('eye-icon-' + id);

    if (passwordField.type === "password") {
        passwordField.type = "text";
        eyeIcon.src = "/icons/EyeOpen.svg";
    } else {
        passwordField.type = "password";
        eyeIcon.src = "/icons/EyeSlashed.svg";
    }
}
