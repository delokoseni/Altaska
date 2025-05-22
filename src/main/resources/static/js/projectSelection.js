import { renderAddMemberForm } from './renderAddMemberForm.js';
import { removeProjectMember } from './memberActions.js';
import { deleteProject } from './projectActions.js';
import { createSubtasksSection } from './subTasks.js';
import { addPerformer } from './taskPerformers.js';
import { deleteTask } from './deleteTask.js';
import { renderKanbanFiltersAndBoard, renderKanbanBoard } from './kanban.js';
import { renderGanttChart } from './gantt.js';
import { loadProjectLogsView } from './projectLogs.js';
import { showToast } from './toast.js';
import { showTaskLogsSidebar } from './taskLogSidebar.js'

window.selectProject = selectProject;
window.loadView = loadView;

function selectProject(buttonElement) {
    const projectId = buttonElement.dataset.projectId;

    // Удалить выделение со всех кнопок
    document.querySelectorAll('.project-button').forEach(btn => {
        btn.classList.remove('active-project-button');
    });

    // Выделить текущую кнопку
    buttonElement.classList.add('active-project-button');

    // Сохранить выбор
    sessionStorage.setItem('currentProjectId', projectId);

    const projectName = buttonElement.textContent.trim();
    sessionStorage.setItem('currentProjectName', projectName);

    const mainContent = document.querySelector('.main-content');
    mainContent.innerHTML = '';

    const menu = document.createElement('div');
    menu.className = 'project-menu';

    const views = ['Список', 'Канбан', 'Гант', 'О проекте', 'История'];
    views.forEach(view => {
        const button = document.createElement('button');
        button.textContent = view;
        button.onclick = function () {
            loadView(view.toLowerCase(), projectId);
        };
        menu.appendChild(button);
    });

    const viewContent = document.createElement('div');
    viewContent.className = 'view-content';

    mainContent.appendChild(menu);
    mainContent.appendChild(viewContent);
    loadView('список', projectId);
}

function goToMainPage() {
    sessionStorage.removeItem('currentProjectId');
    sessionStorage.removeItem('currentProjectName');
    const mainContent = document.querySelector('.main-content');
    mainContent.innerHTML = '';
    const title = document.createElement('h2');
    title.textContent = 'Главная';
    title.style.marginBottom = '12px';
    mainContent.appendChild(title);
    const menu = document.createElement('div');
    menu.className = 'project-menu';
    ['Список', 'Канбан'].forEach(view => {
        const button = document.createElement('button');
        button.textContent = view;
        button.onclick = () => loadMainView(view.toLowerCase());
        menu.appendChild(button);
    });
    const viewContent = document.createElement('div');
    viewContent.className = 'view-content';
    mainContent.append(menu, viewContent);
    loadMainView('список');
}

function loadMainView(view) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';

    if (view === 'список') {
        renderMainAssignedTasks(viewContent);
    }

    if (view === 'канбан') {
        renderKanbanForAssignedTasks(viewContent);
    }
}

function getAssignedTasks() {
    return fetch('/api/tasks/assigned')
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при получении назначенных задач');
            }
            return response.json();
        })
        .then(tasks => {
            console.log('Полученные назначенные задачи:', tasks);
            return tasks;
        })
        .catch(error => {
            console.error('Ошибка загрузки назначенных задач:', error);
            showToast('Ошибка загрузки назначенных задач', 'error');
            return [];
        });
}

// Основная функция для отображения канбана с назначенными задачами
export function renderKanbanForAssignedTasks(container) {
    Promise.all([
        getAssignedTasks(),
        fetch('/api/priorities').then(r => r.json()),
        fetch('/api/statuses').then(r => r.json())
    ])
    .then(([tasks, priorities, statuses]) => {
        container.innerHTML = '';

        // Контейнер для фильтров (только группировка)
        const filterContainer = document.createElement('div');
        filterContainer.className = 'kanban-filters';
        container.appendChild(filterContainer);

        const header = document.createElement('div');
        header.className = 'task-header';
        const title = document.createElement('span');
        title.textContent = tasks.length === 0 ? 'Нет назначенных задач' : 'Мои задачи';
        header.appendChild(title);
        container.appendChild(header);

        // Селектор группировки: по статусу или приоритету
        const groupBySelectorContainer = document.createElement('div');
        groupBySelectorContainer.className = 'kanban-filter';

        const groupByLabel = document.createElement('label');
        groupByLabel.textContent = 'Группировать по: ';
        groupBySelectorContainer.appendChild(groupByLabel);

        const groupBySelect = document.createElement('select');
        groupBySelect.className = 'kanban-filter-select group-by-selector';
        const options = [
            { value: 'status', text: 'Статусу' },
            { value: 'priority', text: 'Приоритету' }
        ];
        options.forEach(({ value, text }) => {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = text;
            groupBySelect.appendChild(option);
        });

        groupBySelectorContainer.appendChild(groupBySelect);
        filterContainer.appendChild(groupBySelectorContainer);

        // Канбан-доска
        const kanbanBoard = document.createElement('div');
        kanbanBoard.className = 'kanban-board';
        container.appendChild(kanbanBoard);

        function updateBoard() {
            renderKanbanBoard(kanbanBoard, groupBySelect.value, tasks, statuses, priorities, updateBoard);
        }

        groupBySelect.addEventListener('change', updateBoard);
        updateBoard();
    })
    .catch(error => {
        console.error('Ошибка при загрузке данных для канбан-доски назначенных задач:', error);
        showToast('Ошибка загрузки данных для канбан-доски', 'error');
    });
}

