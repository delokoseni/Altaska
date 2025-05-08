import { getCsrfToken } from './kanban.js';

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
                Progress: task.progress || 0,
                Predecessor: task.dependencies || '', // строка типа "1FS,2SS" если есть
                // Subtasks можно добавить, если есть иерархия
            };
        });

        // Очищаем предыдущий Gantt, если был
        if (window.ganttObj) window.ganttObj.destroy();

        // Создаём Gantt
        window.ganttObj = new ej.gantt.Gantt({
            dataSource: parsedTasks,
            height: '450px',
            taskFields: {
                id: 'TaskID',
                name: 'TaskName',
                startDate: 'StartDate',
                endDate: 'EndDate',
                progress: 'Progress',
                dependency: 'Predecessor',
                // child: 'subtasks' // если есть подзадачи
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
