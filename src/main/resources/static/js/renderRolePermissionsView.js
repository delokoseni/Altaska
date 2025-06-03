export async function renderRolePermissionsView(role) {
    const roleId = role.id;
    const roleName = role.name;
    const projectId = role.idProject === null ? 0 : role.idProject;

    try {
        const response = await fetch(`/api/projects/${projectId}/roles/${roleId}/permissions`);
        if (!response.ok) throw new Error('Ошибка загрузки прав');

        const permissions = await response.json();

        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';

        const wrapper = document.createElement('div');
        wrapper.className = 'modal-wrapper';

        const modal = document.createElement('div');
        modal.className = 'modal';

        const header = document.createElement('div');
        header.className = 'modal-header';
        header.textContent = `Права роли: ${roleName}`;

        const closeIcon = document.createElement('span');
        closeIcon.className = 'modal-close-icon';
        closeIcon.innerHTML = '&times;';
        closeIcon.onclick = () => document.body.removeChild(overlay);
        header.appendChild(closeIcon);

        const content = document.createElement('div');
        content.className = 'modal-content';

        if (!permissions || permissions.length === 0) {
            const noPerm = document.createElement('p');
            noPerm.textContent = 'Нет прав у этой роли.';
            content.appendChild(noPerm);
        } else {
            const permissionsList = document.createElement('ul');
            permissions.forEach(permission => {
                const item = document.createElement('li');
                item.textContent = permission.name;
                permissionsList.appendChild(item);
            });
            content.appendChild(permissionsList);
        }

        const footer = document.createElement('div');
        footer.className = 'modal-footer';

        modal.appendChild(header);
        modal.appendChild(content);
        modal.appendChild(footer);
        wrapper.appendChild(modal);
        overlay.appendChild(wrapper);
        document.body.appendChild(overlay);

        overlay.addEventListener('click', (e) => {
            if (!e.target.closest('.modal')) {
                document.body.removeChild(overlay);
            }
        });


    } catch (error) {
        console.error('Ошибка при отображении прав роли:', error);
        alert('Не удалось загрузить права роли.');
    }
}