function renderMainAssignedTasks(viewContent) {
    fetch('/api/tasks/assigned')
        .then(res => res.json())
        .then(tasks => {
            const allTasks = [...tasks];
            const container = document.createElement('div');
            container.className = 'task-list';

            const header = document.createElement('div');
            header.className = 'task-header';

            const title = document.createElement('span');
            title.textContent = tasks.length === 0 ? 'Нет задач' : 'Мои задачи:';
            header.appendChild(title);

            const taskCount = document.createElement('span');
            taskCount.style.marginLeft = '12px';
            header.appendChild(taskCount);

            container.appendChild(header);

            const filtersContainer = document.createElement('div');
            filtersContainer.className = 'task-filters';

            const statusSelect = document.createElement('select');
            statusSelect.innerHTML = `
                <option value="">Все статусы</option>
                ${[...new Set(allTasks.map(t => t.idStatus?.name))]
                    .filter(Boolean)
                    .map(name => `<option value="${name}">${name}</option>`).join('')}
            `;

            const prioritySelect = document.createElement('select');
            prioritySelect.innerHTML = `
                <option value="">Все приоритеты</option>
                ${[...new Set(allTasks.map(t => t.idPriority?.name))]
                    .filter(Boolean)
                    .map(name => `<option value="${name}">${name}</option>`).join('')}
            `;

            [statusSelect, prioritySelect].forEach(select => {
                select.className = 'task-filter-select';
                select.style.marginRight = '10px';
                filtersContainer.appendChild(select);
            });

            container.appendChild(filtersContainer);

            const taskList = document.createElement('div');

            container.appendChild(taskList);
            viewContent.appendChild(container);

            function applyFilters() {
                const status = statusSelect.value;
                const priority = prioritySelect.value;

                let filtered = [...allTasks];

                if (status) {
                    filtered = filtered.filter(t => t.idStatus?.name === status);
                }
                if (priority) {
                    filtered = filtered.filter(t => t.idPriority?.name === priority);
                }

                taskList.innerHTML = '';

                if (filtered.length === 0) {
                    taskList.textContent = 'Нет задач по выбранным фильтрам';
                } else {
                    filtered.forEach(task => {
                        const taskDiv = document.createElement('div');
                        taskDiv.className = 'task-item';

                        taskDiv.innerHTML = `
                            <h3>${task.name}</h3>
                            <p>Проект: ${task.idProject?.name || 'Без проекта'}</p>
                            <p>Статус: ${task.idStatus.name} | Приоритет: ${task.idPriority?.name || 'Без приоритета'}</p>
                        `;

                        taskDiv.addEventListener('click', () => showTaskDetails(task));
                        taskList.appendChild(taskDiv);
                    });
                }

                taskCount.textContent = `${filtered.length} / ${allTasks.length}`;
            }

            statusSelect.addEventListener('change', applyFilters);
            prioritySelect.addEventListener('change', applyFilters);

            applyFilters(); // Инициализация
        })
        .catch(err => {
            console.error('Ошибка при загрузке задач:', err);
            viewContent.textContent = 'Ошибка при загрузке задач';
        });
}

function toggleArchived() {
    const archived = document.getElementById('archived-projects');
    const title = document.querySelector('.project-section-title.collapsible');
    if (archived.style.display === 'none') {
        archived.style.display = 'block';
        title.textContent = 'Архивированные проекты ▲';
    } else {
        archived.style.display = 'none';
        title.textContent = 'Архивированные проекты ▼';
    }
}

window.goToMainPage = goToMainPage;
window.toggleArchived = toggleArchived;

function loadView(view, projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';

    if (view === 'список') {
        renderTaskListView(projectId, viewContent);
    }
    if (view === 'канбан') {
        renderKanbanFiltersAndBoard(projectId, showTaskForm);
    }
    if (view === 'гант') {
        renderGanttChart(projectId);
    }
    if (view === 'о проекте')
    {
        loadProjectInfoView(projectId);
    }
    if (view === 'история')
    {
        loadProjectLogsView(projectId);
    }
}

