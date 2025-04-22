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
                    updateProjectField(projectId, 'name', nameInput.value);
                }
            };

            descEditButton.onclick = () => {
                if (descInput.disabled) {
                    descInput.disabled = false;
                    descEditButton.textContent = 'Сохранить';
                } else {
                    descInput.disabled = true;
                    descEditButton.textContent = 'Изменить';
                    updateProjectField(projectId, 'description', descInput.value);
                }
            };

            descGroup.appendChild(descLabel);
            descGroup.appendChild(descInput);
            descGroup.appendChild(descEditButton);

            container.appendChild(nameGroup);
            container.appendChild(descGroup);
            viewContent.appendChild(container);

            // --- Теги ---
            renderTagsSection(container, projectId);

            // --- Участники проекта ---
            fetch(`/api/projects/${projectId}/members`)
                .then(response => response.json())
                .then(members => {
                    const membersSection = document.createElement('div');
                    membersSection.className = 'project-members-section';

                    const membersTitle = document.createElement('h3');
                    membersTitle.textContent = 'Участники проекта:';
                    membersSection.appendChild(membersTitle);

                    if (members.length === 0) {
                        const noMembers = document.createElement('p');
                        noMembers.textContent = 'Нет участников';
                        membersSection.appendChild(noMembers);
                    } else {
                        const membersList = document.createElement('ul');
                        members.forEach(member => {
                            const listItem = document.createElement('li');
                            listItem.textContent = member.email;

                            const editBtn = document.createElement('button');
                            editBtn.textContent = 'Редактировать права';
                            editBtn.onclick = () => {
                                // сюда вставишь логику изменения прав
                                alert(`Редактирование прав участника ${member.email}`);
                            };

                            const deleteBtn = document.createElement('button');
                            deleteBtn.textContent = 'Удалить';
                            deleteBtn.onclick = () => {
                                // сюда вставишь логику удаления
                                alert(`Удаление участника ${member.email}`);
                            };

                            listItem.appendChild(editBtn);
                            listItem.appendChild(deleteBtn);
                            membersList.appendChild(listItem);
                        });
                        membersSection.appendChild(membersList);
                    }

                    const addMemberBtn = document.createElement('button');
                    addMemberBtn.textContent = 'Добавить участника';
                    addMemberBtn.onclick = () => {
                        // сюда вставишь форму/модалку
                        alert('Открыть форму добавления участника');
                    };
                    membersSection.appendChild(addMemberBtn);

                    container.appendChild(membersSection);
                })
                .catch(err => {
                    console.error('Ошибка загрузки участников:', err);
                });
            const archiveButton = createArchiveToggleButton(project, projectId);
            container.appendChild(archiveButton);
            renderRolesSection(container, projectId);
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

