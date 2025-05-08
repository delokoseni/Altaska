import { getCsrfToken } from './kanban.js';

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
        const response = await fetch(`/api/gantt/project/${projectId}`);
        if (!response.ok) throw new Error('Ошибка загрузки задач');

        const tasks = await response.json();
        if (tasks.length === 0) {
            viewContent.innerHTML = '<p>Задачи не найдены для этого проекта.</p>';
            return;
        }

        // Преобразуем задачи под формат Syncfusion
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
                Predecessor: task.dependencies || '', // строка типа "1FS,2SS" если есть
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
                    { field: 'TaskID', headerText: 'Идентификатор', isPrimaryKey: true, width: '140' },
                    { field: 'TaskName', headerText: 'Название задачи'},
                    { field: 'StartDate', headerText: 'Дата начала', format: { type: 'date', format: 'dd.MM.yyyy' }},
                    { field: 'EndDate', headerText: 'Дата окончания', format: { type: 'date', format: 'dd.MM.yyyy' }},
                    { field: 'Predecessor', headerText: 'Зависимости'}
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
            dependencies: task.Predecessor // строка типа "1FS,2SS"
        }));

        try {
            const response = await fetch('/api/gantt/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': getCsrfToken() // если CSRF включён
                },
                body: JSON.stringify({ tasks })
            });

            if (!response.ok) throw new Error('Ошибка сохранения');
            alert('Сохранено!');
        } catch (e) {
            console.error('Ошибка при сохранении диаграммы Ганта:', e);
            alert('Не удалось сохранить изменения.');
        }
    });

    document.querySelector('.view-content').prepend(button);
}
