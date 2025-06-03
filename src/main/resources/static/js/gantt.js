import { getCsrfToken } from './kanban.js';
import { showToast } from './toast.js';

ej.base.L10n.load({
    'ru': {
        'gantt': {
            emptyRecord: "Нет записей для отображения",
            id: "ID",
            name: "Имя задачи",
            startDate: "Дата начала",
            endDate: "Дата окончания",
            duration: "Длительность",
            progress: "Прогресс",
            dependency: "Зависимости",
            add: "Добавить",
            edit: "Редактировать",
            update: "Обновить",
            delete: "Удалить",
            cancel: "Отмена",
            expandAll: "Развернуть все",
            collapseAll: "Свернуть все",
            taskInformation: "Информация о задаче",
            generalTab: "Общее",
            type: "Тип",
            offset: "Смещение",
            saveButton: "Сохранить",
        },
        'treegrid': {
            EmptyRecord: "Нет записей для отображения",
        },
        'grid': {
             EmptyRecord: "Нет записей для отображения",
        },
        'datepicker': {
            placeholder: "Выберите дату",
            today: "Сегодня"
        }
    }
});

export async function renderGanttChart(projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '<div id="Gantt" style="min-height: 400px;"></div>';

    try {
        const [tasksResponse, dependenciesResponse] = await Promise.all([
            fetch(`/api/gantt/project/${projectId}`),
            fetch(`/api/gantt/project/${projectId}/dependencies`)
        ]);

        if (!tasksResponse.ok || !dependenciesResponse.ok) throw new Error('Ошибка загрузки данных');

        const tasks = await tasksResponse.json();
        const dependencies = await dependenciesResponse.json();

        if (tasks.length === 0) {
            viewContent.innerHTML = '<p>Задачи не найдены для этого проекта.</p>';
            return;
        }

        const dependencyMap = {};
        dependencies.forEach(dep => {
            const to = dep.to;
            const from = dep.from;
            const type = dep.type;
            const lag = dep.lag;

            let depStr = `${from}${type}`;
            if (lag !== null && lag !== undefined && !isNaN(lag)) {
                depStr += (lag >= 0 ? '+' : '') + lag + 'd';
            }

            if (!dependencyMap[to]) dependencyMap[to] = [];
            dependencyMap[to].push(depStr);
        });

        const parsedTasks = tasks.map(task => {
            const start = new Date(task.startTimeServer || task.createdAtServer);
            let end = new Date(task.deadlineServer || task.createdAtServer);
            if (+start === +end) end.setDate(end.getDate() + 1);

            return {
                TaskID: Number(task.id),
                TaskName: task.name,
                StartDate: start,
                EndDate: end,
                Progress: 0,
                Predecessor: (dependencyMap[task.id] || []).join(','),
            };
        });

        if (window.ganttObj) window.ganttObj.destroy();

        window.ganttObj = new ej.gantt.Gantt({
            dataSource: parsedTasks,
            height: '450px',
            locale: 'ru',
            includeWeekend: true,
            taskFields: {
                id: 'TaskID',
                name: 'TaskName',
                startDate: 'StartDate',
                endDate: 'EndDate',
                dependency: 'Predecessor',
            },
            columns: [
                { field: 'TaskID', headerText: 'Идентификатор', isPrimaryKey: true, width: 140 },
                { field: 'TaskName', headerText: 'Название задачи' },
                { field: 'StartDate', headerText: 'Дата начала', format: { type: 'date', format: 'dd.MM.yyyy' } },
                { field: 'EndDate', headerText: 'Дата окончания', format: { type: 'date', format: 'dd.MM.yyyy' } },
                { field: 'Predecessor', headerText: 'Зависимости' }
            ],
            timelineSettings: {
                timelineUnitSize: 60,
                topTier: {
                    unit: 'Month',
                    format: 'MMMM yyyy'
                },
                bottomTier: {
                    unit: 'Day',
                    format: 'dd.MM'
                }
            },
            allowTaskbarEditing: true,
            allowSelection: true,
            allowSorting: true,
            editSettings: {
                allowEditing: true,
                allowAdding: true,
                allowDeleting: true,
                allowTaskbarEditing: true,
                showDeleteConfirmDialog: true
            }
        });

        window.ganttObj.appendTo('#Gantt');
        createSaveButton();

    } catch (error) {
        console.error('Ошибка при загрузке Ганта:', error);
        viewContent.innerHTML = '<p>Не удалось загрузить график Ганта. Попробуйте позже.</p>';
    }
}

function createSaveButton() {
    const button = document.createElement('button');
    button.textContent = 'Сохранить';
    button.classList.add('gantt-save-button');

    button.addEventListener('click', async () => {
        if (!window.ganttObj) return;

        const tasks = window.ganttObj.flatData.map(task => ({
            id: task.TaskID,
            startTime: task.StartDate,
            deadline: task.EndDate,
            progress: task.Progress,
            dependencies: task.Predecessor
        }));

        console.log('Связи между задачами (dependencies):');
        tasks.forEach(task => {
            console.log(`Задача ${task.id}: ${task.dependencies || 'нет связей'}`);
        });
        console.log('Данные для сохранения:', tasks);

        try {
            const response = await fetch('/api/gantt/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken()
                },
                body: JSON.stringify({ tasks })
            });

            const text = await response.text();

            if (!response.ok) {
                const hasEnglish = /[a-zA-Z]/.test(text);
                if (hasEnglish) {
                    showToast("Ошибка сохранения диаграммы: неизвестная ошибка", "error");
                } else {
                    showToast("Ошибка сохранения диаграммы: " + text, "error");
                }
                throw new Error(text);
            }

            showToast("Изменения успешно сохранены ✅");
            console.log("Сервер ответил:", text);

        } catch (e) {
            console.error('Ошибка при сохранении диаграммы Ганта:', e);
            const message = e.message || '';
            const hasEnglish = /[a-zA-Z]/.test(message);
            if (hasEnglish) {
                if (message === 'Failed to fetch') {
                    showToast("Не удалось сохранить изменения: проверьте подключение к интернету или попробуйте позже", "error");
                } else {
                    showToast("Не удалось сохранить изменения: неизвестная ошибка", "error");
                }
            } else {
                showToast("Не удалось сохранить изменения: " + message, "error");
            }
        }
    });

    document.querySelector('.view-content').prepend(button);
}


