import { showToast } from './toast.js';

export function addSubtask(taskId, subtaskData, csrfToken, callback) {
    const url = `/api/tasks/${taskId}/subtasks`;
    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken,
        },
        body: JSON.stringify([subtaskData])
    })
    .then(async response => {
        if (!response.ok) {
            const errorText = await response.text();
            const hasEnglish = /[a-zA-Z]/.test(errorText);
            if (hasEnglish) {
                showToast("Не удалось добавить подзадачу: неизвестная ошибка", "error");
            } else {
                showToast("Не удалось добавить подзадачу: " + errorText, "error");
            }
            throw new Error(errorText);
        }
        return response.json();
    })
    .then(subtasks => {
        if (Array.isArray(subtasks) && subtasks.length > 0) {
            showToast("Подзадача успешно добавлена");
            callback(subtasks[0]);
        } else {
            showToast("Не удалось добавить подзадачу: пустой ответ от сервера", "error");
            throw new Error('Подзадача не добавлена');
        }
    })
    .catch(error => {
        const message = error.message || '';
        const hasEnglish = /[a-zA-Z]/.test(message);
        if (hasEnglish) {
            if (message === 'Failed to fetch') {
                showToast("Не удалось добавить подзадачу: проверьте подключение к интернету или попробуйте позже", "error");
            } else {
                showToast("Не удалось добавить подзадачу: неизвестная ошибка", "error");
            }
        } else {
            showToast("Не удалось добавить подзадачу: " + message, "error");
        }
        console.error("Ошибка добавления подзадачи:", error);
    });
}

export function updateSubtask(taskId, subtaskId, subtaskData, csrfToken, callback) {
    const url = `/api/tasks/${taskId}/subtasks/${subtaskId}`;
    fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken,
        },
        body: JSON.stringify(subtaskData),
    })
    .then(async response => {
        if (!response.ok) {
            const errorText = await response.text();
            const hasEnglish = /[a-zA-Z]/.test(errorText);
            if (hasEnglish) {
                showToast("Не удалось обновить подзадачу: неизвестная ошибка", "error");
            } else {
                showToast("Не удалось обновить подзадачу: " + errorText, "error");
            }
            throw new Error(errorText);
        }
        return response.json();
    })
    .then(subtask => {
        showToast("Подзадача успешно обновлена");
        callback(subtask);
    })
    .catch(error => {
        const message = error.message || '';
        const hasEnglish = /[a-zA-Z]/.test(message);
        if (hasEnglish) {
            if (message === 'Failed to fetch') {
                showToast("Не удалось обновить подзадачу: проверьте подключение к интернету или попробуйте позже", "error");
            } else {
                showToast("Не удалось обновить подзадачу: неизвестная ошибка", "error");
            }
        } else {
            showToast("Не удалось обновить подзадачу: " + message, "error");
        }
    });
}

export function deleteSubtask(taskId, subtaskId, csrfToken, callback) {
    const url = `/api/tasks/${taskId}/subtasks/${subtaskId}`;
    fetch(url, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': csrfToken,
        },
    })
    .then(async response => {
        if (!response.ok) {
            const errorText = await response.text();
            const hasEnglish = /[a-zA-Z]/.test(errorText);
            if (hasEnglish) {
                showToast("Не удалось удалить подзадачу: неизвестная ошибка", "error");
            } else {
                showToast("Не удалось удалить подзадачу: " + errorText, "error");
            }
            throw new Error(errorText);
        }
        showToast("Подзадача успешно удалена");
        callback(subtaskId);
    })
    .catch(error => {
        const message = error.message || '';
        const hasEnglish = /[a-zA-Z]/.test(message);
        if (hasEnglish) {
            if (message === 'Failed to fetch') {
                showToast("Не удалось удалить подзадачу: проверьте подключение к интернету или попробуйте позже", "error");
            } else {
                showToast("Не удалось удалить подзадачу: неизвестная ошибка", "error");
            }
        } else {
            showToast("Не удалось удалить подзадачу: " + message, "error");
        }
    });
}

