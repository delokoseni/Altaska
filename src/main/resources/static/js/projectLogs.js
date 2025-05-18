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

                const summary = document.createElement('div');
                summary.className = 'activity-log-summary';
                summary.textContent = `[${date}] ${user} - ${details.action || 'действие'}${details.message ? `: ${details.message}` : ''}`;
                item.appendChild(summary);

                const detailsSection = document.createElement('div');
                detailsSection.className = 'activity-log-details';

                if (details.changes && Array.isArray(details.changes) && details.changes.length > 0) {
                    details.changes.forEach(change => {
                        const changeBlock = document.createElement('div');
                        changeBlock.className = 'log-change-block';

                        const field = document.createElement('div');
                        field.className = 'log-change-field';
                        field.textContent = `Поле: ${change.field}`;

                        const oldPre = document.createElement('pre');
                        oldPre.textContent = 'Было:\n' + JSON.stringify(change.old, null, 2);
                        oldPre.className = 'log-block-old';

                        const newPre = document.createElement('pre');
                        newPre.textContent = 'Стало:\n' + JSON.stringify(change.new, null, 2);
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

            // Скрытие при клике вне
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
            const errorText = document.createElement('p');
            errorText.textContent = 'Ошибка загрузки истории.';
            wrapper.appendChild(errorText);
        });
}