import { initSubtasksSection } from './subTasks.js';
import { createPerformersSection, assignTask, unassignTask } from './taskPerformers.js';
import { initTaskCommentsSection, initCommentInputSection } from './comments.js';
import { initTaskFilesSection } from './attachments.js';
import { startTimer, pauseTimer, resetTimer, updateTimerDisplay } from './timer.js';
import { showToast } from './toast.js';

const targetLabels = {
    name: 'Название',
    description: 'Описание',
    startTime: 'Дата начала',
    deadline: 'Дедлайн',
    priority: 'Приоритет',
    status: 'Статус'
};

function toLocalDateTimeInputString(utcDateString) {
    if (!utcDateString) return '';
    const date = new Date(utcDateString);
    const offset = date.getTimezoneOffset(); // в минутах
    const localDate = new Date(date.getTime() - offset * 60 * 1000);
    return localDate.toISOString().slice(0, 16); // "YYYY-MM-DDTHH:mm"
}

function showTaskDetails(task, view = 'список') {
    const existing = document.querySelector('.task-details-sidebar');
    if (existing) existing.remove();

    const sidebar = document.createElement('div');
    sidebar.className = 'task-details-sidebar slide-in';

    const formatStartTime = toLocalDateTimeInputString(task.startTimeServer);
    const formatDeadline = toLocalDateTimeInputString(task.deadlineServer);

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
                <h2>Задача</h2>
                <label class="field-label">Таймер:</label>
                <div class="task-timer">
                    <span id="timer-${task.id}">00:00:00</span>
                    <button class="timer-btn" data-action="start">▶</button>
                    <button class="timer-btn" data-action="pause">⏸</button>
                    <button class="timer-btn" data-action="reset">⟲</button>
                </div>

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

                <label class="field-label">Дата начала:</label>
                <input class="editable-field task-start-time" type="datetime-local" value="${formatStartTime}" disabled />
                <button class="edit-button" data-target="startTime">Изменить</button>

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

            const timerData = JSON.parse(localStorage.getItem(`timer_${task.id}`));
            if (timerData) {
                updateTimerDisplay(task.id, timerData.timeSpent || 0);
                console.log("timerData.timeSpent", timerData.timeSpent);
            }

            const performersSection = document.createElement('div');
            performersSection.className = 'task-performers-section';
            content.appendChild(performersSection);
            createPerformersSection(task.id, task.idProject.id, performersSection, csrfToken);

            initTaskTagsSection(task, csrfParam, csrfToken, content);

            const subtasksSection = initSubtasksSection(task, csrfToken);
            content.appendChild(subtasksSection);

            const timerDiv = content.querySelector('.task-timer');
            timerDiv.querySelector('[data-action="start"]').onclick = () => startTimer(task.id);
            timerDiv.querySelector('[data-action="pause"]').onclick = () => pauseTimer(task.id);
            timerDiv.querySelector('[data-action="reset"]').onclick = () => resetTimer(task.id);


            // Проверка, является ли текущий пользователь исполнителем задачи
            fetch(`/api/task-performers/${task.id}/is-assigned`)
                .then(response => response.json())
                .then(isAssigned => {
                    const takeTaskButton = document.createElement('button');
                    takeTaskButton.className = 'take-task-button';

                    if (isAssigned) {
                        takeTaskButton.textContent = 'Отказаться от задачи';
                    } else {
                        takeTaskButton.textContent = 'Взять задачу';
                    }

                    takeTaskButton.onclick = () => {
                        const formData = new URLSearchParams();

                        if (isAssigned) {
                            // Отказаться от задачи
                            unassignTask(task.id, formData, csrfToken)
                                .then(() => {
                                    takeTaskButton.textContent = 'Взять задачу';
                                    showTaskDetails(task);  // Обновить детали задачи
                                })
                                .catch(err => {
                                    alert('Ошибка при отказе от задачи: ' + err.message);
                                });
                        } else {
                            // Взять задачу
                            assignTask(task.id, formData, csrfToken)
                                .then(() => {
                                    takeTaskButton.textContent = 'Отказаться от задачи';
                                    showTaskDetails(task);  // Обновить детали задачи
                                })
                                .catch(err => {
                                    alert('Ошибка при принятии задачи: ' + err.message);
                                });
                        }
                    };
                    content.appendChild(takeTaskButton);
                })
                .catch(err => {
                    alert('Ошибка при проверке исполнителя: ' + err.message);
                });

            sidebar.querySelectorAll('.edit-button').forEach(button => {
                button.addEventListener('click', () => {
                    const target = button.dataset.target;
                    const targetClass = {
                        name: '.task-name',
                        description: '.task-description',
                        startTime: '.task-start-time',
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
                        } else if (target === 'startTime') {
                            formData.append('startTime', field.value);
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
                        .then(async response => {
                            if (!response.ok) {
                                const errorText = await response.text();
                                throw new Error(errorText || 'Ошибка при сохранении');
                            }
                            return response.json();
                        })
                        .then(updatedTask => {
                            console.log(`Поле "${target}" обновлено успешно`, updatedTask);
                            showToast(`Поле "${targetLabels[target]}" успешно обновлено`, "success");
                            loadView(view, task.idProject.id);
                        })
                        .catch(err => {
                            const message = err.message || '';
                            const hasEnglishLetters = /[a-zA-Z]/.test(message);

                            if (hasEnglishLetters) {
                                if (message === 'Failed to fetch') {
                                    showToast(`Не удалось обновить "${targetLabels[target]}": проверьте подключение к интернету или попробуйте позже`, "error");
                                } else {
                                    showToast(`Не удалось обновить "${targetLabels[target]}": неизвестная ошибка`, "error");
                                }
                            } else {
                                showToast(`Не удалось обновить "${targetLabels[target]}": ${message}`, "error");
                            }

                            field.disabled = false;
                            button.textContent = 'Сохранить';
                        });
                    }
                });
            });

            setTimeout(() => {
                window.addEventListener('click', outsideClickListener);
            }, 0);

            // Выполняем запрос к серверу, чтобы получить email текущего пользователя
            fetch('/current-user-email')
                .then(response => response.text())  // Получаем email как строку
                .then(email => {
                    // Если email получен, используем его
                    console.log(email);  // Можно заменить на логику, где используется email

                    // Теперь можно передавать email в функцию, как ранее
                    initTaskCommentsSection(task.id, content, email);
                    initCommentInputSection(task.id, content, () => initTaskCommentsSection(task.id, content, email), csrfToken);
                    initTaskFilesSection(task.id, content, csrfToken, email);
                })
                .catch(error => console.error('Error:', error));  // Обрабатываем возможные ошибки

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

function formatDateTimeLocal(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const offset = date.getTimezoneOffset();
    const localDate = new Date(date.getTime() - offset * 60 * 1000);
    return localDate.toISOString().slice(0, 16);
}

function createTagsSection() {
    const tagsSection = document.createElement('div');
    tagsSection.className = 'task-tags-section';

    const tagsTitle = document.createElement('h3');
    tagsTitle.textContent = 'Тэги';
    tagsSection.appendChild(tagsTitle);

    const tagList = document.createElement('div');
    tagList.className = 'task-tag-list';
    tagsSection.appendChild(tagList);

    const addTagContainer = document.createElement('div');
    addTagContainer.className = 'add-task-tag';

    const tagSelect = document.createElement('select');
    tagSelect.className = 'tag-select';

    const addTagButton = document.createElement('button');
    addTagButton.textContent = 'Добавить тэг';

    addTagContainer.appendChild(tagSelect);
    addTagContainer.appendChild(addTagButton);
    tagsSection.appendChild(addTagContainer);

    return { tagsSection, tagList, tagSelect, addTagButton };
}

function loadTaskTags(task, tagList, csrfToken, callback) {
    fetch(`/api/taskstags/task/${task.id}`)
        .then(res => res.json())
        .then(taskTags => {
            tagList.innerHTML = '';
            if (taskTags.length === 0) {
                tagList.textContent = 'Нет тэгов';
                callback?.([]);
                return;
            }

            const ids = [];

            taskTags.forEach(t => {
                ids.push(t.id);

                const tagItem = document.createElement('div');
                tagItem.className = 'task-tag-item';
                tagItem.textContent = t.name;

                const removeBtn = document.createElement('button');
                removeBtn.textContent = '✖';
                removeBtn.onclick = async () => {
                    try {
                        const response = await fetch(`/api/taskstags/${task.id}/${t.id}`, {
                            method: 'DELETE',
                            headers: { 'X-CSRF-TOKEN': csrfToken }
                        });

                        if (!response.ok) {
                            const errorMessage = await response.text();
                            const hasEnglish = /[a-zA-Z]/.test(errorMessage);

                            if (hasEnglish) {
                                showToast("Не удалось удалить тэг: неизвестная ошибка", "error");
                            } else {
                                showToast("Не удалось удалить тэг: " + errorMessage, "error");
                            }
                            return;
                        }

                        showToast("Тэг успешно удалён");
                        showTaskDetails(task);
                    } catch (error) {
                        const message = error.message || '';
                        const hasEnglish = /[a-zA-Z]/.test(message);

                        if (hasEnglish) {
                            if (message === 'Failed to fetch') {
                                showToast("Не удалось удалить тэг: проверьте подключение к интернету или попробуйте позже", "error");
                            } else {
                                showToast("Не удалось удалить тэг: неизвестная ошибка", "error");
                            }
                        } else {
                            showToast("Не удалось удалить тэг: " + message, "error");
                        }
                    }
                };

                tagItem.appendChild(removeBtn);
                tagList.appendChild(tagItem);
            });

            callback?.(ids);
        });
}

function loadProjectTags(task, tagSelect) {
    fetch(`/api/tags/project/${task.idProject.id}`)
        .then(res => res.json())
        .then(projectTags => {
            tagSelect.innerHTML = '';
            const defaultOption = document.createElement('option');
            defaultOption.textContent = 'Выберите тэг...';
            defaultOption.value = '';
            tagSelect.appendChild(defaultOption);

            projectTags.forEach(t => {
                const opt = document.createElement('option');
                opt.value = t.id;
                opt.textContent = t.name;
                tagSelect.appendChild(opt);
            });
        });
}

function setupAddTagButton(task, tagSelect, addTagButton, csrfParam, csrfToken, getCurrentTagIds) {
    addTagButton.onclick = async () => {
        const tagId = tagSelect.value;
        if (!tagId) return;

        const currentTags = getCurrentTagIds();
        if (currentTags.includes(Number(tagId))) {
            showToast('Этот тэг уже прикреплён к задаче.', 'error');
            return;
        }

        const formData = new URLSearchParams();
        formData.append('tagId', tagId);
        formData.append(csrfParam, csrfToken);

        try {
            const response = await fetch(`/api/taskstags/${task.id}/add`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            });

            if (!response.ok) {
                const errorMessage = await response.text();
                const hasEnglish = /[a-zA-Z]/.test(errorMessage);

                if (hasEnglish) {
                    showToast("Не удалось добавить тэг: неизвестная ошибка", "error");
                } else {
                    showToast("Не удалось добавить тэг: " + errorMessage, "error");
                }
                return;
            }

            showToast("Тэг успешно добавлен");
            showTaskDetails(task);
        } catch (error) {
            const message = error.message || '';
            const hasEnglish = /[a-zA-Z]/.test(message);

            if (hasEnglish) {
                if (message === 'Failed to fetch') {
                    showToast("Не удалось добавить тэг: проверьте подключение к интернету или попробуйте позже", "error");
                } else {
                    showToast("Не удалось добавить тэг: неизвестная ошибка", "error");
                }
            } else {
                showToast("Не удалось добавить тэг: " + message, "error");
            }
        }
    };
}

function initTaskTagsSection(task, csrfParam, csrfToken, content) {
    const { tagsSection, tagList, tagSelect, addTagButton } = createTagsSection();
    content.appendChild(tagsSection);

    let currentTaskTagIds = [];

    loadTaskTags(task, tagList, csrfToken, (tagIds) => {
        currentTaskTagIds = tagIds;
    });
    loadProjectTags(task, tagSelect);
    setupAddTagButton(task, tagSelect, addTagButton, csrfParam, csrfToken, () => currentTaskTagIds);

}

window.showTaskDetails = showTaskDetails;