function renderTaskListView(projectId, viewContent) {
    let tasks = [];
    let tagsWithTasks = [];
    let performersMap = {};
    let taskCountInfo;
    let currentFilters = { status: '', priority: '', tag: '', performer: '', sort: 'createdAt-desc' };

    Promise.all([
        fetch(`/api/tasks/project/${projectId}`).then(res => res.json()),
        fetch(`/api/taskstags/project/${projectId}/tags-with-tasks`).then(res => res.json()),
        fetch(`/api/tasks/project/${projectId}/performers-map`).then(res => res.json())
    ]).then(([loadedTasks, loadedTags, loadedPerformers]) => {
        tasks = loadedTasks;
        tagsWithTasks = loadedTags;
        performersMap = loadedPerformers;

        tasks.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        const taskListContainer = document.createElement('div');
        taskListContainer.className = 'task-list';

        const header = document.createElement('div');
        header.className = 'task-header';

        const title = document.createElement('span');
        title.textContent = tasks.length === 0 ? 'Нет задач' : 'Задачи';
        header.appendChild(title);

        const addBtn = document.createElement('button');
        addBtn.className = 'add-project-button';
        addBtn.appendChild(document.createTextNode('+'));

        const addLabel = document.createElement('span');
        addLabel.className = 'add-label';
        addLabel.textContent = 'Добавить';
        addBtn.appendChild(addLabel);

        addBtn.onclick = () => showTaskForm(projectId, taskListContainer);
        header.appendChild(addBtn);

        taskListContainer.appendChild(header);

        const filteredContainer = document.createElement('div');

        const renderFilteredTasks = (filters) => {
            let filtered = [...tasks];

            if (filters.status) {
                filtered = filtered.filter(t => t.idStatus?.name === filters.status);
            }
            if (filters.priority) {
                filtered = filtered.filter(t => t.idPriority?.name === filters.priority);
            }
            if (filters.tag) {
                const tagObj = tagsWithTasks.find(tag => tag.tagName === filters.tag);
                if (tagObj) {
                    const allowedTaskIds = new Set(tagObj.taskIds);
                    filtered = filtered.filter(t => allowedTaskIds.has(t.id));
                } else {
                    filtered = [];
                }
            }
            if (filters.performer) {
                if (filters.performer === '__none__') {
                    filtered = filtered.filter(t => !performersMap[t.id] || performersMap[t.id].length === 0);
                } else {
                    const allowedTaskIds = Object.entries(performersMap)
                        .filter(([taskId, performers]) => performers.includes(filters.performer))
                        .map(([taskId]) => parseInt(taskId));
                    const allowedSet = new Set(allowedTaskIds);
                    filtered = filtered.filter(t => allowedSet.has(t.id));
                }
            }

            if (filters.sort) {
                const [field, order] = filters.sort.split('-');
                filtered.sort((a, b) => {
                    let valA, valB;

                    if (field === 'createdAt') {
                        valA = new Date(a.createdAt);
                        valB = new Date(b.createdAt);
                    } else if (field === 'priority') {
                        valA = a.idPriority?.id || 0;
                        valB = b.idPriority?.id || 0;
                    } else if (field === 'deadline') {
                        valA = a.deadlineServer ? new Date(a.deadlineServer) : new Date(8640000000000000);
                        valB = b.deadlineServer ? new Date(b.deadlineServer) : new Date(8640000000000000);
                    }

                    return order === 'asc' ? valA - valB : valB - valA;
                });
            }

            filteredContainer.innerHTML = '';
            if (filtered.length === 0) {
                filteredContainer.textContent = 'Нет задач по выбранным критериям';
            } else {
                filtered.forEach(task => {
                    const taskDiv = document.createElement('div');
                    taskDiv.setAttribute('data-task-id', task.id);
                    taskDiv.className = 'task-item';

                    const contentWrapper = document.createElement('div');
                    contentWrapper.className = 'task-content';

                    const textContainer = document.createElement('div');
                    const performers = performersMap[task.id]?.join(', ') || 'Нет исполнителей';

                    textContainer.innerHTML = `
                        <h3>${task.name}</h3>
                        <p>Статус: ${task.idStatus.name} | Приоритет: ${task.idPriority?.name || 'Без приоритета'}</p>
                        <p>Исполнители: ${performers}</p>
                    `;

                    const logIcon = document.createElement('img');
                    logIcon.src = '/icons/log.svg';
                    logIcon.alt = 'Лог задачи';
                    logIcon.className = 'delete-task-icon';
                    logIcon.width = 24;
                    logIcon.height = 24;

                    logIcon.onclick = (e) => {
                        e.stopPropagation();
                        showTaskLogsSidebar(task)
                    };

                    const deleteIcon = document.createElement('img');
                    deleteIcon.src = '/icons/trash.svg';
                    deleteIcon.alt = 'Удалить задачу';
                    deleteIcon.className = 'delete-task-icon';
                    deleteIcon.width = 24;
                    deleteIcon.height = 24;

                    deleteIcon.onclick = (e) => {
                        e.stopPropagation();
                        if (confirm('Вы уверены, что хотите удалить эту задачу?')) {
                            deleteTask(task.id, csrfToken, () => {
                                tasks = tasks.filter(t => t.id !== task.id);
                                const status = taskListContainer.querySelector('.status-select')?.value || '';
                                const priority = taskListContainer.querySelector('.priority-select')?.value || '';
                                const tag = taskListContainer.querySelector('.tag-select')?.value || '';
                                const performer = taskListContainer.querySelector('.performer-select')?.value || '';
                                const sort = taskListContainer.querySelector('.sort-select')?.value || 'createdAt-desc';
                                renderFilteredTasks({ status, priority, tag, performer, sort });
                            });
                        }
                    };

                    contentWrapper.appendChild(textContainer);

                    const iconContainer = document.createElement('div');
                    iconContainer.className = 'icon-container';
                    iconContainer.appendChild(logIcon);
                    iconContainer.appendChild(deleteIcon);

                    contentWrapper.appendChild(iconContainer);

                    taskDiv.appendChild(contentWrapper);

                    taskDiv.addEventListener('click', () => {
                        showTaskDetails(task);
                    });

                    filteredContainer.appendChild(taskDiv);
                });
            }

            updateTaskCountInfo(filtered, tasks);
        };

        // Добавим taskCountInfo через замыкание
        const renderTaskFiltersResult = renderTaskFilters(tasks, tagsWithTasks, performersMap, taskListContainer, renderFilteredTasks);
        taskCountInfo = renderTaskFiltersResult.taskCountInfo;

        taskListContainer.appendChild(filteredContainer);

        renderFilteredTasks({ status: '', priority: '', tag: '', performer: '', sort: 'createdAt-desc' });

        viewContent.appendChild(taskListContainer);
    }).catch(error => {
        console.error('Ошибка загрузки задач или тегов/исполнителей:', error);
    });
}

function renderTaskFilters(tasks, tagsWithTasks, performersMap, container, onFilterChange) {
    const filtersContainer = document.createElement('div');
    filtersContainer.className = 'task-filters';

    const statusSelect = document.createElement('select');
    statusSelect.innerHTML = `
        <option value="">Все статусы</option>
        ${[...new Set(tasks.map(t => t.idStatus?.name))].filter(Boolean).map(name => `<option value="${name}">${name}</option>`).join('')}
    `;

    const prioritySelect = document.createElement('select');
    prioritySelect.innerHTML = `
        <option value="">Все приоритеты</option>
        ${[...new Set(tasks.map(t => t.idPriority?.name))].filter(Boolean).map(name => `<option value="${name}">${name}</option>`).join('')}
    `;

    const tagSelect = document.createElement('select');
    tagSelect.innerHTML = `
        <option value="">Все теги</option>
        ${tagsWithTasks.map(tag => `<option value="${tag.tagName}">${tag.tagName}</option>`).join('')}
    `;

    const performerSelect = document.createElement('select');
    performerSelect.innerHTML = `
        <option value="">Все исполнители</option>
        <option value="__none__">Без исполнителей</option>
        ${[...new Set(Object.values(performersMap).flat())]
            .filter(Boolean)
            .map(name => `<option value="${name}">${name}</option>`).join('')}
    `;

    const sortSelect = document.createElement('select');
    sortSelect.innerHTML = `
        <option value="createdAt-desc">Сначала новые</option>
        <option value="createdAt-asc">Сначала старые</option>
        <option value="priority-asc">Приоритет ↑</option>
        <option value="priority-desc">Приоритет ↓</option>
        <option value="deadline-asc">Ближайший срок</option>
        <option value="deadline-desc">Самый отдалённый срок</option>
    `;

    [statusSelect, prioritySelect, tagSelect, performerSelect, sortSelect].forEach(select => {
        select.className = 'task-filter-select';
        select.addEventListener('change', () => {
            const filters = {
                status: statusSelect.value,
                priority: prioritySelect.value,
                tag: tagSelect.value,
                performer: performerSelect.value,
                sort: sortSelect.value
            };
            onFilterChange(filters);
        });
    });

    function createFilterBlock(labelText, selectElement) {
        const wrapper = document.createElement('div');
        wrapper.style.display = 'flex';
        wrapper.style.alignItems = 'center';
        wrapper.style.gap = '5px';

        const label = document.createElement('span');
        label.textContent = labelText;

        wrapper.append(label, selectElement);
        return wrapper;
    }

    const firstRow = document.createElement('div');
    firstRow.style.display = 'flex';
    firstRow.style.gap = '15px';
    firstRow.style.marginBottom = '8px';

    firstRow.append(
        createFilterBlock('Статус:', statusSelect),
        createFilterBlock('Приоритет:', prioritySelect),
        createFilterBlock('Тег:', tagSelect)
    );

    const secondRow = document.createElement('div');
    secondRow.style.display = 'flex';
    secondRow.style.gap = '15px';

    const taskCountInfo = document.createElement('div');
    taskCountInfo.classList.add('task-count-info');
    taskCountInfo.textContent = `Количество задач: ${tasks.length} / ${tasks.length}`;

    secondRow.append(
        createFilterBlock('Исполнитель:', performerSelect),
        createFilterBlock('Сортировка:', sortSelect),
        taskCountInfo
    );

    filtersContainer.innerHTML = '';
    filtersContainer.append(firstRow, secondRow);

    container.appendChild(filtersContainer);

    return { taskCountInfo };
}

