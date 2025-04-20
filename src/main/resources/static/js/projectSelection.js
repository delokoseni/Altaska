function selectProject(projectId, projectName) {
    sessionStorage.setItem('currentProjectId', projectId);
    sessionStorage.setItem('currentProjectName', projectName);

    const mainContent = document.querySelector('.main-content');
    mainContent.innerHTML = '';

    const menu = document.createElement('div');
    menu.className = 'project-menu';

    const views = ['Список', 'Канбан', 'Гант', 'О проекте'];
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
    if (view === 'канбан') { }
    if (view === 'гант') { }
    if (view === 'о проекте')
    {
        loadProjectInfoView(projectId);
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

            const selectedTagIds = [];
            const tagSelector = createTagSelector(projectId, selectedTagIds);

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
            form.appendChild(tagSelector);
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

                selectedTagIds.forEach(tagId => {
                        formData.append('tagIds', tagId);
                });

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

function loadProjectInfoView(projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';

    fetch(`/api/projects/${projectId}`)
        .then(response => response.json())
        .then(project => {
            const container = document.createElement('div');
            container.className = 'project-info-container';

            // --- Название ---
            const nameGroup = document.createElement('div');
            const nameLabel = document.createElement('label');
            nameLabel.textContent = 'Название проекта:';
            const nameInput = document.createElement('input');
            nameInput.type = 'text';
            nameInput.value = project.name;
            nameInput.disabled = true;

            const nameEditButton = document.createElement('button');
            nameEditButton.textContent = 'Изменить';

            nameGroup.appendChild(nameLabel);
            nameGroup.appendChild(nameInput);
            nameGroup.appendChild(nameEditButton);

            // --- Описание ---
            const descGroup = document.createElement('div');
            const descLabel = document.createElement('label');
            descLabel.textContent = 'Описание проекта:';
            const descInput = document.createElement('textarea');
            descInput.value = project.description || '';
            descInput.disabled = true;

            const descEditButton = document.createElement('button');
            descEditButton.textContent = 'Изменить';

            nameEditButton.onclick = () => {
                if (nameInput.disabled) {
                    nameInput.disabled = false;
                    nameEditButton.textContent = 'Сохранить';
                } else {
                    nameInput.disabled = true;
                    nameEditButton.textContent = 'Изменить';
                    updateProjectField(projectId, 'name', nameInput.value); // отправка
                }
            };

            descEditButton.onclick = () => {
                if (descInput.disabled) {
                    descInput.disabled = false;
                    descEditButton.textContent = 'Сохранить';
                } else {
                    descInput.disabled = true;
                    descEditButton.textContent = 'Изменить';
                    updateProjectField(projectId, 'description', descInput.value); // отправка
                }
            };

            descGroup.appendChild(descLabel);
            descGroup.appendChild(descInput);
            descGroup.appendChild(descEditButton);

            container.appendChild(nameGroup);
            container.appendChild(descGroup);
            viewContent.appendChild(container);
            renderTagsSection(container, projectId);
        })
        .catch(error => {
            console.error('Ошибка загрузки проекта:', error);
        });
}

function updateProjectField(projectId, field, value) {
    const formData = new FormData();
    formData.append(field, value);
    formData.append(csrfParam, csrfToken);

    fetch(`/api/projects/update/${projectId}`, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error("Ошибка при обновлении проекта");
        return response.json();
    })
    .then(updated => {
        console.log(`Поле ${field} обновлено`, updated);
    })
    .catch(error => {
        console.error('Ошибка при обновлении поля проекта:', error);
    });
}

function renderTagsSection(container, projectId) {
    const tagsGroup = document.createElement('div');
    tagsGroup.className = 'tags-group';

    const tagsHeader = document.createElement('div');
    tagsHeader.className = 'tags-header';

    const tagsTitle = document.createElement('span');
    tagsTitle.textContent = 'Список тэгов';

    const addTagButton = document.createElement('button');
    addTagButton.textContent = '+ Добавить тэг';
    addTagButton.onclick = () => showAddTagForm(tagsGroup, projectId);

    tagsHeader.appendChild(tagsTitle);
    tagsHeader.appendChild(addTagButton);
    tagsGroup.appendChild(tagsHeader);

    const tagList = document.createElement('div');
    tagList.className = 'tag-list';
    tagsGroup.appendChild(tagList);

    container.appendChild(tagsGroup);

    loadTags(tagList, projectId);
}

function loadTags(tagListElement, projectId) {
    fetch(`/api/tags/project/${projectId}`)
        .then(response => response.json())
        .then(tags => {
            tagListElement.innerHTML = '';
            tags.forEach(tag => {
                tagListElement.appendChild(createTagElement(tag, projectId));
            });
        });
}

function createTagElement(tag, projectId) {
    const tagItem = document.createElement('div');
    tagItem.className = 'tag-item';

    const tagName = document.createElement('span');
    tagName.textContent = tag.name;

    const editBtn = document.createElement('button');
    editBtn.textContent = '✎';
    editBtn.onclick = () => editTag(tag.id, tag.name, projectId);

    const delBtn = document.createElement('button');
    delBtn.textContent = '✖';
    delBtn.onclick = () => {
        fetch(`/api/tags/${tag.id}`, {
            method: 'DELETE',
            headers: { 'X-CSRF-TOKEN': csrfToken }
        }).then(() => loadProjectInfoView(projectId));
    };

    tagItem.appendChild(tagName);
    tagItem.appendChild(editBtn);
    tagItem.appendChild(delBtn);

    return tagItem;
}

function showAddTagForm(container, projectId) {
    const form = document.createElement('div');

    const input = document.createElement('input');
    input.placeholder = 'Введите тэг';

    const saveBtn = document.createElement('button');
    saveBtn.textContent = 'Сохранить';

    saveBtn.onclick = () => {
        const formData = new FormData();
        formData.append('name', input.value);
        formData.append(csrfParam, csrfToken);

        fetch(`/api/tags/create/${projectId}`, {
            method: 'POST',
            body: formData
        }).then(() => loadProjectInfoView(projectId));
    };

    form.appendChild(input);
    form.appendChild(saveBtn);
    container.appendChild(form);
}

function editTag(tagId, currentName, projectId) {
    const newName = prompt('Новое имя тега:', currentName);
    if (newName && newName !== currentName) {
        const formData = new FormData();
        formData.append('name', newName);
        formData.append(csrfParam, csrfToken);

        fetch(`/api/tags/update/${tagId}`, {
            method: 'POST',
            body: formData
        }).then(() => loadProjectInfoView(projectId));
    }
}

function createTagSelector(projectId, selectedTags) {
    const container = document.createElement('div');
    container.className = 'tag-selector-container';

    const tagSelect = document.createElement('select');
    tagSelect.className = 'tag-select';

    const addBtn = document.createElement('button');
    addBtn.type = 'button';
    addBtn.textContent = 'Добавить тег';

    const tagsList = document.createElement('div');
    tagsList.className = 'selected-tags';

    // Храним id выбранных тегов, чтобы не дублировались
    const selectedTagIds = new Set();

    fetch(`/api/tags/project/${projectId}`)
        .then(response => response.json())
        .then(tags => {
            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = 'Выберите тег';
            tagSelect.appendChild(defaultOption);

            tags.forEach(tag => {
                const option = document.createElement('option');
                option.value = tag.id;
                option.textContent = tag.name;
                tagSelect.appendChild(option);
            });
        });

    addBtn.onclick = () => {
        const selectedId = tagSelect.value;
        const selectedText = tagSelect.options[tagSelect.selectedIndex]?.textContent;

        if (selectedId && !selectedTagIds.has(selectedId)) {
            selectedTagIds.add(selectedId);
            selectedTags.push(selectedId); // Добавляем в общий массив

            const tagItem = document.createElement('span');
            tagItem.className = 'tag-item';
            tagItem.textContent = selectedText;

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.textContent = '×';
            removeBtn.className = 'remove-tag-btn';
            removeBtn.onclick = () => {
                tagsList.removeChild(tagItem);
                selectedTagIds.delete(selectedId);
                const index = selectedTags.indexOf(selectedId);
                if (index !== -1) selectedTags.splice(index, 1);
            };

            tagItem.appendChild(removeBtn);
            tagsList.appendChild(tagItem);
        }
    };

    container.appendChild(tagSelect);
    container.appendChild(addBtn);
    container.appendChild(tagsList);

    return container;
}
