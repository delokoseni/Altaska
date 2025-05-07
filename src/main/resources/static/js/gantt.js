export async function renderGanttChart(projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '<div id="Gantt" style="min-height: 400px;"></div>';

    try {
        const response = await fetch(`/api/tasks/project/${projectId}`);
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

    } catch (error) {
        console.error('Ошибка при загрузке Ганта:', error);
        viewContent.innerHTML = '<p>Не удалось загрузить график Ганта. Попробуйте позже.</p>';
    }
}