function updateTaskCountInfo(filteredTasks, allTasks) {
    const countBlock = document.querySelector('.task-count-info');
    if (countBlock) {
        countBlock.textContent = `Количество задач: ${filteredTasks.length} / ${allTasks.length}`;
    }
}

function showTaskForm(projectId, container, from = 'список') {
    const existingForm = container.querySelector('.task-form');
    const previousContent = container.innerHTML;
    if (existingForm) return;
    container.innerHTML = '';

    fetch('/api/priorities')
        .then(response => response.json())
        .then(priorities => {
            return Promise.all([
                priorities,
                fetch(`/api/projects/${projectId}/confirmed-members`).then(res => res.json())
            ]);
        })
        .then(([priorities, members]) => {
            const wrapper = document.createElement('div');
            wrapper.classList.add('task-form-wrapper');

            const form = document.createElement('form');
            form.className = 'task-form';

            const createdAtInput = document.createElement('input');
            createdAtInput.type = 'hidden';
            createdAtInput.name = 'createdAt';

            const updatedAtInput = document.createElement('input');
            updatedAtInput.type = 'hidden';
            updatedAtInput.name = 'updatedAt';

            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = csrfParam;
            csrfInput.value = csrfToken;

            form.append(createdAtInput, updatedAtInput, csrfInput);

            const nameInput = document.createElement('input');
            nameInput.type = 'text';
            nameInput.placeholder = 'Название задачи';
            nameInput.name = 'name';
            nameInput.required = true;

            const descriptionInput = document.createElement('textarea');
            descriptionInput.placeholder = 'Описание задачи';
            descriptionInput.name = 'description';

            const startTimeLabel = document.createElement('h3');
            startTimeLabel.textContent = 'Дата начала';
            const startTimeInput = document.createElement('input');
            startTimeInput.type = 'datetime-local';
            startTimeInput.name = 'startTime';
            startTimeInput.placeholder = 'Начало выполнения';

            const deadlineLabel = document.createElement('h3');
            deadlineLabel.textContent = 'Срок выполнения';
            const deadlineInput = document.createElement('input');
            deadlineInput.type = 'datetime-local';
            deadlineInput.name = 'deadline';
            deadlineInput.placeholder = 'Срок выполнения';

            const prioritySelect = document.createElement('select');
            prioritySelect.name = 'priorityId';
            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = 'Без приоритета';
            prioritySelect.appendChild(defaultOption);

            priorities.forEach(p => {
                const option = document.createElement('option');
                option.value = p.id;
                option.textContent = p.name;
                prioritySelect.appendChild(option);
            });

            const performerLabel = document.createElement('div');
            performerLabel.className = 'performer-container';
            performerLabel.textContent = 'Назначить исполнителей:';

            const performerCheckboxes = [];

            members.forEach(m => {
                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.value = m.userId;
                checkbox.name = 'performerIds';

                const label = document.createElement('label');
                label.appendChild(checkbox);
                label.append(` ${m.email}`);

                performerCheckboxes.push(checkbox);
                performerLabel.appendChild(label);
                performerLabel.appendChild(document.createElement('br'));
            });

            const selectedTagIds = [];
            const tagSelector = createTagSelector(projectId, selectedTagIds);

            const subtasksSection = createSubtasksSection();

            const filesInput = document.createElement('input');
            filesInput.type = 'file';
            filesInput.name = 'files';
            filesInput.multiple = false;

            const submitBtn = document.createElement('button');
            submitBtn.type = 'submit';
            submitBtn.textContent = 'Создать';

            const backButton = document.createElement('button');
            backButton.textContent = 'Назад';
            backButton.type = 'button';
            backButton.className = 'back-button';
            backButton.onclick = () => {
                loadView(from, projectId);
            };

            form.append(
                backButton,
                nameInput,
                descriptionInput,
                startTimeLabel,
                startTimeInput,
                deadlineLabel,
                deadlineInput,
                prioritySelect,
                tagSelector,
                performerLabel,
                subtasksSection,
                filesInput,
                submitBtn
            );

            form.onsubmit = function (e) {
                e.preventDefault();

                // Проверка файлов
                const maxFileSize = 25 * 1024 * 1024; // 25MB
                for (let file of filesInput.files) {
                    if (file.size > maxFileSize) {
                        alert(`Файл "${file.name}" превышает 25 МБ`);
                        return;
                    }
                }

                const now = new Date();
                createdAtInput.value = now.toISOString().split('T')[0];
                updatedAtInput.value = now.toISOString();

                const formData = new FormData(form);
                selectedTagIds.forEach(tagId => formData.append('tagIds', tagId));

                const subtaskElements = subtasksSection.querySelectorAll('.subtask-item');
                const subtasksToSend = [];
                subtaskElements.forEach(item => {
                    const name = item.querySelector('.subtask-name').value.trim();
                    const description = item.querySelector('.subtask-description').value.trim();
                    if (name) {
                        subtasksToSend.push({ name, description });
                    }
                });

                fetch(`/api/tasks/create/${projectId}`, {
                    method: 'POST',
                    body: formData
                })
                .then(async response => {
                    if (response.status === 403) {
                        const text = await response.text();
                        showToast(text || 'Недостаточно прав для создания задачи', 'error');
                        throw new Error('Нет прав');
                    }
                    if (!response.ok) {
                        const text = await response.text();
                        showToast(text || 'Ошибка при создании задачи', 'error');
                        throw new Error('Ошибка при создании задачи');
                    }
                    return response.json();
                })
                .then(data => {
                    const taskId = data.id;

                    const selectedUserIds = performerCheckboxes
                        .filter(cb => cb.checked)
                        .map(cb => cb.value);
                    const performerPromises = selectedUserIds.map(userId => addPerformer(taskId, userId, csrfToken));

                    const files = filesInput.files;
                    const uploadPromises = [];

                    for (let i = 0; i < files.length; i++) {
                        const formDataFile = new FormData();
                        formDataFile.append('file', files[i]);
                        formDataFile.append('taskId', taskId);

                        const uploadPromise = fetch('/api/files/upload', {
                            method: 'POST',
                            headers: {
                                'X-CSRF-TOKEN': csrfToken
                            },
                            body: formDataFile
                        });

                        uploadPromises.push(uploadPromise);
                    }

                    const subtaskPromise = subtasksToSend.length > 0
                        ? fetch(`/api/tasks/${taskId}/subtasks`, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'X-CSRF-TOKEN': csrfToken
                            },
                            body: JSON.stringify(subtasksToSend)
                        })
                        : Promise.resolve();

                    return Promise.all([
                        ...performerPromises,
                        subtaskPromise,
                        ...uploadPromises
                    ]);
                })
                .then(() => {
                    showToast('Задача успешно создана', 'success');
                    loadView(from, projectId);
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                })
                .catch(error => {
                    if (error.message === 'Нет прав') return;
                    console.error('Ошибка:', error);
                    showToast('Произошла ошибка при создании задачи', 'error');
                });
            };

            wrapper.appendChild(form);
            container.appendChild(wrapper);
        });
}

