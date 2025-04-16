function showTaskDetails(task) {
    const existing = document.querySelector('.task-details-sidebar');
    if (existing) existing.remove();

    const sidebar = document.createElement('div');
    sidebar.className = 'task-details-sidebar slide-in';

    const formatDeadline = task.deadline ? new Date(task.deadline).toISOString().slice(0, 16) : '';

    sidebar.innerHTML = `
        <button class="close-button" title="Закрыть">&times;</button>
        <div class="task-details-content">
            <h2>${task.name}</h2>

            <label class="field-label">Название:</label>
            <input class="editable-field task-name" value="${task.name}" disabled />
            <button class="edit-button" data-target="name">Изменить</button>

            <label class="field-label">Описание:</label>
            <textarea class="editable-field task-description" rows="5" disabled>${task.description || ''}</textarea>
            <button class="edit-button" data-target="description">Изменить</button>

            <label class="field-label">Статус:</label>
            <div class="field-value">${task.idStatus?.name || 'Неизвестно'}</div>

            <label class="field-label">Приоритет:</label>
            <div class="field-value">${task.idPriority?.name || 'Без приоритета'}</div>

            <label class="field-label">Срок:</label>
            <input class="editable-field task-deadline" type="datetime-local" value="${formatDeadline}" disabled />
            <button class="edit-button" data-target="deadline">Изменить</button>
        </div>
    `;

    document.body.appendChild(sidebar);

    sidebar.querySelectorAll('.edit-button').forEach(button => {
        button.addEventListener('click', () => {
            const targetClass = {
                name: '.task-name',
                description: '.task-description',
                deadline: '.task-deadline'
            }[button.dataset.target];

            const field = sidebar.querySelector(targetClass);
            const isDisabled = field.disabled;

            field.disabled = !isDisabled;
            button.textContent = isDisabled ? 'Сохранить' : 'Изменить';
        });
    });

    const closeButton = sidebar.querySelector('.close-button');
    closeButton.addEventListener('click', () => {
        sidebar.remove();
        window.removeEventListener('click', outsideClickListener);
    });

    setTimeout(() => {
        window.addEventListener('click', outsideClickListener);
    }, 0);

    function outsideClickListener(e) {
        if (!sidebar.contains(e.target)) {
            sidebar.remove();
            window.removeEventListener('click', outsideClickListener);
        }
    }
}