export function createSubtasksSection() {
    const container = document.createElement('div');
    container.className = 'subtasks-section';

    const title = document.createElement('h4');
    title.textContent = 'Подзадачи';
    container.appendChild(title);

    const list = document.createElement('ul');
    list.className = 'subtask-list';
    container.appendChild(list);

    const addBtn = document.createElement('button');
    addBtn.type = 'button';
    addBtn.textContent = 'Добавить подзадачу';
    addBtn.onclick = () => {
        const item = document.createElement('li');
        item.className = 'subtask-item';

        const nameInput = document.createElement('input');
        nameInput.type = 'text';
        nameInput.placeholder = 'Название подзадачи';
        nameInput.required = true;
        nameInput.className = 'subtask-name';

        const descInput = document.createElement('textarea');
        descInput.placeholder = 'Описание подзадачи';
        descInput.className = 'subtask-description';

        const removeBtn = document.createElement('button');
        removeBtn.type = 'button';
        removeBtn.textContent = 'Удалить';
        removeBtn.onclick = () => item.remove();

        item.appendChild(nameInput);
        item.appendChild(descInput);
        item.appendChild(removeBtn);

        list.appendChild(item);
    };

    container.appendChild(addBtn);
    return container;
}

export function initSubtasksSection(task, csrfToken) {
    const section = document.createElement('div');
    section.className = 'task-subtasks-section';

    const title = document.createElement('h3');
    title.textContent = 'Подзадачи';
    section.appendChild(title);

    const list = document.createElement('div');
    list.className = 'subtask-list';
    section.appendChild(list);

    const addButton = document.createElement('button');
    addButton.textContent = 'Добавить подзадачу';
    addButton.onclick = () => addSubtaskForm(task.id, list, csrfToken);
    section.appendChild(addButton);

    fetch(`/api/tasks/${task.id}/subtasks`)
        .then(res => res.json())
        .then(subtasks => {
            if (subtasks.length === 0) {
                const empty = document.createElement('p');
                empty.textContent = 'Нет подзадач';
                list.appendChild(empty);
            } else {
                subtasks.forEach(st => list.appendChild(createSubtaskItem(st, task.id, csrfToken)));
            }
        });

    return section;
}

function createSubtaskItem(subtask, taskId, csrfToken) {
    const wrapper = document.createElement('div');
    wrapper.className = 'subtask-item';

    const nameInput = document.createElement('input');
    nameInput.className = 'subtask-name';
    nameInput.value = subtask.name;
    nameInput.disabled = true;

    const descInput = document.createElement('input');
    descInput.className = 'subtask-description';
    descInput.value = subtask.description || '';
    descInput.disabled = true;

    const editButton = document.createElement('button');
    editButton.textContent = 'Изменить';
    editButton.onclick = () => {
        if (nameInput.disabled) {
            nameInput.disabled = false;
            descInput.disabled = false;
            editButton.textContent = 'Сохранить';
        } else {
            const updatedData = {
                name: nameInput.value,
                description: descInput.value
            };

            updateSubtask(taskId, subtask.id, updatedData, csrfToken, (updatedSubtask) => {
                nameInput.disabled = true;
                descInput.disabled = true;
                editButton.textContent = 'Изменить';
            });
        }
    };

    const deleteButton = document.createElement('button');
    deleteButton.textContent = 'Удалить';
    deleteButton.onclick = () => {
        deleteSubtask(taskId, subtask.id, csrfToken, () => {
            wrapper.remove();
        });
    };

    wrapper.appendChild(nameInput);
    wrapper.appendChild(descInput);
    wrapper.appendChild(editButton);
    wrapper.appendChild(deleteButton);

    return wrapper;
}

function addSubtaskForm(taskId, list, csrfToken) {
    const wrapper = document.createElement('div');
    wrapper.className = 'subtask-item';

    const nameInput = document.createElement('input');
    nameInput.placeholder = 'Название подзадачи';
    nameInput.className = 'subtask-name';

    const descInput = document.createElement('input');
    descInput.placeholder = 'Описание';
    descInput.className = 'subtask-description';

    const saveBtn = document.createElement('button');
    saveBtn.textContent = 'Сохранить';
    saveBtn.onclick = () => {
        const name = nameInput.value.trim();
        const description = descInput.value.trim();

        if (!name) return alert('Название обязательно');

        const data = { name, description };
        addSubtask(taskId, data, csrfToken, (newSubtask) => {
            refreshSubtaskList(taskId, list, csrfToken);
        });
    };

    wrapper.appendChild(nameInput);
    wrapper.appendChild(descInput);
    wrapper.appendChild(saveBtn);

    list.appendChild(wrapper);
}

function refreshSubtaskList(taskId, list, csrfToken) {
    list.innerHTML = '';
    fetch(`/api/tasks/${taskId}/subtasks`)
        .then(res => res.json())
        .then(subtasks => {
            if (subtasks.length === 0) {
                const empty = document.createElement('p');
                empty.textContent = 'Нет подзадач';
                list.appendChild(empty);
            } else {
                subtasks.forEach(st => list.appendChild(createSubtaskItem(st, taskId, csrfToken)));
            }
        })
        .catch(err => alert('Ошибка при обновлении списка подзадач: ' + err.message));
}