function loadProjectInfoView(projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';
    const wrapper = document.createElement('div');
    wrapper.className = 'project-info-view';
    viewContent.appendChild(wrapper);


    fetch(`/api/projects/${projectId}`)
        .then(response => response.json())
        .then(project => {
            const container = document.createElement('div');
            container.className = 'project-info-container';

            const nameGroup = document.createElement('div');
            const nameLabel = document.createElement('h3');
            nameLabel.textContent = 'Название проекта:';
            const nameInput = document.createElement('input');
            nameInput.type = 'text';
            nameInput.value = project.name;
            nameInput.disabled = true;

            const nameEditButton = document.createElement('button');
            nameEditButton.textContent = 'Изменить';

            nameGroup.appendChild(nameLabel);
            nameGroup.appendChild(nameInput);
            nameGroup.appendChild(nameEditButton);

            const descGroup = document.createElement('div');
            const descLabel = document.createElement('h3');
            descLabel.textContent = 'Описание проекта:';
            const descInput = document.createElement('textarea');
            descInput.value = project.description || '';
            descInput.disabled = true;

            const descEditButton = document.createElement('button');
            descEditButton.textContent = 'Изменить';

            nameEditButton.onclick = () => {
                if (nameInput.disabled) {
                    nameInput.disabled = false;
                    nameEditButton.textContent = 'Сохранить';
                } else {
                    nameInput.disabled = true;
                    nameEditButton.textContent = 'Изменить';
                    updateProjectField(projectId, 'name', nameInput.value);
                }
            };

            descEditButton.onclick = () => {
                if (descInput.disabled) {
                    descInput.disabled = false;
                    descEditButton.textContent = 'Сохранить';
                } else {
                    descInput.disabled = true;
                    descEditButton.textContent = 'Изменить';
                    updateProjectField(projectId, 'description', descInput.value);
                }
            };

            descGroup.appendChild(descLabel);
            descGroup.appendChild(descInput);
            descGroup.appendChild(descEditButton);

            container.appendChild(nameGroup);
            container.appendChild(descGroup);
            wrapper.appendChild(container);

            renderTagsSection(container, projectId);

            Promise.all([
                fetch(`/api/projects/${projectId}/members`).then(res => res.json()),
                fetch(`/api/projects/${projectId}/roles`).then(res => res.json())
            ]).then(([members, roles]) => {
                const membersSection = document.createElement('div');
                membersSection.className = 'project-members-section';

                const membersTitle = document.createElement('h3');
                membersTitle.textContent = 'Участники проекта:';
                membersSection.appendChild(membersTitle);

                if (members.length === 0) {
                    const noMembers = document.createElement('p');
                    noMembers.textContent = 'Нет участников';
                    membersSection.appendChild(noMembers);
                } else {
                    const membersList = document.createElement('ul');
                    members.forEach(member => {
                        const listItem = renderMemberItem(member, projectId, roles);
                        membersList.appendChild(listItem);
                    });
                    membersSection.appendChild(membersList);
                }

                const addMemberBtn = document.createElement('button');
                addMemberBtn.textContent = 'Добавить участника';
                addMemberBtn.onclick = () => {
                    renderAddMemberForm(container, projectId, roles, container.innerHTML, loadProjectInfoView, handleFetchWithToast);
                };
                membersSection.appendChild(addMemberBtn);

                container.appendChild(membersSection);
            }).catch(err => {
                console.error('Ошибка загрузки участников или ролей:', err);
            });
            renderRolesSection(container, projectId);
            const archiveButton = createArchiveToggleButton(project, projectId);
            container.appendChild(archiveButton);

            const leaveButton = createLeaveProjectButton(projectId);
            container.appendChild(leaveButton);

            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'Удалить проект';
            deleteButton.style.backgroundColor = 'red';
            deleteButton.style.color = 'white';
            deleteButton.style.marginTop = '20px';
            deleteButton.onclick = () => {
                deleteProject(projectId, handleFetchWithToast, () => {
                    window.location.reload();
                });
            };
            container.appendChild(deleteButton);
        })
        .catch(error => {
            console.error('Ошибка загрузки проекта:', error);
        });
}

