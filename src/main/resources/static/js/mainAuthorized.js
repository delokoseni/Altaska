document.addEventListener('DOMContentLoaded', () => {
    const showButton = document.getElementById('show-create-form');
    const mainContent = document.querySelector('.main-content');

    if (showButton && mainContent) {
        showButton.addEventListener('click', () => {
            mainContent.innerHTML = '';

            const formWrapper = document.createElement('div');
            formWrapper.id = 'create-project-form';

            const form = document.createElement('form');
            form.method = 'post';
            form.action = '/create-project';

            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = csrfParam;
            csrfInput.value = csrfToken;

            form.innerHTML += `
                <label>Название проекта:</label><br>
                <input type="text" name="name" required><br><br>

                <label>Описание:</label><br>
                <textarea name="description" rows="4" cols="50" required></textarea><br><br>
            `;

            const createdAtInput = document.createElement('input');
            createdAtInput.type = 'hidden';
            createdAtInput.name = 'createdAt';
            createdAtInput.id = 'createdAt';

            const updatedAtInput = document.createElement('input');
            updatedAtInput.type = 'hidden';
            updatedAtInput.name = 'updatedAt';
            updatedAtInput.id = 'updatedAt';

            const submitBtn = document.createElement('button');
            submitBtn.type = 'submit';
            submitBtn.textContent = 'Создать';

            form.appendChild(csrfInput);
            form.appendChild(createdAtInput);
            form.appendChild(updatedAtInput);
            form.appendChild(submitBtn);

            formWrapper.appendChild(form);
            mainContent.appendChild(formWrapper);

            form.addEventListener('submit', (event) => {
                const now = new Date();
                createdAtInput.value = now.toISOString().split('T')[0];
                updatedAtInput.value = now.toISOString();
            });
        });
    }
});
