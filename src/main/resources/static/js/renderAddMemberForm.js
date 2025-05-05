import { showLoader, hideLoader } from './loader.js';

export function renderAddMemberForm(container, projectId, roles, previousContentBackup, loadProjectInfoView) {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
    if (!previousContentBackup) {
        previousContentBackup = container.innerHTML;
    }
    container.innerHTML = '';

    const form = document.createElement('div');
    form.className = 'add-member-form';

    const emailLabel = document.createElement('label');
    emailLabel.textContent = 'Email участника:';
    const emailInput = document.createElement('input');
    emailInput.type = 'email';
    emailInput.required = true;
    emailInput.classList.add('member-email-input');

    const roleLabel = document.createElement('label');
    roleLabel.textContent = 'Выберите роль:';
    const roleSelect = document.createElement('select');
    roleSelect.required = true;

    roles.forEach(role => {
        const option = document.createElement('option');
        option.value = role.id;
        option.textContent = role.name;
        roleSelect.appendChild(option);
    });

    const inviteButton = document.createElement('button');
    inviteButton.textContent = 'Пригласить';

    inviteButton.onclick = () => {
        const email = emailInput.value.trim();
        const roleId = roleSelect.value;

        if (!email || !roleId) {
            alert('Пожалуйста, заполните все поля.');
            return;
        }
        showLoader();
        const clientDate = new Date().toISOString().split('T')[0];
        const inviteUrl = `/api/projects/${projectId}/invite?email=${encodeURIComponent(email)}&roleId=${roleId}&clientDate=${clientDate}`;
        fetch(inviteUrl, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        })
        .then(res => {
            hideLoader();
            if (res.ok) {
                alert('Приглашение отправлено!');
                container.innerHTML = previousContentBackup;
                loadProjectInfoView(projectId);
            } else {
                return res.text().then(text => { throw new Error(text); });
            }
        })
        .catch(err => {
            console.error('Ошибка при отправке приглашения:', err);
            alert('Ошибка при отправке приглашения: ' + err.message);
        });
    };

    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Отмена';
    cancelButton.onclick = () => {
        container.innerHTML = previousContentBackup;
        loadProjectInfoView(projectId);
    };

    form.appendChild(emailLabel);
    form.appendChild(emailInput);
    form.appendChild(roleLabel);
    form.appendChild(roleSelect);
    form.appendChild(inviteButton);
    form.appendChild(cancelButton);
    container.appendChild(form);
}