function createLeaveProjectButton(projectId) {
    const button = document.createElement('button');
    button.textContent = 'Покинуть проект';
    button.classList.add('leave-project-button');

    button.addEventListener('click', async () => {
        if (!confirm('Вы уверены, что хотите покинуть проект?')) return;

        try {
            const response = await fetch(`/api/projects/${projectId}/members/leave`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                }
            });

            const result = await response.json();

            if (response.ok) {
                showToast('Вы покинули проект.', 'success');
                setTimeout(() => location.reload(), 1000);
            } else {
                showToast(result.message || 'Ошибка при выходе из проекта.', 'error');
            }
        } catch (error) {
            console.error(error);
            showToast('Ошибка при подключении к серверу.', 'error');
        }
    });

    return button;
}

export async function handleFetchWithToast(url, options, successMessage, errorMessagePrefix = "Ошибка") {
    try {
        const response = await fetch(url, options);
        const responseText = await response.text();

        if (!response.ok) {
            // Пробрасываем ошибку, но не показываем Toast тут — это сделает catch
            throw new Error(responseText || 'Неизвестная ошибка');
        }

        showToast(successMessage || "Операция выполнена успешно");
        return responseText ? JSON.parse(responseText) : null;
    } catch (error) {
        const message = error.message || '';
        const hasEnglish = /[a-zA-Z]/.test(message);
        if (hasEnglish) {
            if (message === 'Failed to fetch') {
                showToast(`${errorMessagePrefix}: нет подключения или сервер недоступен`, "error");
            } else {
                showToast(`${errorMessagePrefix}: неизвестная ошибка`, "error");
            }
        } else {
            showToast(`${errorMessagePrefix}: ${message}`, "error");
        }
        throw error;
    }
}

const fieldNames = {
    name: "название",
    description: "описание"
};

function updateProjectField(projectId, field, value) {
    const formData = new FormData();
    formData.append(field, value);
    formData.append(csrfParam, csrfToken);

    const humanField = fieldNames[field] || field;

    handleFetchWithToast(
        `/api/projects/update/${projectId}`,
        {
            method: 'POST',
            body: formData
        },
        `Поле «${humanField}» успешно обновлено`,
        `Ошибка при обновлении поля «${humanField}»`
    )
    .then(updated => {
        console.log(`Поле ${field} обновлено`, updated);
    })
    .catch(error => {
        console.error('Ошибка при обновлении поля проекта:', error);
    });
}

function renderTagsSection(container, projectId) {
    const tagsGroup = document.createElement('div');
    tagsGroup.className = 'tags-group';

    const tagsHeader = document.createElement('div');
    tagsHeader.className = 'tags-header';

    const tagsTitle = document.createElement('h3');
    tagsTitle.textContent = 'Список тэгов';

    const addTagButton = document.createElement('button');
    addTagButton.textContent = '+ Добавить тэг';
    addTagButton.onclick = () => showAddTagForm(tagsGroup, projectId);

    tagsHeader.appendChild(tagsTitle);
    tagsHeader.appendChild(addTagButton);
    tagsGroup.appendChild(tagsHeader);

    const tagList = document.createElement('div');
    tagList.className = 'tag-list';
    tagsGroup.appendChild(tagList);

    container.appendChild(tagsGroup);

    loadTags(tagList, projectId);
}

function loadTags(tagListElement, projectId) {
    fetch(`/api/tags/project/${projectId}`)
        .then(response => response.json())
        .then(tags => {
            tagListElement.innerHTML = '';
            tags.forEach(tag => {
                tagListElement.appendChild(createTagElement(tag, projectId));
            });
        });
}

function createTagElement(tag, projectId) {
    const tagItem = document.createElement('div');
    tagItem.className = 'tag-item';

    const tagName = document.createElement('span');
    tagName.textContent = tag.name;

    const editBtn = document.createElement('button');
    editBtn.className = 'tag-edit-button';
    editBtn.innerHTML = `<img src="/icons/pencil.svg" alt="Редактировать" class="icon">`;
    editBtn.onclick = () => editTag(tag.id, tag.name, projectId);

    const delBtn = document.createElement('button');
    delBtn.className = 'tag-delete-button';
    delBtn.innerHTML = `<img src="/icons/trash.svg" alt="Удалить" class="icon">`;
    delBtn.onclick = () => {
        handleFetchWithToast(
            `/api/tags/${tag.id}`,
            {
                method: 'DELETE',
                headers: { 'X-CSRF-TOKEN': csrfToken }
            },
            `Тег «${tag.name}» успешно удалён`,
            `Ошибка при удалении тега «${tag.name}»`
        )
        .then(() => loadProjectInfoView(projectId))
        .catch(error => {
            console.error('Ошибка при удалении тега:', error);
        });
    };

    tagItem.appendChild(tagName);
    tagItem.appendChild(editBtn);
    tagItem.appendChild(delBtn);

    return tagItem;
}

function showAddTagForm(container, projectId) {
    // Не добавлять форму повторно
    if (container.querySelector('.add-tag-form-wrapper')) return;

    const formWrapper = document.createElement('div');
    formWrapper.className = 'add-tag-form-wrapper';

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = 'Введите тэг';
    input.classList.add('add-tag');

    const saveBtn = document.createElement('button');
    saveBtn.textContent = 'Сохранить';
    saveBtn.classList.add('save-tag-button');

    const cancelBtn = document.createElement('button');
    cancelBtn.textContent = 'Отмена';
    cancelBtn.classList.add('cancel-tag-button');
    cancelBtn.onclick = () => formWrapper.remove();

    const submitHandler = () => {
        const tagName = input.value.trim();
        if (!tagName) return;

        const formData = new FormData();
        formData.append('name', tagName);
        formData.append(csrfParam, csrfToken);

        handleFetchWithToast(
            `/api/tags/create/${projectId}`,
            {
                method: 'POST',
                body: formData
            },
            'Тег успешно добавлен',
            'Не удалось добавить тег'
        ).then(() => {
            loadProjectInfoView(projectId);
        }).catch(error => {
            console.error('Ошибка при добавлении тега:', error);
        });
    };

    saveBtn.onclick = submitHandler;

    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            e.preventDefault();
            submitHandler();
        }
    });

    formWrapper.appendChild(input);
    formWrapper.appendChild(saveBtn);
    formWrapper.appendChild(cancelBtn);
    container.appendChild(formWrapper);
}

