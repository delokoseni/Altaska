import { renderAddMemberForm } from './renderAddMemberForm.js';
import { removeProjectMember } from './memberActions.js';
import { deleteProject } from './projectActions.js';
import { createSubtasksSection } from './subTasks.js';
import { addPerformer } from './taskPerformers.js';
import { deleteTask } from './deleteTask.js';
import { renderKanbanFiltersAndBoard } from './kanban.js';
import { renderGanttChart } from './gantt.js';

window.selectProject = selectProject;
window.loadView = loadView;

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

                        const deleteIcon = document.createElement('img');
                        deleteIcon.src = '/icons/trash.svg';
                        deleteIcon.alt = 'Удалить задачу';
                        deleteIcon.className = 'delete-task-icon';
                        deleteIcon.width = 24;
                        deleteIcon.height = 24;

                        deleteIcon.onclick = (e) => {
                            e.stopPropagation();
                            if (confirm('Вы уверены, что хотите удалить эту задачу?')) {
                                deleteTask(task.id, csrfToken);
                            }
                        };

                        taskDiv.appendChild(deleteIcon);

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
    if (view === 'канбан') {
        renderKanbanFiltersAndBoard(projectId, showTaskForm);
    }
    if (view === 'гант') {
        renderGanttChart(projectId);
    }
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
            return Promise.all([
                priorities,
                fetch(`/api/projects/${projectId}/confirmed-members`).then(res => res.json())
            ]);
        })
        .then(([priorities, members]) => {
            const wrapper = document.createElement('div');
            wrapper.classList.add('task-form-wrapper');

            const form = document.createElement('form');
            form.className = 'task-form';

            const createdAtInput = document.createElement('input');
            createdAtInput.type = 'hidden';
            createdAtInput.name = 'createdAt';

            const updatedAtInput = document.createElement('input');
            updatedAtInput.type = 'hidden';
            updatedAtInput.name = 'updatedAt';

            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = csrfParam;
            csrfInput.value = csrfToken;

            form.append(createdAtInput, updatedAtInput, csrfInput);

            const nameInput = document.createElement('input');
            nameInput.type = 'text';
            nameInput.placeholder = 'Название задачи';
            nameInput.name = 'name';
            nameInput.required = true;

            const descriptionInput = document.createElement('textarea');
            descriptionInput.placeholder = 'Описание задачи';
            descriptionInput.name = 'description';

            const deadlineInput = document.createElement('input');
            deadlineInput.type = 'datetime-local';
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

            const performerLabel = document.createElement('div');
            performerLabel.className = 'performer-container';
            performerLabel.textContent = 'Назначить исполнителей:';

            const performerCheckboxes = [];

            members.forEach(m => {
                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.value = m.userId;
                checkbox.name = 'performerIds';

                const label = document.createElement('label');
                label.appendChild(checkbox);
                label.append(` ${m.email}`);

                performerCheckboxes.push(checkbox);
                performerLabel.appendChild(label);
                performerLabel.appendChild(document.createElement('br'));
            });

            const selectedTagIds = [];
            const tagSelector = createTagSelector(projectId, selectedTagIds);

            const subtasksSection = createSubtasksSection();

            const filesInput = document.createElement('input');
            filesInput.type = 'file';
            filesInput.name = 'files';
            filesInput.multiple = true;

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

            form.append(
                backButton,
                nameInput,
                descriptionInput,
                deadlineInput,
                prioritySelect,
                tagSelector,
                performerLabel,
                subtasksSection,
                filesInput,
                submitBtn
            );

            form.onsubmit = function (e) {
                e.preventDefault();

                // Проверка файлов
                const maxFileSize = 25 * 1024 * 1024; // 25MB
                for (let file of filesInput.files) {
                    if (file.size > maxFileSize) {
                        alert(`Файл "${file.name}" превышает 25 МБ`);
                        return;
                    }
                }

                const now = new Date();
                createdAtInput.value = now.toISOString().split('T')[0];
                updatedAtInput.value = now.toISOString();

                const formData = new FormData(form);
                selectedTagIds.forEach(tagId => formData.append('tagIds', tagId));

                const subtaskElements = subtasksSection.querySelectorAll('.subtask-item');
                const subtasksToSend = [];
                subtaskElements.forEach(item => {
                    const name = item.querySelector('.subtask-name').value.trim();
                    const description = item.querySelector('.subtask-description').value.trim();
                    if (name) {
                        subtasksToSend.push({ name, description });
                    }
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
                    const taskId = data.id;

                    // Назначение исполнителей
                    const selectedUserIds = performerCheckboxes
                        .filter(cb => cb.checked)
                        .map(cb => cb.value);
                    const performerPromises = selectedUserIds.map(userId => addPerformer(taskId, userId, csrfToken));

                    // Загрузка файлов
                    const files = filesInput.files;
                    const uploadPromises = [];

                    for (let i = 0; i < files.length; i++) {
                        const formDataFile = new FormData();
                        formDataFile.append('file', files[i]);
                        formDataFile.append('taskId', taskId);

                        const uploadPromise = fetch('/api/files/upload', {
                            method: 'POST',
                            headers: {
                                'X-CSRF-TOKEN': csrfToken
                            },
                            body: formDataFile
                        });

                        uploadPromises.push(uploadPromise);
                    }

                    return Promise.all([
                        ...performerPromises,
                        subtasksToSend.length > 0
                            ? fetch(`/api/tasks/${taskId}/subtasks`, {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                    'X-CSRF-TOKEN': csrfToken
                                },
                                body: JSON.stringify(subtasksToSend)
                            })
                            : Promise.resolve(),
                        ...uploadPromises
                    ]);
                })
                .then(() => {
                    loadView('список', projectId);
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                    alert('Произошла ошибка при создании задачи');
                });
            };

            wrapper.appendChild(form);
            container.appendChild(wrapper);
        });
}

