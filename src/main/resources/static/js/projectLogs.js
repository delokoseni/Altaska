import { showToast } from './toast.js';

const fieldTranslations = {
    name: 'Название',
    description: 'Описание',
    status: 'Статус',
    priority: 'Приоритет',
    startTime: 'Время начала задачи',
    archived: 'Архивирован',
    permissions: 'Права',
    role: 'Роль',
    content: 'Содержание',
    deadline: 'Дедлайн',
};

const permissionLabels = {
    "add_task_tags": "Добавление тегов к задаче",
    "remove_task_tags": "Удаление тегов из задачи",
    "add_task_performers": "Назначение исполнителей задачи",
    "remove_task_performers": "Удаление исполнителей задачи",
    "accept_tasks": "Взятие задачи в работу",
    "reject_tasks": "Отказ от выполнения задачи",
    "create_tasks": "Создание задач",
    "edit_task_title": "Редактирование названия задачи",
    "edit_task_description": "Редактирование описания задачи",
    "edit_task_priority": "Изменение приоритета задачи",
    "edit_task_status": "Изменение статуса задачи",
    "edit_task_deadline": "Изменение срока выполнения",
    "edit_task_start_date": "Изменение даты начала задачи",
    "delete_tasks": "Удаление задач",
    "attach_task_files": "Прикрепление файлов к задаче",
    "download_task_files": "Загрузка файлов из задачи",
    "delete_task_files": "Удаление прикреплённых файлов",
    "write_task_comments": "Написание комментариев",
    "edit_task_comments": "Редактирование комментариев",
    "delete_task_comments": "Удаление комментариев",
    "create_gantt_chart": "Создание диаграммы Ганта",
    "edit_project_title_description": "Редактирование названия и описания проекта",
    "archive_project": "Архивирование проекта",
    "change_user_roles": "Назначение ролей участникам",
    "invite_project_members": "Приглашение участников в проект",
    "remove_project_members": "Удаление участников из проекта",
    "delete_project": "Удаление проекта",
    "create_roles": "Создание ролей",
    "delete_roles": "Удаление ролей",
    "edit_roles": "Редактирование ролей",
    "create_subtasks": "Создание подзадач",
    "edit_subtasks": "Редактирование подзадач",
    "delete_subtasks": "Удаление подзадач",
    "create_tags": "Создание тегов проекта",
    "delete_tags": "Удаление тегов проекта",
    "edit_tags": "Редактирование тегов проекта",
    "view_project_log": "Просмотр истории проекта"
};

function formatPermissions(jsonValue) {
    let permissions;
    try {
        permissions = typeof jsonValue === 'string' ? JSON.parse(jsonValue) : jsonValue;
    } catch (e) {
        return 'Ошибка разбора JSON';
    }

    if (typeof permissions !== 'object' || permissions === null) {
        return 'Недопустимый формат прав';
    }

    const lines = Object.entries(permissions)
        .filter(([key]) => permissionLabels[key])
        .map(([key, value]) => `- ${permissionLabels[key]}: ${value === true ? 'Да' : 'Нет'}`);

    return lines.length > 0 ? lines.join('\n') : 'Нет данных';
}

export function loadProjectLogsView(projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';
    const wrapper = document.createElement('div');
    wrapper.className = 'project-logs-view';
    viewContent.appendChild(wrapper);

    fetch(`/api/logs/project/${projectId}`)
        .then(async response => {
            if (!response.ok) {
                const errorText = await response.text();
                showToast(errorText || 'Ошибка при загрузке истории.', 'error');
                const errorMsg = document.createElement('p');
                errorMsg.textContent = errorText || 'Ошибка при загрузке истории.';
                wrapper.appendChild(errorMsg);
                return null;
            }
            return response.json();
        })
        .then(logs => {
            if (!logs) return;

            if (logs.length === 0) {
                const noLogs = document.createElement('p');
                noLogs.textContent = 'История пуста.';
                wrapper.appendChild(noLogs);
                return;
            }

            const logList = document.createElement('ul');
            logList.className = 'activity-log-list';

            logs.forEach(log => {
                const item = document.createElement('li');
                item.className = 'activity-log-item';

                const date = new Date(log.activityDate).toLocaleString();
                const user = log.idUser?.email || 'Неизвестный пользователь';
                const details = log.details;

                const summary = document.createElement('div');
                summary.className = 'activity-log-summary';
                summary.textContent = `[${date}] ${user} - ${details.message ? `${details.message}` : ''}`;
                item.appendChild(summary);

                const detailsSection = document.createElement('div');
                detailsSection.className = 'activity-log-details';

                if (details.changes && Array.isArray(details.changes) && details.changes.length > 0) {
                    details.changes.forEach(change => {
                        const changeBlock = document.createElement('div');
                        changeBlock.className = 'log-change-block';

                        const field = document.createElement('div');
                        field.className = 'log-change-field';
                        const translatedField = fieldTranslations[change.field] || change.field;
                        field.textContent = translatedField;

                        const oldPre = document.createElement('pre');
                        oldPre.textContent = 'Было:\n' + formatValue(change.old, change.field);
                        oldPre.className = 'log-block-old';

                        const newPre = document.createElement('pre');
                        newPre.textContent = 'Стало:\n' + formatValue(change.new, change.field);
                        newPre.className = 'log-block-new';

                        changeBlock.appendChild(field);
                        changeBlock.appendChild(oldPre);
                        changeBlock.appendChild(newPre);
                        detailsSection.appendChild(changeBlock);
                    });
                } else {
                    detailsSection.textContent = 'Нет дополнительной информации.';
                }

                item.appendChild(detailsSection);
                logList.appendChild(item);

                summary.addEventListener('click', (e) => {
                    e.stopPropagation();
                    item.classList.toggle('expanded');
                });
            });

            wrapper.appendChild(logList);

            document.addEventListener('click', (e) => {
                document.querySelectorAll('.activity-log-item.expanded').forEach(el => {
                    if (!el.contains(e.target)) {
                        el.classList.remove('expanded');
                    }
                });
            });
        })
        .catch(error => {
            console.error('Ошибка загрузки логов:', error);
            showToast('Ошибка загрузки истории.', 'error');
            const errorText = document.createElement('p');
            errorText.textContent = 'Ошибка загрузки истории.';
            wrapper.appendChild(errorText);
        });
}

function formatValue(value, field = '') {
    if (value === null || value === '') return '';
    if (value === true) return 'Да';
    if (value === false) return 'Нет';
    if (field === 'permissions') {
        return formatPermissions(value);
    }
    return typeof value === 'string' ? value : JSON.stringify(value, null, 2);
}

