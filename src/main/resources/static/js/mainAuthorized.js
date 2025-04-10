document.addEventListener('DOMContentLoaded', () => {
    const showButton = document.getElementById('show-create-form');
    const form = document.getElementById('create-project-form');

    if (showButton && form) {
        showButton.addEventListener('click', () => {
            form.style.display = 'block';
        });
    }
    const projectForm = form.querySelector('form');
        if (projectForm) {
            projectForm.addEventListener('submit', (event) => {
                const now = new Date();
                document.getElementById('createdAt').value = now.toISOString().split('T')[0]; // YYYY-MM-DD
                document.getElementById('updatedAt').value = now.toISOString(); // ISO 8601, подходит для OffsetDateTime
            });
        }
});
