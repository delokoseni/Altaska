const Gantt = window.Gantt;

export async function renderGanttChart(projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '<div id="gantt-target" style="overflow-x: auto; min-height: 400px;"></div>';

    try {
        const response = await fetch(`/api/tasks/project/${projectId}`);
        if (!response.ok) throw new Error('Ошибка загрузки задач');

        const tasks = await response.json();
        if (tasks.length === 0) {
            viewContent.innerHTML = '<p>Задачи не найдены для этого проекта.</p>';
            return;
        }

        const parsedTasks = tasks.map(task => {
            const start = normalizeDate(task.startTimeServer || task.createdAtServer);
            let end = normalizeDate(task.deadlineServer || task.createdAtServer);

            if (start === end) {
                end = addOneDay(end);
            }

            return {
                id: task.id.toString(),
                name: task.name,
                start: start,
                end: end,
                progress: task.progress || 0,
                dependencies: '',
            };
        });

        console.log('parsedTasks:', parsedTasks);

        new Gantt("#gantt-target", parsedTasks, {
            view_mode: 'Day',
            custom_popup_html: null, // Можете добавить всплывающее окно для задач
        });

    } catch (error) {
        console.error('Ошибка при загрузке Ганта:', error);
        viewContent.innerHTML = '<p>Не удалось загрузить график Ганта. Попробуйте позже.</p>';
    }
}

function normalizeDate(input) {
    const date = new Date(input);
    return date.toISOString().split('.')[0]; // Формат: YYYY-MM-DDTHH:mm:ss
}

function addOneDay(dateStr) {
  const d = new Date(dateStr);
  d.setDate(d.getDate() + 1);
  return d.toISOString().split('.')[0];
}
