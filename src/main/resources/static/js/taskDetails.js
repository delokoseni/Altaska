function showTaskDetails(task) {
    const existing = document.querySelector('.task-details-sidebar');
    if (existing) existing.remove();

    const sidebar = document.createElement('div');
    sidebar.className = 'task-details-sidebar slide-in';

    sidebar.innerHTML = `
        <button class="close-button" title="Закрыть">&times;</button>
        <div class="task-details-content">
            <h2>${task.name}</h2>
            <p><strong>Описание:</strong> ${task.description || 'Нет описания'}</p>
            <p><strong>Статус:</strong> ${task.idStatus?.name || 'Неизвестно'}</p>
            <p><strong>Приоритет:</strong> ${task.idPriority?.name || 'Без приоритета'}</p>
            <p><strong>Срок:</strong> ${task.deadline ? new Date(task.deadline).toLocaleString() : 'Не установлен'}</p>
        </div>
    `;

    document.body.appendChild(sidebar);

    const closeButton = sidebar.querySelector('.close-button');
    closeButton.addEventListener('click', () => {
        sidebar.remove();
        window.removeEventListener('click', outsideClickListener);
    });

    setTimeout(() => {
        window.addEventListener('click', outsideClickListener);
    }, 0);

    function outsideClickListener(e) {
        if (!sidebar.contains(e.target)) {
            sidebar.remove();
            window.removeEventListener('click', outsideClickListener);
        }
    }
}
