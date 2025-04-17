function showTaskDetails(task) {
    const existing = document.querySelector('.task-details-sidebar');
    if (existing) existing.remove();

    const sidebar = document.createElement('div');
    sidebar.className = 'task-details-sidebar slide-in';

    const formatDeadline = task.deadline ? new Date(task.deadline).toISOString().slice(0, 16) : '';

    Promise.all([
        fetch('/api/priorities').then(res => res.json()),
        fetch('/api/statuses').then(res => res.json())
    ]).then(([priorities, statuses]) => {

        const prioritySelect = document.createElement('select');
        prioritySelect.className = 'editable-field task-priority';
        prioritySelect.disabled = true;
        const noPriorityOption = document.createElement('option');
        noPriorityOption.value = '';
        noPriorityOption.textContent = 'Без приоритета';
        prioritySelect.appendChild(noPriorityOption);
        priorities.forEach(p => {
            const option = document.createElement('option');
            option.value = p.id;
            option.textContent = p.name;
            if (task.idPriority && task.idPriority.id === p.id) {
                option.selected = true;
            }
            prioritySelect.appendChild(option);
        });

        const statusSelect = document.createElement('select');
        statusSelect.className = 'editable-field task-status';
        statusSelect.disabled = true;
        statuses.forEach(s => {
            const option = document.createElement('option');
            option.value = s.id;
            option.textContent = s.name;
            if (task.idStatus && task.idStatus.id === s.id) {
                option.selected = true;
            }
            statusSelect.appendChild(option);
        });

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
                <div class="status-container"></div>
                <button class="edit-button" data-target="status">Изменить</button>

                <label class="field-label">Приоритет:</label>
                <div class="priority-container"></div>
                <button class="edit-button" data-target="priority">Изменить</button>

                <label class="field-label">Срок:</label>
                <input class="editable-field task-deadline" type="datetime-local" value="${formatDeadline}" disabled />
                <button class="edit-button" data-target="deadline">Изменить</button>
            </div>
        `;

        sidebar.querySelector('.priority-container').appendChild(prioritySelect);
        sidebar.querySelector('.status-container').appendChild(statusSelect);

        document.body.appendChild(sidebar);

        sidebar.querySelectorAll('.edit-button').forEach(button => {
            button.addEventListener('click', () => {
                const targetClass = {
                    name: '.task-name',
                    description: '.task-description',
                    deadline: '.task-deadline',
                    priority: '.task-priority',
                    status: '.task-status'
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
    }).catch(error => {
        console.error('Ошибка загрузки приоритетов или статусов:', error);
    });
}