function loadProjectInfoView(projectId) {
    const viewContent = document.querySelector('.view-content');
    viewContent.innerHTML = '';
    const wrapper = document.createElement('div');
    wrapper.className = 'project-info-view';
    viewContent.appendChild(wrapper);


    fetch(`/api/projects/${projectId}`)
        .then(response => response.json())
        .then(project => {
            const container = document.createElement('div');
            container.className = 'project-info-container';

            // --- Название ---
            const nameGroup = document.createElement('div');
            const nameLabel = document.createElement('h3');
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
            const descLabel = document.createElement('h3');
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
            wrapper.appendChild(container);

            // --- Теги ---
            renderTagsSection(container, projectId);

            Promise.all([
                fetch(`/api/projects/${projectId}/members`).then(res => res.json()),
                fetch(`/api/projects/${projectId}/roles`).then(res => res.json())
            ]).then(([members, roles]) => {
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
                        const listItem = renderMemberItem(member, projectId, roles);
                        membersList.appendChild(listItem);
                    });
                    membersSection.appendChild(membersList);
                }

                const addMemberBtn = document.createElement('button');
                addMemberBtn.textContent = 'Добавить участника';
                addMemberBtn.onclick = () => {
                    renderAddMemberForm(container, projectId, roles, container.innerHTML, loadProjectInfoView);
                };
                membersSection.appendChild(addMemberBtn);

                container.appendChild(membersSection);
            }).catch(err => {
                console.error('Ошибка загрузки участников или ролей:', err);
            });
            renderRolesSection(container, projectId);
            const archiveButton = createArchiveToggleButton(project, projectId);
            container.appendChild(archiveButton);
            const deleteButton = document.createElement('button');
            deleteButton.textContent = 'Удалить проект';
            deleteButton.style.backgroundColor = 'red';
            deleteButton.style.color = 'white';
            deleteButton.style.marginTop = '20px';
            deleteButton.onclick = () => {
                deleteProject(projectId, () => {
                    window.location.reload();
                });
            };
            container.appendChild(deleteButton);
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

    const tagsTitle = document.createElement('h3');
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
    editBtn.className = 'tag-edit-button';
    editBtn.innerHTML = `<img src="/icons/pencil.svg" alt="Редактировать" class="icon">`;
    editBtn.onclick = () => editTag(tag.id, tag.name, projectId);

    const delBtn = document.createElement('button');
    delBtn.className = 'tag-delete-button';
    delBtn.innerHTML = `<img src="/icons/trash.svg" alt="Удалить" class="icon">`;
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
    // Не добавлять форму повторно
    if (container.querySelector('.add-tag-form-wrapper')) return;

    const formWrapper = document.createElement('div');
    formWrapper.className = 'add-tag-form-wrapper';

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = 'Введите тэг';
    input.classList.add('add-tag');

    const saveBtn = document.createElement('button');
    saveBtn.textContent = 'Сохранить';
    saveBtn.classList.add('save-tag-button');

    const cancelBtn = document.createElement('button');
    cancelBtn.textContent = 'Отмена';
    cancelBtn.classList.add('cancel-tag-button');
    cancelBtn.onclick = () => formWrapper.remove();

    const submitHandler = () => {
        if (!input.value.trim()) return;

        const formData = new FormData();
        formData.append('name', input.value.trim());
        formData.append(csrfParam, csrfToken);

        fetch(`/api/tags/create/${projectId}`, {
            method: 'POST',
            body: formData
        }).then(() => loadProjectInfoView(projectId));
    };

    saveBtn.onclick = submitHandler;

    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            e.preventDefault();
            submitHandler();
        }
    });

    formWrapper.appendChild(input);
    formWrapper.appendChild(saveBtn);
    formWrapper.appendChild(cancelBtn);
    container.appendChild(formWrapper);
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
    fetch(`/api/projects/${projectId}/roles`)
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

                    const text = document.createElement('span');
                    text.textContent = role.name + (role.idProject === null ? ' (глобальная)' : '');
                    li.appendChild(text);

                    // Только для проектных ролей
                    if (role.idProject !== null) {
                        const editButton = document.createElement('button');
                        editButton.className = 'role-edit-button';
                        editButton.innerHTML = `<img src="/icons/pencil.svg" alt="Редактировать" class="icon">`;
                        editButton.onclick = () => {
                            renderEditRoleView(container, projectId, role, rolesSection);
                        };
                        li.appendChild(editButton);

                        const deleteButton = document.createElement('button');
                        deleteButton.className = 'role-delete-button';
                        deleteButton.innerHTML = `<img src="/icons/trash.svg" alt="Удалить" class="icon">`;
                        deleteButton.onclick = () => {
                            deleteRole(role.id, projectId, rolesSection);
                        };
                        li.appendChild(deleteButton);
                    }


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
                previousSection.remove(); // Показываем предыдущую секцию
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

