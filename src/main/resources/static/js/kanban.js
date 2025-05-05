// Функция для получения всех задач проекта
function getAllTasksForProject(projectId) {
    return fetch(`/api/tasks/project/dto/${projectId}`)
        .then(response => response.json())
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

// Функция для получения всех тэгов проекта (пока не используется)
// function getTagsForProject(projectId) {
//     return fetch(`/api/tags/project/${projectId}`)
//         .then(response => response.json())
//         .catch(error => console.error('Ошибка загрузки тэгов:', error));
// }

// Функция для создания фильтра (select)
function createFilter(label, options, filterType) {
    const filterContainer = document.createElement('div');
    filterContainer.className = 'filter-container';

    const filterLabel = document.createElement('span');
    filterLabel.textContent = label;
    filterContainer.appendChild(filterLabel);

    const select = document.createElement('select');
    select.className = `${filterType}-filter`;

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = `Все ${label.toLowerCase()}`;
    select.appendChild(defaultOption);

    options.forEach(option => {
        const optionElement = document.createElement('option');

        if (filterType === 'member') {
            optionElement.value = option.userId;
            optionElement.textContent = option.email;
        } else {
            optionElement.value = option.id;
            optionElement.textContent = option.name || option.title || 'Неизвестно';
        }

        select.appendChild(optionElement);
    });

    filterContainer.appendChild(select);
    return filterContainer;
}

// Селектор группировки
function createGroupBySelector() {
    const container = document.createElement('div');
    container.className = 'filter-container';

    const label = document.createElement('span');
    label.textContent = 'Группировать по:';
    container.appendChild(label);

    const select = document.createElement('select');
    select.className = 'group-by-selector';

    const groupOptions = [
        { value: 'status', label: 'Статусы' },
        { value: 'priority', label: 'Приоритеты' },
        // { value: 'tag', label: 'Тэги' } // Пока закомментировано
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
export function renderKanbanFiltersAndBoard(projectId) {
    Promise.all([
        getAllTasksForProject(projectId),
        getAllPriorities(),
        getAllStatuses(),
        getConfirmedMembers(projectId),
        // getTagsForProject(projectId) // пока отключено
    ])
    .then(([tasks, priorities, statuses, members /*, tags */]) => {
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
            renderKanbanBoard(kanbanBoard, groupBySelect.value, filteredTasks, statuses, priorities);
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
            return task.performers?.some(p => p.userId === parseInt(filters.memberId));
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
            groupKey = task.status;
        } else if (groupBy === 'priority') {
            groupKey = task.priority || 'Без приоритета';
        }

        if (!groups[groupKey]) {
            groups[groupKey] = [];
        }
        groups[groupKey].push(task);
    });

    return groups;
}

// Отображение канбан-доски
function renderKanbanBoard(container, groupBy, tasks, statuses, priorities) {
    container.innerHTML = '';
    const groupedTasks = groupTasksByFilter(tasks, groupBy, statuses, priorities);

    for (const group in groupedTasks) {
        const groupColumn = document.createElement('div');
        groupColumn.classList.add('kanban-group');

        const header = document.createElement('h3');
        header.textContent = group;
        groupColumn.appendChild(header);

        groupedTasks[group].forEach(task => {
            const taskCard = document.createElement('div');
            taskCard.classList.add('kanban-task');
            taskCard.textContent = task.name;
            groupColumn.appendChild(taskCard);
        });

        container.appendChild(groupColumn);
    }
}