function editTag(tagId, currentName, projectId) {
    const newName = prompt('Новое имя тега:', currentName);
    if (newName && newName !== currentName) {
        const formData = new FormData();
        formData.append('name', newName.trim());
        formData.append(csrfParam, csrfToken);

        handleFetchWithToast(
            `/api/tags/update/${tagId}`,
            {
                method: 'POST',
                body: formData
            },
            'Тег успешно обновлён',
            'Не удалось обновить тег'
        )
        .then(() => loadProjectInfoView(projectId))
        .catch(error => {
            console.error('Ошибка при обновлении тега:', error);
        });
    }
}

function createTagSelector(projectId, selectedTags) {
    const container = document.createElement('div');
    container.className = 'tag-selector-container';

    const tagSelect = document.createElement('select');
    tagSelect.className = 'tag-select';

    const addBtn = document.createElement('button');
    addBtn.type = 'button';
    addBtn.textContent = 'Добавить тег';

    const tagsList = document.createElement('div');
    tagsList.className = 'selected-tags';

    // Храним id выбранных тегов, чтобы не дублировались
    const selectedTagIds = new Set();

    fetch(`/api/tags/project/${projectId}`)
        .then(response => response.json())
        .then(tags => {
            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = 'Выберите тег';
            tagSelect.appendChild(defaultOption);

            tags.forEach(tag => {
                const option = document.createElement('option');
                option.value = tag.id;
                option.textContent = tag.name;
                tagSelect.appendChild(option);
            });
        });

    addBtn.onclick = () => {
        const selectedId = tagSelect.value;
        const selectedText = tagSelect.options[tagSelect.selectedIndex]?.textContent;

        if (selectedId && !selectedTagIds.has(selectedId)) {
            selectedTagIds.add(selectedId);
            selectedTags.push(selectedId); // Добавляем в общий массив

            const tagItem = document.createElement('span');
            tagItem.className = 'tag-item';
            tagItem.textContent = selectedText;

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.textContent = '×';
            removeBtn.className = 'remove-tag-btn';
            removeBtn.onclick = () => {
                tagsList.removeChild(tagItem);
                selectedTagIds.delete(selectedId);
                const index = selectedTags.indexOf(selectedId);
                if (index !== -1) selectedTags.splice(index, 1);
            };

            tagItem.appendChild(removeBtn);
            tagsList.appendChild(tagItem);
        }
    };

    container.appendChild(tagSelect);
    container.appendChild(addBtn);
    container.appendChild(tagsList);

    return container;
}

function createArchiveToggleButton(project, projectId) {
    const button = document.createElement('button');
    button.textContent = project.isArchived ? 'Разархивировать проект' : 'Архивировать проект';

    button.onclick = () => {
        const newArchivedState = !project.isArchived;
        const successMessage = newArchivedState
            ? 'Проект успешно архивирован'
            : 'Проект успешно разархивирован';
        const errorMessage = newArchivedState
            ? 'Ошибка при архивировании проекта'
            : 'Ошибка при разархивировании проекта';

        handleFetchWithToast(
            `/api/projects/archive/${projectId}`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify({ archived: newArchivedState })
            },
            successMessage,
            errorMessage
        )
        .then(() => {
            setTimeout(() => {
                location.reload();
            }, 1000);
        })
        .catch(error => console.error('Ошибка при архивации/разархивации проекта:', error));
    };
    return button;
}

function renderRolesSection(container, projectId) {
    fetch(`/api/projects/${projectId}/roles`)
        .then(response => response.json())
        .then(roles => {
            const rolesSection = document.createElement('div');
            rolesSection.className = 'roles-section';

            const title = document.createElement('h3');
            title.textContent = 'Роли в проекте';
            rolesSection.appendChild(title);

            if (roles.length === 0) {
                const noRoles = document.createElement('p');
                noRoles.textContent = 'Нет доступных ролей.';
                rolesSection.appendChild(noRoles);
            } else {
                const list = document.createElement('ul');
                roles.forEach(role => {
                    const li = document.createElement('li');

                    const text = document.createElement('span');
                    text.textContent = role.name + (role.idProject === null ? ' (глобальная)' : '');
                    li.appendChild(text);

                    // Только для проектных ролей
                    if (role.idProject !== null) {
                        const editButton = document.createElement('button');
                        editButton.className = 'role-edit-button';
                        editButton.innerHTML = `<img src="/icons/pencil.svg" alt="Редактировать" class="icon">`;
                        editButton.onclick = () => {
                            renderEditRoleView(container, projectId, role, rolesSection);
                        };
                        li.appendChild(editButton);

                        const deleteButton = document.createElement('button');
                        deleteButton.className = 'role-delete-button';
                        deleteButton.innerHTML = `<img src="/icons/trash.svg" alt="Удалить" class="icon">`;
                        deleteButton.onclick = () => {
                            deleteRole(role.id, projectId, rolesSection);
                        };
                        li.appendChild(deleteButton);
                    }


                    list.appendChild(li);
                });
                rolesSection.appendChild(list);
            }

            const addRoleButton = document.createElement('button');
            addRoleButton.textContent = 'Добавить роль в проект';
            addRoleButton.onclick = () => {
                renderCreateRoleView(container, projectId, rolesSection);
            };
            rolesSection.appendChild(addRoleButton);

            container.appendChild(rolesSection);
        })
        .catch(error => {
            console.error('Ошибка загрузки ролей:', error);
        });
}

