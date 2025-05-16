export function loadProjectLogsView(projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';
    const wrapper = document.createElement('div');
    wrapper.className = 'project-logs-view';
    viewContent.appendChild(wrapper);

    const title = document.createElement('h2');
    title.textContent = 'История действий';
    wrapper.appendChild(title);

    fetch(`/api/logs/project/${projectId}`)
        .then(response => response.json())
        .then(logs => {
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

                let text = `[${date}] ${user} - ${details.action || 'действие'}`;

                if (details.message) {
                    text += `: ${details.message}`;
                }

                item.textContent = text;
                logList.appendChild(item);
            });

            wrapper.appendChild(logList);
        })
        .catch(error => {
            console.error('Ошибка загрузки логов:', error);
            const errorText = document.createElement('p');
            errorText.textContent = 'Ошибка загрузки истории.';
            wrapper.appendChild(errorText);
        });
}