function createArchiveToggleButton(project, projectId) {
    const button = document.createElement('button');
    button.textContent = project.isArchived ? 'Разархивировать проект' : 'Архивировать проект';

    button.onclick = () => {
        fetch(`/api/projects/archive/${projectId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                archived: !project.isArchived
            })
        })
            .then(response => {
                if (!response.ok) throw new Error('Ошибка архивации проекта');
                return response.json();
            })
            .then(() => {
                loadProjectInfoView(projectId);
            })
            .catch(error => {
                console.error('Ошибка при архивации проекта:', error);
            });
    };

    return button;
}

function renderRolesSection(container, projectId) {
    fetch(`/api/projects/${projectId}/roles`) // Получаем роли проекта и общие (где id_project = null)
        .then(response => response.json())
        .then(roles => {
            const rolesSection = document.createElement('div');
            rolesSection.className = 'roles-section';

            const title = document.createElement('h3');
            title.textContent = 'Роли в проекте';
            rolesSection.appendChild(title);

            if (roles.length === 0) {
                const noRoles = document.createElement('p');
                noRoles.textContent = 'Нет доступных ролей.';
                rolesSection.appendChild(noRoles);
            } else {
                const list = document.createElement('ul');
                roles.forEach(role => {
                    const li = document.createElement('li');
                    li.textContent = role.name + (role.idProject === null ? ' (глобальная)' : '');
                    list.appendChild(li);
                });
                rolesSection.appendChild(list);
            }

            const addRoleButton = document.createElement('button');
            addRoleButton.textContent = 'Добавить роль в проект';
            addRoleButton.onclick = () => {
                renderCreateRoleView(container, projectId, rolesSection);
            };
            rolesSection.appendChild(addRoleButton);

            container.appendChild(rolesSection);
        })
        .catch(error => {
            console.error('Ошибка загрузки ролей:', error);
        });
}

function renderCreateRoleView(container, projectId, previousSection) {
    // Скрываем предыдущую секцию
    previousSection.style.display = 'none';

    // Создаем секцию для создания роли
    const createSection = document.createElement('div');
    createSection.className = 'create-role-section';

    const title = document.createElement('h3');
    title.textContent = 'Создание кастомной роли';
    createSection.appendChild(title);

    // Поле для ввода имени роли
    const nameLabel = document.createElement('label');
    nameLabel.textContent = 'Имя роли:';
    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    createSection.appendChild(nameLabel);
    createSection.appendChild(nameInput);

    // Список прав с чекбоксами
    const permissionsList = document.createElement('div');
    permissionsList.className = 'permissions-list';

    const allPermissions = [
        "add_roles", "edit_roles", "delete_roles",
        "add_project_members", "delete_project_members",
        "rename_project", "edit_project_description",
        "add_task_lists", "rename_task_lists", "edit_task_lists_description", "delete_task_lists",
        "add_tasks_yourself", "add_tasks_another",
        "rename_tasks_yourself", "rename_tasks_another",
        "edit_tasks_description_yourself", "edit_tasks_description_another",
        "delete_tasks_yourself", "delete_tasks_another",
        "add_attachments_yourself", "add_attachments_another",
        "delete_attachments_yourself", "delete_attachments_another",
        "change_task_priority_yourself", "change_task_priority_another",
        "add_priorities", "edit_priorities", "delete_priorities",
        "add_statuses", "edit_statuses", "delete_statuses",
        "add_comments_yourself", "add_comments_another",
        "edit_comments", "delete_comments", "delete_other_comments",
        "add_tags", "edit_tags", "delete_tags",
        "set_tags_yourself", "set_tags_another",
        "delete_tags_yourself", "delete_tags_another",
        "add_subtasks_yourself", "add_subtasks_another",
        "rename_subtasks_yourself", "rename_subtasks_another",
        "edit_subtasks_description_yourself", "edit_subtasks_description_another",
        "make_edit_gantt", "take_tasks", "change_task_assignee", "cancel_tasks",
        "change_roles_another"
    ];

    allPermissions.forEach(permission => {
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = permission;
        checkbox.name = permission;

        const label = document.createElement('label');
        label.htmlFor = permission;
        label.textContent = permission;

        const wrapper = document.createElement('div');
        wrapper.className = 'permission-item';
        wrapper.appendChild(checkbox);
        wrapper.appendChild(label);

        permissionsList.appendChild(wrapper);
    });

    createSection.appendChild(permissionsList);

    // Кнопка "Создать"
    const createButton = document.createElement('button');
    createButton.textContent = 'Создать';
    createButton.onclick = () => {
        const name = nameInput.value.trim();
        if (!name) {
            alert('Имя роли не может быть пустым');
            return;
        }

        // Собираем объект прав
        const permissions = {};
        allPermissions.forEach(p => {
            const checkbox = document.getElementById(p);
            permissions[p] = checkbox.checked;
        });

        // Логируем объект прав для дебага
        console.log("Права перед отправкой на сервер:", permissions);

        // Отправляем данные на сервер
        fetch(`/api/projects/${projectId}/roles`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({
                name: name,
                permissions: permissions
            })
        })
        .then(response => {
            if (response.ok) {
                alert('Роль создана!');
                container.removeChild(createSection);
                previousSection.style.display = '';  // Показываем предыдущую секцию
                renderRolesSection(container, projectId); // Перерисовываем список ролей
            } else {
                // Обработка ошибок, если статус не OK
                response.text().then(errorMessage => {
                    alert(`Ошибка при создании роли: ${errorMessage}`);
                });
            }
        })
        .catch(err => {
            // Логирование ошибок при отправке
            console.error('Ошибка при отправке данных:', err);
            alert('Произошла ошибка при отправке данных на сервер');
        });
    };

    createSection.appendChild(createButton);
    container.appendChild(createSection);
}

