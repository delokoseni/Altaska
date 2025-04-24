export function addSubtask(taskId, subtaskData, callback) {
    const url = '/api/tasks/' + taskId + '/subtasks'; // API эндпоинт для добавления подзадачи
    fetch(url, {
        method: 'POST',
        headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': csrfToken,  // CSRF-токен
        },
        body: JSON.stringify(subtaskData),
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            callback(data.subtask);  // Возвращаем данные подзадачи
        } else {
            alert('Ошибка при добавлении подзадачи');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
    });
}

export function updateSubtask(taskId, subtaskId, subtaskData, callback) {
    const url = `/api/tasks/${taskId}/subtasks/${subtaskId}`; // API эндпоинт для редактирования подзадачи
    fetch(url, {
        method: 'PUT',
        headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': csrfToken,  // CSRF-токен
        },
        body: JSON.stringify(subtaskData),
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            callback(data.subtask);  // Возвращаем обновлённую подзадачу
        } else {
            alert('Ошибка при редактировании подзадачи');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
    });
}

export function deleteSubtask(taskId, subtaskId, callback) {
    const url = `/api/tasks/${taskId}/subtasks/${subtaskId}`; // API эндпоинт для удаления подзадачи
    fetch(url, {
        method: 'DELETE',
        headers: {
        'X-CSRF-TOKEN': csrfToken,  // CSRF-токен
        },
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            callback(subtaskId);  // Возвращаем ID удалённой подзадачи
        } else {
            alert('Ошибка при удалении подзадачи');
        }
    })
    .catch(error => {
        console.error('Ошибка:', error);
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
