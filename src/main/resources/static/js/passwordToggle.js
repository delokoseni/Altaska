function togglePasswordVisibility(id) {
    var passwordField = document.getElementById(id);
    var eyeIcon = document.getElementById('eye-icon-' + id);

    if (passwordField.type === "password") {
        passwordField.type = "text"; // Установить тип text
        eyeIcon.src = "/icons/EyeOpen.svg"; // Использовать открытую иконку
    } else {
        passwordField.type = "password"; // Установить тип password
        eyeIcon.src = "/icons/EyeSlashed.svg"; // Использовать перечеркнутую иконку
    }
}
