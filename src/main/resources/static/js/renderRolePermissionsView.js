export async function renderRolePermissionsView(role) {
    const roleId = role.id;
    const roleName = role.name;
    const projectId = role.idProject;
    console.log('role:', role);
    console.log('roleId:', roleId);
    console.log('roleName:', roleName);
    console.log('projectId:', projectId);

    try {
        const response = await fetch(`/api/projects/${projectId}/roles/${roleId}/permissions`);
        if (!response.ok) throw new Error('Ошибка загрузки прав');

        const permissions = await response.json();

        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';

        const modal = document.createElement('div');
        modal.className = 'modal';

        const title = document.createElement('h3');
        title.textContent = `Права роли: ${roleName}`;
        modal.appendChild(title);

        if (!permissions || permissions.length === 0) {
            const noPerm = document.createElement('p');
            noPerm.textContent = 'Нет прав у этой роли.';
            modal.appendChild(noPerm);
        } else {
            const permissionsList = document.createElement('ul');
            permissions.forEach(permission => {
                const item = document.createElement('li');
                item.textContent = permission.name;
                permissionsList.appendChild(item);
            });
            modal.appendChild(permissionsList);
        }

        const closeBtn = document.createElement('button');
        closeBtn.textContent = 'Закрыть';
        closeBtn.onclick = () => document.body.removeChild(overlay);
        modal.appendChild(closeBtn);

        overlay.appendChild(modal);
        document.body.appendChild(overlay);

    } catch (error) {
        console.error('Ошибка при отображении прав роли:', error);
        alert('Не удалось загрузить права роли.');
    }
}
