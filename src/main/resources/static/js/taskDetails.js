function showTaskDetails(task) {
    const existing = document.querySelector('.task-details-sidebar');
    if (existing) existing.remove();

    const sidebar = document.createElement('div');
    sidebar.className = 'task-details-sidebar slide-in';

    const formatDeadline = task.deadline ? new Date(task.deadline).toISOString().slice(0, 16) : '';

    fetch('/api/priorities')
        .then(response => response.json())
        .then(priorities => {
            return fetch('/api/statuses')
                .then(response => response.json())
                .then(statuses => ({ priorities, statuses }));
        })
        .then(({ priorities, statuses }) => {
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

            sidebar.innerHTML = '';

            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = csrfParam;
            csrfInput.value = csrfToken;

            const closeButton = document.createElement('button');
            closeButton.className = 'close-button';
            closeButton.title = 'Закрыть';
            closeButton.innerHTML = '&times;';
            closeButton.onclick = () => {
                sidebar.remove();
                window.removeEventListener('click', outsideClickListener);
            };

            const content = document.createElement('div');
            content.className = 'task-details-content';

            content.innerHTML = `
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
            `;

            content.querySelector('.priority-container').appendChild(prioritySelect);
            content.querySelector('.status-container').appendChild(statusSelect);
            content.appendChild(csrfInput);

            sidebar.appendChild(closeButton);
            sidebar.appendChild(content);
            document.body.appendChild(sidebar);

            sidebar.querySelectorAll('.edit-button').forEach(button => {
                button.addEventListener('click', () => {
                    const target = button.dataset.target;
                    const targetClass = {
                        name: '.task-name',
                        description: '.task-description',
                        deadline: '.task-deadline',
                        priority: '.task-priority',
                        status: '.task-status'
                    }[target];

                    const field = sidebar.querySelector(targetClass);
                    const isDisabled = field.disabled;

                    if (isDisabled) {
                        field.disabled = false;
                        button.textContent = 'Сохранить';
                    } else {
                        field.disabled = true;
                        button.textContent = 'Изменить';

                        const formData = new URLSearchParams();
                        if (target === 'name' || target === 'description') {
                            formData.append(target, field.value);
                        } else if (target === 'priority') {
                            formData.append('priorityId', field.value || '');
                        } else if (target === 'status') {
                            formData.append('statusId', field.value);
                        } else if (target === 'deadline') {
                            formData.append('deadline', field.value);
                        }
                        formData.append(csrfParam, csrfToken);

                        fetch(`/api/tasks/${task.id}/${target}`, {
                            method: 'PUT',
                            headers: {
                                'Content-Type': 'application/x-www-form-urlencoded'
                            },
                            body: formData
                        })
                        .then(response => {
                            if (!response.ok) throw new Error('Ошибка при сохранении');
                            return response.json();
                        })
                        .then(updatedTask => {
                            console.log(`Поле "${target}" обновлено успешно`, updatedTask);
                        })
                        .catch(err => {
                            alert(`Не удалось обновить ${target}: ${err.message}`);
                            field.disabled = false;
                            button.textContent = 'Сохранить';
                        });
                    }
                });
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
        })
        .catch(error => {
            console.error('Ошибка загрузки приоритетов или статусов:', error);
        });
}
