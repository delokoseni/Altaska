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
        button.onclick = function () { loadView(view.toLowerCase(), projectId); };
        menu.appendChild(button);
    });

    const viewContent = document.createElement('div');
    viewContent.className = 'view-content';

    mainContent.appendChild(menu);
    mainContent.appendChild(viewContent);
}


function loadView(view, projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';

    if (view === 'список') {
        fetch(`/api/tasks/project/${projectId}`)
            .then(response => response.json())
            .then(tasks => {
                const taskListContainer = document.createElement('div');
                taskListContainer.className = 'task-list';

                const header = document.createElement('div');
                header.className = 'task-header';
                const title = document.createElement('span');
                title.textContent = tasks.length === 0 ? 'Нет задач' : 'Задачи';
                header.appendChild(title);

                const addBtn = document.createElement('button');
                addBtn.textContent = '+ Добавить';
                addBtn.className = 'add-task-button';
                addBtn.onclick = () => showTaskForm(projectId, taskListContainer);
                header.appendChild(addBtn);
                taskListContainer.appendChild(header);

                if (tasks.length > 0) {
                    tasks.forEach(task => {
                        const taskDiv = document.createElement('div');
                        taskDiv.className = 'task-item';
                        taskDiv.innerHTML = `
                            <h3>${task.name}</h3>
                            <p>${task.description}</p>
                        `;
                        taskListContainer.appendChild(taskDiv);
                    });
                }

                viewContent.appendChild(taskListContainer);
            })
            .catch(error => {
                console.error('Ошибка загрузки задач:', error);
            });
    }
}

function showTaskForm(projectId, container) {
    const existingForm = container.querySelector('.task-form');
    if (existingForm) return;

    const form = document.createElement('form');
    form.className = 'task-form';

    // Добавление CSRF токена в скрытое поле
    const csrfInput = document.createElement('input');
    csrfInput.type = 'hidden';
    csrfInput.name = csrfParam;  // имя параметра CSRF токена
    csrfInput.value = csrfToken; // значение токена
    form.appendChild(csrfInput);

    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.placeholder = 'Название задачи';
    nameInput.name = 'name';  // имя параметра
    nameInput.required = true;

    const descriptionInput = document.createElement('textarea');
    descriptionInput.placeholder = 'Описание задачи';
    descriptionInput.name = 'description';  // имя параметра
    descriptionInput.required = true;

    const submitBtn = document.createElement('button');
    submitBtn.type = 'submit';
    submitBtn.textContent = 'Создать';

    form.appendChild(nameInput);
    form.appendChild(descriptionInput);
    form.appendChild(submitBtn);

    form.onsubmit = function (e) {
        e.preventDefault();

        // Создаем FormData для отправки данных формы
        const formData = new FormData();
        formData.append('name', nameInput.value);  // Параметр name
        formData.append('description', descriptionInput.value);  // Параметр description
        formData.append(csrfParam, csrfToken); // Добавляем CSRF токен

        fetch(`/api/tasks/create/${projectId}`, {
            method: 'POST',
            body: formData  // Отправляем форму как данные формы
        })
        .then(response => {
            if (!response.ok) throw new Error("Ошибка при создании задачи");
            return response.json();
        })
        .then(data => {
            loadView('список', projectId);  // Обновление списка задач
        })
        .catch(error => {
            console.error('Ошибка создания задачи:', error);
        });
    };

    container.appendChild(form);
}
