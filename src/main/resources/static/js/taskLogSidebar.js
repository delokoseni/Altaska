export function showTaskLogsSidebar(task) {
    const existing = document.querySelector('.task-details-sidebar');
    if (existing) existing.remove();

    const sidebar = document.createElement('div');
    sidebar.className = 'task-details-sidebar slide-in';

    const closeButton = document.createElement('button');
    closeButton.className = 'close-button';
    closeButton.title = 'Закрыть';
    closeButton.innerHTML = '&times;';
    closeButton.onclick = () => {
        sidebar.remove();
        window.removeEventListener('click', outsideClickListener);
    };

    const content = document.createElement('div');
    content.className = 'task-details-content';

    content.innerHTML = `
        <div class="task-log-section">
            <h3>История изменения статуса</h3>
            <div class="task-log-container logs-status">
                <p>Загрузка...</p>
            </div>
        </div>
    `;

    sidebar.appendChild(closeButton);
    sidebar.appendChild(content);
    document.body.appendChild(sidebar);

    setTimeout(() => {
        window.addEventListener('click', outsideClickListener);
    }, 0);

    fetch(`/api/statuses-log/task/${task.id}`)
        .then(res => res.json())
        .then(logs => {
            const container = content.querySelector('.logs-status');
            if (!logs.length) {
                container.innerHTML = '<p>Нет изменений статуса</p>';
                return;
            }

            container.innerHTML = logs.map(log => `
                <div class="log-entry">
                    <span class="log-time">${new Date(log.timestamp).toLocaleString()}</span>
                    <span class="log-user">${log.user}</span> изменил статус на <b>${log.status}</b>
                </div>
            `).join('');
        })
        .catch(err => {
            console.error('Ошибка загрузки логов статуса:', err);
            content.querySelector('.logs-status').innerHTML = '<p class="error">Ошибка загрузки логов статуса</p>';
        });

    function outsideClickListener(e) {
        if (!sidebar.contains(e.target)) {
            sidebar.remove();
            window.removeEventListener('click', outsideClickListener);
        }
    }
}
