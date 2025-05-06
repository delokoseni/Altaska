// Функция для получения всех задач проекта
function getAllTasksForProject(projectId) {
    return fetch(`/api/tasks/project/${projectId}/withperformers`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при получении задач');
            }
            return response.json();
        })
        .then(tasks => {
            console.log('Полученные задачи:', tasks);
            return tasks;
        })
        .catch(error => console.error('Ошибка загрузки задач:', error));
}

// Функция для получения всех приоритетов
function getAllPriorities() {
    return fetch('/api/priorities')
        .then(response => response.json())
        .catch(error => console.error('Ошибка загрузки приоритетов:', error));
}

// Функция для получения всех статусов
function getAllStatuses() {
    return fetch('/api/statuses')
        .then(response => response.json())
        .catch(error => console.error('Ошибка загрузки статусов:', error));
}

// Функция для получения всех подтвержденных участников проекта
function getConfirmedMembers(projectId) {
    return fetch(`/api/projects/${projectId}/confirmed-members`)
        .then(response => response.json())
        .catch(error => console.error('Ошибка загрузки участников:', error));
}

// Функция для создания фильтра (select)
function createFilter(label, options, filterType) {
    const filterContainer = document.createElement('div');
    filterContainer.className = 'kanban-filter-container';

    const filterLabel = document.createElement('span');
    filterLabel.textContent = label;
    filterLabel.className = 'kanban-filter-label';
    filterContainer.appendChild(filterLabel);

    const select = document.createElement('select');
    select.className = `kanban-filter-select ${filterType}-filter`;

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = `Все ${label.toLowerCase()}`;
    select.appendChild(defaultOption);

    if (filterType === 'member') {
        const noneOption = document.createElement('option');
        noneOption.value = 'none';
        noneOption.textContent = 'Без исполнителя';
        select.appendChild(noneOption);

        options.forEach(option => {
            const optionElement = document.createElement('option');
            optionElement.value = option.userId;
            optionElement.textContent = option.email;
            select.appendChild(optionElement);
        });
    } else {
        options.forEach(option => {
            const optionElement = document.createElement('option');
            optionElement.value = option.id;
            optionElement.textContent = option.name || option.title || 'Неизвестно';
            select.appendChild(optionElement);
        });
    }

    filterContainer.appendChild(select);
    return filterContainer;
}

// Селектор группировки
function createGroupBySelector() {
    const container = document.createElement('div');
    container.className = 'kanban-filter-container';

    const label = document.createElement('span');
    label.textContent = 'Группировать по:';
    label.className = 'kanban-filter-label';
    container.appendChild(label);

    const select = document.createElement('select');
    select.className = 'kanban-filter-select group-by-selector';

    const groupOptions = [
        { value: 'status', label: 'Статусы' },
        { value: 'priority', label: 'Приоритеты' },
    ];

    groupOptions.forEach(opt => {
        const option = document.createElement('option');
        option.value = opt.value;
        option.textContent = opt.label;
        select.appendChild(option);
    });

    container.appendChild(select);
    return container;
}

// Основная функция отображения фильтров и канбан-доски
// Основная функция отображения фильтров и канбан-доски
export function renderKanbanFiltersAndBoard(projectId) {
    Promise.all([
        getAllTasksForProject(projectId),
        getAllPriorities(),
        getAllStatuses(),
        getConfirmedMembers(projectId),
    ])
    .then(([tasks, priorities, statuses, members]) => {
        const viewContent = document.querySelector('.view-content');
        viewContent.innerHTML = '';

        const filterContainer = document.createElement('div');
        filterContainer.className = 'kanban-filters';
        viewContent.appendChild(filterContainer);

        const groupBySelectorContainer = createGroupBySelector();
        const groupBySelect = groupBySelectorContainer.querySelector('select');
        filterContainer.appendChild(groupBySelectorContainer);

        const memberFilterContainer = createFilter('Исполнители', members, 'member');
        const memberSelect = memberFilterContainer.querySelector('select');
        filterContainer.appendChild(memberFilterContainer);

        const kanbanBoard = document.createElement('div');
        kanbanBoard.className = 'kanban-board';
        viewContent.appendChild(kanbanBoard);

        function updateBoard() {
            const filters = {
                memberId: memberSelect.value
            };

            const filteredTasks = filterTasks(tasks, filters);
            renderKanbanBoard(kanbanBoard, groupBySelect.value, filteredTasks, statuses, priorities, updateBoard);
        }

        groupBySelect.addEventListener('change', updateBoard);
        memberSelect.addEventListener('change', updateBoard);

        updateBoard();
    })
    .catch(error => console.error('Ошибка при загрузке данных для канбан-доски:', error));
}