function deleteRole(roleId, projectId, container) {
    if (!confirm('Удалить эту роль?')) return;

    fetch(`/api/projects/${projectId}/roles/${roleId}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    })
    .then(response => {
        if (response.ok) {
            // Перерисовываем блок с ролями
            container.innerHTML = '';
            renderRolesSection(container, projectId);
        } else {
            console.error('Ошибка при удалении роли:', response.statusText);
        }
    })
    .catch(error => {
        console.error('Ошибка при удалении роли:', error);
    });
}

function renderEditRoleView(container, projectId, role, previousSection) {
    previousSection.style.display = 'none';

    const editSection = document.createElement('div');
    editSection.className = 'edit-role-section';

    const title = document.createElement('h3');
    title.textContent = `Редактирование роли "${role.name}"`;
    editSection.appendChild(title);

    const nameLabel = document.createElement('label');
    nameLabel.textContent = 'Новое имя роли:';
    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.value = role.name;
    editSection.appendChild(nameLabel);
    editSection.appendChild(nameInput);

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
        checkbox.id = 'edit_' + permission;
        checkbox.name = permission;
        checkbox.checked = role.permissions?.[permission] || false;

        const label = document.createElement('label');
        label.htmlFor = 'edit_' + permission;
        label.textContent = permission;

        const wrapper = document.createElement('div');
        wrapper.className = 'permission-item';
        wrapper.appendChild(checkbox);
        wrapper.appendChild(label);

        permissionsList.appendChild(wrapper);
    });

    editSection.appendChild(permissionsList);

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.onclick = async () => {
        const newName = nameInput.value.trim();
        if (!newName) {
            alert('Имя роли не может быть пустым');
            return;
        }

        const updatedPermissions = {};
        allPermissions.forEach(p => {
            updatedPermissions[p] = document.getElementById('edit_' + p).checked;
        });

        const updatedRole = await updateRole(projectId, role.id, {
            name: newName,
            permissions: updatedPermissions
        }, csrfToken);

        if (updatedRole) {
            alert('Роль обновлена!');
            container.removeChild(editSection);
            previousSection.remove();
            renderRolesSection(container, projectId);
        }
    };

    editSection.appendChild(saveButton);
    container.appendChild(editSection);
}

async function updateRole(projectId, roleId, updatedData, csrfToken) {
    try {
        const response = await fetch(`/api/projects/${projectId}/roles/${roleId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(updatedData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Ошибка при обновлении роли: ${errorText}`);
        }

        const updatedRole = await response.json();
        console.log("Роль обновлена:", updatedRole);
        return updatedRole;
    } catch (error) {
        console.error(error.message);
    }
}

function renderMemberItem(member, projectId, roles) {
    const listItem = document.createElement('li');

    // Почта участника
    const emailSpan = document.createElement('span');
    emailSpan.textContent = member.email + (member.confirmed ? ' ' : ' (Приглашение отправлено) ');
    emailSpan.style.color = member.confirmed ? '' : '#888';
    listItem.appendChild(emailSpan);

    // Выпадающий список ролей
    const roleSelect = document.createElement('select');

    roles.forEach(role => {
        const option = document.createElement('option');
        option.value = role.id;
        option.textContent = role.name;
        if (role.id === member.roleId) {
            option.selected = true;
        }
        roleSelect.appendChild(option);
    });

    roleSelect.onchange = () => {
        const newRoleId = parseInt(roleSelect.value);
        if (newRoleId !== member.roleId) {
            fetch(`/api/projects/${projectId}/members/${member.userId}/role`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: JSON.stringify({ roleId: newRoleId })
            })
            .then(res => {
                if (res.ok) {
                    alert('Роль обновлена');
                    loadProjectInfoView(projectId);
                } else {
                    res.text().then(text => alert('Ошибка: ' + text));
                }
            })
            .catch(err => console.error('Ошибка смены роли:', err));
        }
    };

    listItem.appendChild(roleSelect);

    // Кнопка удаления участника
    const deleteBtn = document.createElement('button');
    if(member.confirmed)
        deleteBtn.textContent = 'Удалить';
    else
        deleteBtn.textContent = 'Отозвать';
    deleteBtn.onclick = () => {
        removeProjectMember(projectId, member.userId, member.email, loadProjectInfoView);
    };
    listItem.appendChild(deleteBtn);

    return listItem;
}

