function selectProject(projectId, projectName) {
    sessionStorage.setItem('currentProjectId', projectId);
    sessionStorage.setItem('currentProjectName', projectName);
    const mainContent = document.querySelector('.main-content');
    mainContent.innerHTML = '';

    const menu = document.createElement('div');
    menu.className = 'project-menu';

    const views = ['Список', 'Канбан', 'Гант', 'Участники'];
    views.forEach(view => {
        const button = document.createElement('button');
        button.textContent = view;
        button.onclick = function() { loadView(view.toLowerCase(), projectId); };
        menu.appendChild(button);
    });

    mainContent.appendChild(menu);
}

function loadView(view, projectId) {
    console.log(`Загрузка ${view} для проекта ${projectId}`);
}