// Фильтрация задач
function filterTasks(tasks, filters) {
    return tasks.filter(task => {
        if (filters.memberId) {
            if (filters.memberId === 'none') {
                return !Array.isArray(task.performers) || task.performers.length === 0;
            }

            return Array.isArray(task.performers) &&
                   task.performers.some(p => String(p.id) === filters.memberId);
        }
        return true;
    });
}

function groupTasksByFilter(tasks, groupBy, statuses, priorities) {
    const groups = {};

    if (groupBy === 'status') {
        statuses.forEach(status => {
            groups[status.name] = [];
        });
    } else if (groupBy === 'priority') {
        priorities.forEach(priority => {
            groups[priority.name] = [];
        });
        groups['Без приоритета'] = [];
    }

    tasks.forEach(task => {
        let groupKey = '';

        if (groupBy === 'status') {
            groupKey = task.idStatus.name;
        } else if (groupBy === 'priority') {
            groupKey = task.idPriority?.name || 'Без приоритета';
        }

        if (!groups[groupKey]) {
            groups[groupKey] = [];
        }
        groups[groupKey].push(task);
    });

    return groups;
}


// Отображение канбан-доски с поддержкой перетаскивания
function renderKanbanBoard(container, groupBy, tasks, statuses, priorities, updateBoard) {
    container.innerHTML = '';
    const groupedTasks = groupTasksByFilter(tasks, groupBy, statuses, priorities);

    for (const group in groupedTasks) {
        const groupColumn = document.createElement('div');
        groupColumn.classList.add('kanban-group');
        groupColumn.setAttribute('data-status', group);

        const header = document.createElement('h3');
        header.textContent = group;
        groupColumn.appendChild(header);

        groupColumn.addEventListener('dragover', (e) => {
            e.preventDefault();
            groupColumn.classList.add('drag-over');
        });

        groupColumn.addEventListener('dragleave', () => {
            groupColumn.classList.remove('drag-over');
        });

        groupColumn.addEventListener('drop', (e) => {
            e.preventDefault();
            const taskId = e.dataTransfer.getData('taskId');
            const task = tasks.find(t => t.id === parseInt(taskId));

            if (task) {
                if (groupBy === 'status') {
                    task.idStatus = statuses.find(s => s.name === group) || null;
                    updateTaskStatus(task.id, group);
                } else if (groupBy === 'priority') {
                    task.idPriority = priorities.find(p => p.name === group) || null;
                    updateTaskPriority(task.id, group);
                }
            }

            groupColumn.classList.remove('drag-over');
            updateBoard();
        });

        groupedTasks[group].forEach(task => {
            const taskCard = document.createElement('div');
            taskCard.classList.add('kanban-task');
            taskCard.setAttribute('draggable', 'true');
            taskCard.setAttribute('data-task-id', task.id);

            const taskTitle = document.createElement('div');
            taskTitle.textContent = task.name;
            taskCard.appendChild(taskTitle);

            taskCard.addEventListener('click', () => {
                showTaskDetails(task, 'канбан');
            });

            taskCard.addEventListener('dragstart', (e) => {
                e.dataTransfer.setData('taskId', task.id);
            });

            groupColumn.appendChild(taskCard);
        });

        container.appendChild(groupColumn);
    }
}

// Функция для обновления статуса задачи на сервере
function updateTaskStatus(taskId, newStatus) {
    const csrfToken = getCsrfToken();
    fetch(`/api/tasks/${taskId}/statusByName`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
        body: JSON.stringify({ statusName: newStatus }),
    })
    .then(response => response.json())
    .then(updatedTask => {
        console.log('Статус задачи обновлен:', updatedTask);
    })
    .catch(error => console.error('Ошибка обновления статуса задачи:', error));
}

// Функция для обновления приоритета задачи на сервере
function updateTaskPriority(taskId, newPriority) {
    const csrfToken = getCsrfToken();
    fetch(`/api/tasks/${taskId}/priorityByName`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
        body: JSON.stringify({ priorityName: newPriority }),
    })
    .then(response => response.json())
    .then(updatedTask => {
        console.log('Приоритет задачи обновлен:', updatedTask);
    })
    .catch(error => console.error('Ошибка обновления приоритета задачи:', error));
}

function getCsrfToken() {
    return document.querySelector('meta[name="csrf-token"]').getAttribute('content');
}
