document.addEventListener('DOMContentLoaded', () => {
    const showButton = document.getElementById('show-create-form');
    const form = document.getElementById('create-project-form');
    console.log(showButton, form); // Для отладки

    if (showButton && form) {
        showButton.addEventListener('click', () => {
            form.style.display = 'block';
        });
    }
});
