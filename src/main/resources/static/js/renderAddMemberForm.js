import { showLoader, hideLoader } from './loader.js';

export async function renderAddMemberForm(container, projectId, previousContentBackup, loadProjectInfoView, handleFetchWithToast) {
    window.scrollTo({ top: 0, behavior: 'smooth' });

    if (!previousContentBackup) {
        previousContentBackup = container.innerHTML;
    }

    container.innerHTML = '';
    showLoader();

    let roles;
    try {
        const res = await fetch(`/api/projects/${projectId}/roles`);
        if (!res.ok) throw new Error('Ошибка при загрузке ролей');
        roles = await res.json();
    } catch (err) {
        console.error('Ошибка загрузки ролей:', err);
        hideLoader();
        showToast('Не удалось загрузить роли.', 'error');
        container.innerHTML = previousContentBackup;
        return;
    }

    hideLoader();

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
    inviteButton.onclick = async () => {
        const email = emailInput.value.trim();
        const roleId = roleSelect.value;

        if (!email || !roleId) {
            showToast('Пожалуйста, заполните все поля.', 'error');
            return;
        }

        const clientDate = new Date().toISOString().split('T')[0];
        const inviteUrl = `/api/projects/${projectId}/invite?email=${encodeURIComponent(email)}&roleId=${roleId}&clientDate=${clientDate}`;

        showLoader();
        try {
            await handleFetchWithToast(
                inviteUrl,
                {
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    }
                },
                'Приглашение отправлено!',
                'Ошибка при отправке приглашения'
            );
            container.innerHTML = previousContentBackup;
            loadProjectInfoView(projectId);
        } catch (err) {
            console.error('Ошибка при отправке приглашения:', err);
        } finally {
            hideLoader();
        }
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
