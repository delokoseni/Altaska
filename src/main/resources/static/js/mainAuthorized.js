document.addEventListener('DOMContentLoaded', () => {
    const showButton = document.getElementById('show-create-form');
    const mainContent = document.querySelector('.main-content');
    const previousContent = mainContent.innerHTML;
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
                <label>Название проекта:</label>
                <input type="text" name="name" required>
                <label>Описание:</label>
                <textarea name="description" rows="4" cols="50"></textarea>
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

            const backButton = document.createElement('button');
            backButton.textContent = 'Назад';
            backButton.type = 'button';
            backButton.className = 'back-button';
            backButton.onclick = () => {
                goToMainPage();
                //mainContent.innerHTML = previousContent;
            };

            formWrapper.appendChild(backButton);
            form.appendChild(csrfInput);
            form.appendChild(createdAtInput);
            form.appendChild(updatedAtInput);
            form.appendChild(submitBtn);

            formWrapper.appendChild(form);
            mainContent.appendChild(formWrapper);

            form.addEventListener('submit', async (event) => {
                event.preventDefault();
                const now = new Date();
                createdAtInput.value = now.toISOString().split('T')[0];
                updatedAtInput.value = now.toISOString();
                const formData = new FormData(form);
                try {
                    const response = await fetch('/create-project', {
                        method: 'POST',
                        body: formData
                    });
                    const result = await response.text();
                    if (!response.ok) {
                        alert(result);
                    } else {
                        window.location.href = '/mainauthorized';
                    }
                } catch (error) {
                    alert('Ошибка при создании проекта: ' + error.message);
                }
            });

        });
    }
});