function renderCreateRoleView(container, projectId, previousSection) {
    previousSection.style.display = 'none';

    const createSection = document.createElement('div');
    createSection.className = 'create-role-section';

    const title = document.createElement('h3');
    title.textContent = 'Создание кастомной роли';
    createSection.appendChild(title);

    const nameLabel = document.createElement('label');
    nameLabel.textContent = 'Имя роли:';
    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    createSection.appendChild(nameLabel);
    createSection.appendChild(nameInput);

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

    const permissionsList = document.createElement('div');
    permissionsList.className = 'permissions-list';

    Object.entries(permissionLabels).forEach(([permissionKey, permissionLabel]) => {
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = permissionKey;
        checkbox.name = permissionKey;
        checkbox.value = permissionKey;

        const label = document.createElement('label');
        label.htmlFor = permissionKey;
        label.textContent = permissionLabel;

        const wrapper = document.createElement('div');
        wrapper.className = 'permission-item';
        wrapper.appendChild(checkbox);
        wrapper.appendChild(label);

        permissionsList.appendChild(wrapper);
    });

    createSection.appendChild(permissionsList);

    const createButton = document.createElement('button');
    createButton.textContent = 'Создать';
    createButton.onclick = () => {
        const name = nameInput.value.trim();
        if (!name) {
            showToast('Имя роли не может быть пустым', 'error');
            return;
        }

        const permissions = {};
        Object.keys(permissionLabels).forEach(key => {
            const checkbox = document.getElementById(key);
            permissions[key] = checkbox.checked;
        });

        handleFetchWithToast(
            `/api/projects/${projectId}/roles`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify({ name, permissions })
            },
            'Роль успешно создана',
            'Ошибка при создании роли'
        )
        .then(() => {
            container.removeChild(createSection);
            previousSection.remove();
            renderRolesSection(container, projectId);
        })
        .catch(error => {
            console.error('Ошибка при создании роли:', error);
        });
    };

    createSection.appendChild(createButton);
    container.appendChild(createSection);
}


function deleteRole(roleId, projectId, container) {
    if (!confirm('Удалить эту роль?')) return;

    handleFetchWithToast(
        `/api/projects/${projectId}/roles/${roleId}`,
        {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        },
        'Роль успешно удалена',
        'Не удалось удалить роль'
    )
    .then(() => {
        container.innerHTML = '';
        renderRolesSection(container, projectId);
    })
    .catch(error => {
        console.error('Ошибка при удалении роли:', error);
    });
}

function renderEditRoleView(container, projectId, role, previousSection) {
    previousSection.style.display = 'none';

    const editSection = document.createElement('div');
    editSection.className = 'edit-role-section';

    const title = document.createElement('h3');
    title.textContent = `Редактирование роли "${role.name}"`;
    editSection.appendChild(title);

    const nameLabel = document.createElement('label');
    nameLabel.textContent = 'Новое имя роли:';
    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.value = role.name;
    editSection.appendChild(nameLabel);
    editSection.appendChild(nameInput);

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
    const permissionsList = document.createElement('div');
    permissionsList.className = 'permissions-list';

    Object.entries(permissionLabels).forEach(([permissionKey, permissionLabel]) => {
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = 'edit_' + permissionKey;
        checkbox.name = permissionKey;
        checkbox.checked = role.permissions?.[permissionKey] || false;

        const label = document.createElement('label');
        label.htmlFor = 'edit_' + permissionKey;
        label.textContent = permissionLabel;

        const wrapper = document.createElement('div');
        wrapper.className = 'permission-item';
        wrapper.appendChild(checkbox);
        wrapper.appendChild(label);

        permissionsList.appendChild(wrapper);
    });

    editSection.appendChild(permissionsList);

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.onclick = async () => {
        const newName = nameInput.value.trim();
        if (!newName) {
            alert('Имя роли не может быть пустым');
            return;
        }

        const updatedPermissions = {};
        Object.keys(permissionLabels).forEach(key => {
            updatedPermissions[key] = document.getElementById('edit_' + key).checked;
        });

        console.log("Обновлённые права:", updatedPermissions);

        const updatedRole = await updateRole(projectId, role.id, {
            name: newName,
            permissions: updatedPermissions
        }, csrfToken);

        if (updatedRole) {
            container.removeChild(editSection);
            previousSection.remove();
            renderRolesSection(container, projectId);
        }
    };

    editSection.appendChild(saveButton);
    container.appendChild(editSection);
}

async function updateRole(projectId, roleId, updatedData, csrfToken) {
    try {
        const updatedRole = await handleFetchWithToast(
            `/api/projects/${projectId}/roles/${roleId}`,
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify(updatedData)
            },
            'Роль успешно обновлена',
            'Ошибка при обновлении роли'
        );

        console.log("Роль обновлена:", updatedRole);
        return updatedRole;
    } catch (error) {
        console.error('Ошибка при обновлении роли:', error);
    }
}


function renderMemberItem(member, projectId, roles) {
    const listItem = document.createElement('li');

    // Почта участника
    const emailSpan = document.createElement('span');
    emailSpan.textContent = member.email + (member.confirmed ? ' ' : ' (Приглашение отправлено) ');
    emailSpan.style.color = member.confirmed ? '' : '#888';
    listItem.appendChild(emailSpan);

    // Выпадающий список ролей
    const roleSelect = document.createElement('select');

    roles.forEach(role => {
        const option = document.createElement('option');
        option.value = role.id;
        option.textContent = role.name;
        if (role.id === member.roleId) {
            option.selected = true;
        }
        roleSelect.appendChild(option);
    });

    roleSelect.onchange = () => {
        const newRoleId = parseInt(roleSelect.value);
        if (newRoleId !== member.roleId) {
            handleFetchWithToast(
                `/api/projects/${projectId}/members/${member.userId}/role`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': csrfToken
                    },
                    body: JSON.stringify({ roleId: newRoleId })
                },
                'Роль участника успешно обновлена',
                'Ошибка при обновлении роли участника'
            )
            .then(() => loadProjectInfoView(projectId))
            .catch(err => console.error('Ошибка смены роли:', err));
        }
    };

    listItem.appendChild(roleSelect);

    const deleteBtn = document.createElement('button');
    deleteBtn.textContent = member.confirmed ? 'Удалить' : 'Отозвать';
    deleteBtn.onclick = () => {
        removeProjectMember(projectId, member.userId, member.email, handleFetchWithToast, loadProjectInfoView);
    };
    listItem.appendChild(deleteBtn);

    return listItem;
}


