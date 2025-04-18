function selectProject(projectId, projectName) {
    sessionStorage.setItem('currentProjectId', projectId);
    sessionStorage.setItem('currentProjectName', projectName);

    const mainContent = document.querySelector('.main-content');
    mainContent.innerHTML = '';

    const menu = document.createElement('div');
    menu.className = 'project-menu';

    const views = ['Список', 'Канбан', 'Гант', 'Участники', 'Настройки'];
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
    loadView('список', projectId);
}

function loadView(view, projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';

    if (view === 'список') {
        fetch(`/api/tasks/project/${projectId}`)
            .then(response => response.json())
            .then(tasks => {
                tasks.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                const taskListContainer = document.createElement('div');
                taskListContainer.className = 'task-list';
                const header = document.createElement('div');
                header.className = 'task-header';
                const title = document.createElement('span');
                title.textContent = tasks.length === 0 ? 'Нет задач' : 'Задачи';
                header.appendChild(title);

                const addBtn = document.createElement('button');
                addBtn.className = 'add-project-button';
                addBtn.title = 'Добавить';

                const plusText = document.createTextNode('+');
                addBtn.appendChild(plusText);
                const addLabel = document.createElement('span');
                addLabel.className = 'add-label';
                addLabel.textContent = 'Добавить';
                addBtn.appendChild(addLabel);

                addBtn.onclick = () => showTaskForm(projectId, taskListContainer);
                header.appendChild(addBtn);
                taskListContainer.appendChild(header);

                if (tasks.length > 0) {
                    tasks.forEach(task => {
                        const taskDiv = document.createElement('div');
                        taskDiv.className = 'task-item';
                        taskDiv.innerHTML = `
                            <h3>${task.name}</h3>
                            <p>${task.idStatus.name} ${task.idPriority ? task.idPriority.name : ''}</p>
                        `;
                        taskDiv.addEventListener('click', () => {
                            showTaskDetails(task);
                        });
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
    const previousContent = container.innerHTML;
    if (existingForm) return;
    container.innerHTML = '';
    fetch('/api/priorities')
        .then(response => response.json())
        .then(priorities => {
            const form = document.createElement('form');
            form.className = 'task-form';

            const createdAtInput = document.createElement('input');
            createdAtInput.type = 'hidden';
            createdAtInput.name = 'createdAt';

            const updatedAtInput = document.createElement('input');
            updatedAtInput.type = 'hidden';
            updatedAtInput.name = 'updatedAt';

            form.appendChild(createdAtInput);
            form.appendChild(updatedAtInput);

            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = csrfParam;
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);

            const nameInput = document.createElement('input');
            nameInput.type = 'text';
            nameInput.placeholder = 'Название задачи';
            nameInput.name = 'name';
            nameInput.required = true;

            const descriptionInput = document.createElement('textarea');
            descriptionInput.placeholder = 'Описание задачи';
            descriptionInput.name = 'description';

            const deadlineInput = document.createElement('input');
            deadlineInput.type = 'datetime-local'; // Дата и время
            deadlineInput.name = 'deadline';
            deadlineInput.placeholder = 'Срок выполнения';

            const prioritySelect = document.createElement('select');
            prioritySelect.name = 'priorityId';

            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = 'Без приоритета';
            prioritySelect.appendChild(defaultOption);

            priorities.forEach(p => {
                const option = document.createElement('option');
                option.value = p.id;
                option.textContent = p.name;
                prioritySelect.appendChild(option);
            });

            const submitBtn = document.createElement('button');
            submitBtn.type = 'submit';
            submitBtn.textContent = 'Создать';

            const backButton = document.createElement('button');
            backButton.textContent = 'Назад';
            backButton.type = 'button';
            backButton.className = 'back-button';
            backButton.onclick = () => {
                container.innerHTML = previousContent;
            };

            form.appendChild(backButton);
            form.appendChild(nameInput);
            form.appendChild(descriptionInput);
            form.appendChild(deadlineInput);
            form.appendChild(prioritySelect);
            form.appendChild(submitBtn);

            form.onsubmit = function (e) {
                e.preventDefault();
                const now = new Date();
                createdAtInput.value = now.toISOString().split('T')[0];
                updatedAtInput.value = now.toISOString();

                const formData = new FormData();
                formData.append('createdAt', createdAtInput.value);
                formData.append('updatedAt', updatedAtInput.value);
                formData.append('name', nameInput.value);
                formData.append('description', descriptionInput.value);
                formData.append('priorityId', prioritySelect.value);
                if (deadlineInput.value) {
                    formData.append('deadline', deadlineInput.value);
                }
                formData.append(csrfParam, csrfToken);

                fetch(`/api/tasks/create/${projectId}`, {
                    method: 'POST',
                    body: formData
                })
                .then(response => {
                    if (!response.ok) throw new Error("Ошибка при создании задачи");
                    return response.json();
                })
                .then(data => {
                    loadView('список', projectId);
                })
                .catch(error => {
                    console.error('Ошибка создания задачи:', error);
                });
            };

            container.appendChild(form);
        });
}

